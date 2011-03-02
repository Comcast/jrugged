package org.fishwife.jrugged.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.fishwife.jrugged.ServiceWrapper;
import org.fishwife.jrugged.ServiceWrapperFactory;

public class PerHostServiceWrappedHttpClient extends AbstractHttpClientDecorator {

    private ServiceWrapperFactory factory;
    private Map<HttpHost, HttpClient> clients = new HashMap<HttpHost, HttpClient>();
    
    public PerHostServiceWrappedHttpClient(HttpClient backend, ServiceWrapperFactory factory) {
        super(backend);
        this.factory = factory;
    }

    public HttpResponse execute(HttpHost host, HttpRequest req, HttpContext ctx)
            throws IOException, ClientProtocolException {
        host = getCanonicalHost(host);
        HttpClient client = clients.get(host);
        if (client == null) {
            ServiceWrapper wrapper = factory.getWrapperWithName(host.toHostString());
            client = new ServiceWrappedHttpClient(backend, wrapper);
            clients.put(host, client);
        }
        return client.execute(host, req, ctx);
    }

    private HttpHost getCanonicalHost(HttpHost host) {
        URI uri;
        try {
            uri = new URI(host.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String hostname = uri.getHost();
        int port = uri.getPort();
        String scheme = uri.getScheme();
        boolean isHttps = "HTTPS".equalsIgnoreCase(scheme);
        String schemePart = isHttps ? (scheme + "://") : ""; 
        if (port == -1) {
            port = isHttps ? 443 : 80;
        }
        return new HttpHost(schemePart + hostname + ":" + port);
    }

}
