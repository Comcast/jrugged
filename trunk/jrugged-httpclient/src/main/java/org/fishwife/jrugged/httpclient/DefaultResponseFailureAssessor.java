package org.fishwife.jrugged.httpclient;

import org.apache.http.HttpResponse;

/** A {@link ResponseFailureAssessor} that treats all
 * 4XX and 5XX status codes as failures.
 */
public class DefaultResponseFailureAssessor implements ResponseFailureAssessor {

    public boolean isFailure(HttpResponse resp) {
        int status = resp.getStatusLine().getStatusCode();
        return (status >= 400 && status <= 499) || 
                (status >= 500 && status <= 599);
    }

}
