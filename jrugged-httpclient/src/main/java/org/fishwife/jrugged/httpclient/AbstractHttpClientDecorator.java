package org.fishwife.jrugged.httpclient;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * General decorator class for an {@link HttpClient}; implementors for
 * decorators need only implement a single abstract method.
 */
public abstract class AbstractHttpClientDecorator implements HttpClient {

    protected HttpClient backend;
    
    public AbstractHttpClientDecorator(HttpClient backend) {
        this.backend = backend;
    }
    
    public abstract HttpResponse execute(HttpHost host, HttpRequest req,
            HttpContext ctx) throws IOException, ClientProtocolException;
    
    protected HttpHost getHttpHost(HttpUriRequest req) {
        URI uri = req.getURI();
        String scheme = uri.getScheme();
        if ("HTTPS".equalsIgnoreCase(scheme)) {
            return new HttpHost(uri.getScheme() + "://" + uri.getAuthority());
        } else {
            return new HttpHost(uri.getAuthority());
        }
    }
    
    public HttpResponse execute(HttpUriRequest req) throws IOException,
            ClientProtocolException {
        return execute(req, (HttpContext)null);
    }

    public HttpResponse execute(HttpUriRequest req, HttpContext ctx)
            throws IOException, ClientProtocolException {
        return execute(getHttpHost(req), req, ctx);
    }

    public HttpResponse execute(HttpHost host, HttpRequest req)
            throws IOException, ClientProtocolException {
        return execute(host, req, (HttpContext)null);
    }

    public <T> T execute(HttpUriRequest req, ResponseHandler<? extends T> rh)
            throws IOException, ClientProtocolException {
        return rh.handleResponse(execute(req));
    }

    public <T> T execute(HttpUriRequest req,
            ResponseHandler<? extends T> rh, HttpContext ctx)
            throws IOException, ClientProtocolException {
        return rh.handleResponse(execute(req, ctx));
    }

    public <T> T execute(HttpHost host, HttpRequest req,
            ResponseHandler<? extends T> rh) throws IOException,
            ClientProtocolException {
        return rh.handleResponse(execute(host, req));
    }

    public <T> T execute(HttpHost host, HttpRequest req,
            ResponseHandler<? extends T> rh, HttpContext ctx)
            throws IOException, ClientProtocolException {
        return rh.handleResponse(execute(host, req, ctx));
    }

    public ClientConnectionManager getConnectionManager() {
        return backend.getConnectionManager();
    }
    
    public HttpParams getParams() {
        return backend.getParams();
    }
}

