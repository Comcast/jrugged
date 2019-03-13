/* ServiceWrappedHttpClientDecorator.java
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

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.fishwife.jrugged.ServiceWrapper;

/**
 * Decorator that runs {@link org.apache.http.client.HttpClient} request
 * executions through a {@link ServiceWrapper}.
 */
public class ServiceWrappedHttpClientDecorator extends AbstractHttpClientDecorator {

    private ServiceWrapper wrapper;

    public ServiceWrappedHttpClientDecorator(HttpClient backend, ServiceWrapper wrapper) {
        super(backend);
        this.wrapper = wrapper;
    }

    public HttpResponse execute(final HttpHost host, final HttpRequest req, final HttpContext ctx)
            throws IOException, ClientProtocolException {
        try {
            return wrapper.invoke(new Callable<HttpResponse>() {
                public HttpResponse call() throws Exception {
                    return backend.execute(host, req, ctx);
                }
            });
        } catch (IOException ioe) {
            throw(ioe);
        } catch (RuntimeException re) {
            throw(re);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
