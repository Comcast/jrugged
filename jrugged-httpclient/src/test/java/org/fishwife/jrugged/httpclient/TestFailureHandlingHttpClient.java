package org.fishwife.jrugged.httpclient;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.fishwife.jrugged.CircuitBreakerException;
import org.junit.Before;
import org.junit.Test;


public class TestFailureHandlingHttpClient {

    private FailureHandlingHttpClient impl;
    private HttpClient mockBackend;
    private HttpResponse resp;
    private HttpHost host;
    private HttpRequest req;
    private HttpContext ctx;
    
    @Before
    public void setUp() {
        mockBackend = createMock(HttpClient.class);
        resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        host = new HttpHost("foo.example.com");
        req = new HttpGet("http://foo.example.com/");
        ctx = new BasicHttpContext();
        impl = new FailureHandlingHttpClient(mockBackend);
    }
    
    @Test
    public void returnsBackendResponseOnSuccess() throws Exception {
       expect(mockBackend.execute(host, req, ctx))
           .andReturn(resp);
       replay(mockBackend);
       HttpResponse result = impl.execute(host, req, ctx);
       verify(mockBackend);
       assertSame(resp, result);
    }
    
    @Test
    public void returnsEnclosedResponseOnUnsuccessfulException() throws Exception {
        Exception e = new UnsuccessfulResponseException(resp);
        expect(mockBackend.execute(host, req, ctx))
            .andThrow(e);
        replay(mockBackend);    
        HttpResponse result = impl.execute(host, req, ctx);
        verify(mockBackend);
        assertSame(resp, result);
    }
    
    @Test
    public void returns504ForCircuitBreakerException() throws Exception {
        expect(mockBackend.execute(host, req, ctx))
            .andThrow(new CircuitBreakerException());
        replay(mockBackend);    
        HttpResponse result = impl.execute(host, req, ctx);
        verify(mockBackend);
        assertEquals(HttpStatus.SC_GATEWAY_TIMEOUT, result.getStatusLine().getStatusCode());
    }
}
