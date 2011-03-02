package org.fishwife.jrugged.httpclient;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

public class TestAbstractHttpClientDecorator {

    private AbstractHttpClientDecorator impl;

    @Before
    public void setUp() {
        impl = new AbstractHttpClientDecorator(null) {
            public HttpResponse execute(HttpHost host, HttpRequest req,
                    HttpContext ctx) throws IOException,
                    ClientProtocolException {
                throw new IllegalStateException("not implemented");
            }
        };
    }
    
    @Test
    public void canExtractSimpleHostProperly() {
        assertEquals(new HttpHost("foo.example.com"),
                impl.getHttpHost(new HttpGet("http://foo.example.com/bar")));
    }
    
    @Test
    public void canExtractHostWithPort() {
        assertEquals(new HttpHost("foo.example.com:8080"),
                impl.getHttpHost(new HttpGet("http://foo.example.com:8080/bar")));        
    }
    
    @Test
    public void canExtractHttpsHostProperly() {
        assertEquals(new HttpHost("https://foo.example.com:443"),
                impl.getHttpHost(new HttpGet("https://foo.example.com:443/bar")));                
    }
}
