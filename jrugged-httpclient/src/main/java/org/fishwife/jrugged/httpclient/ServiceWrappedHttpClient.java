/* ServiceWrappedHttpClient.java
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

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.fishwife.jrugged.ServiceWrapper;

/**
 * Facade class for assembling an HttpClient that will feed request
 * executions through a ServiceWrapper, while exposing 4XX and 5XX
 * responses as exceptions so that the ServiceWrapper can experience them
 * as failures, but unwrapping those exceptions before getting up to
 * the original caller.
 */
public class ServiceWrappedHttpClient extends AbstractHttpClientDecorator {

    private HttpClient client;
    
    public ServiceWrappedHttpClient(HttpClient backend, ServiceWrapper wrapper) {
        super(backend);
        HttpClient client1 = new FailureExposingHttpClient(backend);
        HttpClient client2 = new ServiceWrappedHttpClientDecorator(client1, wrapper);
        this.client = new FailureHandlingHttpClient(client2); 
    }

    public HttpResponse execute(HttpHost host, HttpRequest req, HttpContext ctx)
            throws IOException, ClientProtocolException {
        return client.execute(host, req, ctx);
    }

}
