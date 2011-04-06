/* TestFailureHandlingHttpClient.java
 * 
 * Copyright 2009-2011 Comcast Interactive Media, LLC.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fishwife.jrugged.httpclient;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;

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
    public void throwsIOExceptionForCircuitBreakerException() throws Exception {
        expect(mockBackend.execute(host, req, ctx))
            .andThrow(new CircuitBreakerException());
        replay(mockBackend);    
        try {
            impl.execute(host, req, ctx);
            fail("should have thrown exception");
        } catch (IOException expected) {
        }
        verify(mockBackend);
    }
}
