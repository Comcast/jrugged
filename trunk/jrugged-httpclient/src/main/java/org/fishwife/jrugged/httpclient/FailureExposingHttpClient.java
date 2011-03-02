package org.fishwife.jrugged.httpclient;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * This is a decorator for an {@link org.apache.http.client.HttpClient}
 * that will raise runtime exceptions for 4XX or 5XX responses, so that
 * they can be used to signal failures to JRugged
 * {@link org.fishwife.jrugged.ServiceWrapper} instances. 
 *
 */
public class FailureExposingHttpClient extends AbstractHttpClientDecorator {

    public FailureExposingHttpClient(HttpClient backend) {
        super(backend);
    }

    public HttpResponse execute(HttpHost host, HttpRequest req, HttpContext ctx)
            throws IOException, ClientProtocolException {
        HttpResponse resp = backend.execute(host, req, ctx);
        if (isFailedResponse(resp)) {
            throw new UnsuccessfulResponseException(resp);
        }
        return resp;
    }

    private boolean isFailedResponse(HttpResponse resp) {
        int status = resp.getStatusLine().getStatusCode();
        return (status >= 400 && status <= 499) || 
                (status >= 500 && status <= 599);
    }
}
