/* TestFailureExposingHttpClient.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
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

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;


public class TestFailureExposingHttpClient {

    private FailureExposingHttpClient impl;
    private StubHttpClient backend;
    private HttpResponse resp;
    private HttpHost host;
    private HttpRequest req;
    private HttpContext ctx;

    @Before
    public void setUp() {
        backend = new StubHttpClient();
        impl = new FailureExposingHttpClient(backend);
        host = new HttpHost("foo.example.com");
        req = new HttpGet("http://foo.example.com/");
        ctx = new BasicHttpContext();
    }

    @Test
    public void returns1XXResponseAsIs() throws Exception {
        for(int i = 100; i <= 199; i++) {
            resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, i, "1XX Thingy");
            backend.setResponse(resp);
            assertSame(resp, impl.execute(host, req, ctx));
        }
    }

    @Test
    public void returns2XXResponseAsIs() throws Exception {
        for(int i = 200; i <= 299; i++) {
            resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, i, "Success");
            backend.setResponse(resp);
            assertSame(resp, impl.execute(host, req, ctx));
        }
    }

    @Test
    public void returns3XXResponseAsIs() throws Exception {
        for(int i = 300; i <= 399; i++) {
            resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, i, "3XX Thingy");
            backend.setResponse(resp);
            assertSame(resp, impl.execute(host, req, ctx));
        }
    }

    @Test
    public void generatesFailureOn4XXResponse() throws Exception {
        for(int i = 400; i <= 499; i++) {
            resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, i, "Client Error");
            backend.setResponse(resp);
            try {
                impl.execute(host, req, ctx);
                fail("should have thrown exception");
            } catch (UnsuccessfulResponseException expected) {
            }
        }
    }

    @Test
    public void generatesFailureOn5XXResponse() throws Exception {
        for(int i = 500; i <= 599; i++) {
            resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, i, "Server Error");
            backend.setResponse(resp);
            try {
                impl.execute(host, req, ctx);
                fail("should have thrown exception");
            } catch (UnsuccessfulResponseException expected) {
            }
        }
    }

    @Test(expected=UnsuccessfulResponseException.class)
    public void exposesFailureIfAssessorSaysTo() throws Exception {
        resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        backend.setResponse(resp);
        impl = new FailureExposingHttpClient(backend, new ResponseFailureAssessor() {
            public boolean isFailure(HttpResponse response) {
                return true;
            }
        });
        impl.execute(host, req, ctx);
    }

    @Test
    public void doesNotExposeFailureIfAssessorSaysNotTo() throws Exception {
        resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Bork");
        backend.setResponse(resp);
        impl = new FailureExposingHttpClient(backend, new ResponseFailureAssessor() {
            public boolean isFailure(HttpResponse response) {
                return false;
            }
        });
        HttpResponse result = impl.execute(host, req, ctx);
        assertSame(result, resp);
    }

    private static class StubHttpClient extends AbstractHttpClientDecorator {
        private HttpResponse response;

        public StubHttpClient() { super(null); }

        public HttpResponse execute(HttpHost host, HttpRequest req,
                HttpContext ctx) throws IOException, ClientProtocolException {
            return response;
        }

        public void setResponse(HttpResponse resp) {
            this.response = resp;
        }
    }
}
