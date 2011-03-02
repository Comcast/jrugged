package org.fishwife.jrugged.httpclient;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.fishwife.jrugged.ServiceWrapper;

/**
 * Decorator that runs {@link org.apache.http.client.HttpClient} request
 * executions through a {@link ServiceWrapper}.
 */
public class ServiceWrappedHttpClientDecorator extends AbstractHttpClientDecorator {

    private ServiceWrapper wrapper;
    
    public ServiceWrappedHttpClientDecorator(HttpClient backend, ServiceWrapper wrapper) {
        super(backend);
        this.wrapper = wrapper;
    }

    public HttpResponse execute(final HttpHost host, final HttpRequest req, final HttpContext ctx)
            throws IOException, ClientProtocolException {
        try {
            return wrapper.invoke(new Callable<HttpResponse>() {
                public HttpResponse call() throws Exception {
                    return backend.execute(host, req, ctx);
                }
            });
        } catch (IOException ioe) {
            throw(ioe);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
