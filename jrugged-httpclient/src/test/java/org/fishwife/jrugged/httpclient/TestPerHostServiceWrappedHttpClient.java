/* TestPerHostServiceWrappedHttpClient.java
 * 
 * Copyright 2009-2012 Comcast Interactive Media, LLC.
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

import java.util.concurrent.Callable;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.fishwife.jrugged.ServiceWrapper;
import org.fishwife.jrugged.ServiceWrapperFactory;
import org.junit.Before;
import org.junit.Test;

public class TestPerHostServiceWrappedHttpClient {

    private PerHostServiceWrappedHttpClient impl;
    private HttpClient mockBackend;
    private ServiceWrapperFactory mockFactory;
    private final static String HOST_STRING = "foo.example.com:8080";
    private HttpHost host;
    private HttpRequest req;
    private HttpContext ctx;
    private HttpResponse resp;

    @Before
    public void setUp() {
        host = new HttpHost(HOST_STRING);
        req = new HttpGet("http://foo.example.com:8080/");
        ctx = new BasicHttpContext();
        resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");

        mockBackend = createMock(HttpClient.class);
        mockFactory = createMock(ServiceWrapperFactory.class);
        impl = new PerHostServiceWrappedHttpClient(mockBackend, mockFactory);        
    }

    private void replayMocks() {
        replay(mockBackend);
        replay(mockFactory);
    }

    private void verifyMocks() {
        verify(mockBackend);
        verify(mockFactory);
    }
    
    @Test
    public void isAnHttpClient() {
        assertTrue(impl instanceof HttpClient);
    }
    
    @Test
    public void usesRequestHostToCreateWrapper() throws Exception {
        final ServiceWrapper wrapper = new NullWrapper();
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
                .andReturn(resp);
        expect(mockFactory.getWrapperWithName(HOST_STRING)).andReturn(wrapper);
        replayMocks();
        impl.execute(host, req, ctx);
        verifyMocks();
    }

    @Test
    public void wiresWrapperUpToRequest() throws Exception {
        final Flag f = new Flag();
        ServiceWrapper wrapper = new NullWrapper(f);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp);
        expect(mockFactory.getWrapperWithName(isA(String.class))).andReturn(wrapper);
        replayMocks();
        impl.execute(host, req, ctx);
        verifyMocks();
        assertTrue(f.set);
    }
    
    @Test
    public void reusesWrapperForRequestsFromSameHost() throws Exception {
        ServiceWrapper wrapper = new NullWrapper();
        HttpUriRequest req1 = new HttpGet("http://foo.example.com/bar");
        HttpUriRequest req2 = new HttpGet("http://foo.example.com/baz");
        HttpResponse resp1 = resp;
        HttpResponse resp2 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        expect(mockFactory.getWrapperWithName(isA(String.class))).andReturn(wrapper);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp1);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp2);

        replayMocks();
        impl.execute(req1, ctx);
        impl.execute(req2, ctx);
        verifyMocks();
    }
    
    @Test
    public void reusesWrapperForRequestsFromEquivalentHostsDefaultHttpPort() throws Exception {
        ServiceWrapper wrapper = new NullWrapper();
        HttpUriRequest req1 = new HttpGet("http://foo.example.com/bar");
        HttpUriRequest req2 = new HttpGet("http://foo.example.com:80/baz");
        HttpResponse resp1 = resp;
        HttpResponse resp2 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        expect(mockFactory.getWrapperWithName(isA(String.class))).andReturn(wrapper);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp1);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp2);

        replayMocks();
        impl.execute(req1, ctx);
        impl.execute(req2, ctx);
        verifyMocks();
    }
    
    @Test
    public void reusesWrapperForRequestsFromEquivalentHostsDefaultHttpsPort() throws Exception {
        ServiceWrapper wrapper = new NullWrapper();
        HttpUriRequest req1 = new HttpGet("https://foo.example.com/bar");
        HttpUriRequest req2 = new HttpGet("https://foo.example.com:443/baz");
        HttpResponse resp1 = resp;
        HttpResponse resp2 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        expect(mockFactory.getWrapperWithName(isA(String.class))).andReturn(wrapper);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp1);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp2);

        replayMocks();
        impl.execute(req1, ctx);
        impl.execute(req2, ctx);
        verifyMocks();
    }

    @Test
    public void reusesWrapperForRequestsFromEquivalentHostsCaseInsensitiveHostName() throws Exception {
        ServiceWrapper wrapper = new NullWrapper();
        HttpUriRequest req1 = new HttpGet("http://foo.example.com/bar");
        HttpUriRequest req2 = new HttpGet("http://FOO.Example.cOM/baz");
        HttpResponse resp1 = resp;
        HttpResponse resp2 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        expect(mockFactory.getWrapperWithName(isA(String.class))).andReturn(wrapper);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp1);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp2);

        replayMocks();
        impl.execute(req1, ctx);
        impl.execute(req2, ctx);
        verifyMocks();
    }
    
    @Test
    public void usesDifferentWrappersForDifferentHosts() throws Exception {
        Flag f1 = new Flag();
        ServiceWrapper wrapper1 = new NullWrapper(f1);
        Flag f2 = new Flag();
        ServiceWrapper wrapper2 = new NullWrapper(f2);
        HttpUriRequest req1 = new HttpGet("http://foo.example.com/");
        HttpUriRequest req2 = new HttpGet("http://bar.example.com/");
        HttpResponse resp1 = resp;
        HttpResponse resp2 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        expect(mockFactory.getWrapperWithName("foo.example.com:80")).andReturn(wrapper1);
        expect(mockFactory.getWrapperWithName("bar.example.com:80")).andReturn(wrapper2);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp1);
        expect(mockBackend.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(HttpContext.class)))
            .andReturn(resp2);

        replayMocks();
        impl.execute(req1, ctx);
        assertTrue(f1.set && !f2.set);
        impl.execute(req2, ctx);
        assertTrue(f1.set && f2.set);
        verifyMocks();        
    }
    
    private static class Flag {
        public boolean set;
    }
    
    private static class NullWrapper implements ServiceWrapper {

        private Flag f = new Flag();
        public NullWrapper() { }
        public NullWrapper(Flag f) { this.f = f; }
        
        public <T> T invoke(Callable<T> c) throws Exception {
            f.set = true;
            return c.call();
        }

        public void invoke(Runnable r) throws Exception {
            f.set = true;
            r.run();
        }

        public <T> T invoke(Runnable r, T result) throws Exception {
            f.set = true;
            r.run();
            return result;
        }
    }
}
