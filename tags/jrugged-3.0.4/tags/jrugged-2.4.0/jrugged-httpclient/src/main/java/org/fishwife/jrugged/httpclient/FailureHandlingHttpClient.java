/* FailureHandlingHttpClient.java
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

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.fishwife.jrugged.CircuitBreakerException;

public class FailureHandlingHttpClient extends AbstractHttpClientDecorator {

    public FailureHandlingHttpClient(HttpClient backend) {
        super(backend);
    }

    public HttpResponse execute(HttpHost host, HttpRequest req, HttpContext ctx)
            throws IOException, ClientProtocolException {
        try {
            return backend.execute(host, req, ctx);
        } catch (UnsuccessfulResponseException ure) {
            return ure.getResponse();
        } catch (CircuitBreakerException cbe) {
            HttpResponse resp = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_GATEWAY_TIMEOUT, "Gateway Timeout");
            String msg = "CircuitBreaker was tripped.\n";
            resp.setEntity(new ByteArrayEntity(msg.getBytes()));
            resp.setHeader("Content-Type","text/plain;utf-8");
            resp.setHeader("Content-Length", resp.getEntity().getContentLength() + "");
            resp.setHeader("Server", "jrugged-httpclient/0.0.1");
            resp.setHeader("Date", DateUtils.formatDate(new Date()));
            return resp;
        }
    }

}
