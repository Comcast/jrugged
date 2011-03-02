package org.fishwife.jrugged.httpclient;

import org.apache.http.HttpResponse;

/**
 * Used to wrap 4XX or 5XX responses from an HTTP server in exceptions
 * so that {@link org.fishwife.jrugged.ServiceWrapper} instances can
 * experience them as failures.
 */
public class UnsuccessfulResponseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private HttpResponse response;
    
    public UnsuccessfulResponseException(HttpResponse resp) {
        this.response = resp;
    }

    public HttpResponse getResponse() {
        return response;
    }
}
