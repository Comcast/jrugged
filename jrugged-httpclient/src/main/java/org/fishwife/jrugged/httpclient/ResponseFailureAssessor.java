package org.fishwife.jrugged.httpclient;

import org.apache.http.HttpResponse;

/**
 * A ResponseFailureAssessor is used by the {@link FailureExposingHttpClient} to
 * determine whether a given response should be considered a "failure" or not.
 */
public interface ResponseFailureAssessor {
	/**
	 * Returns <code>true</code> if the given response should be treated as a
	 * failure. <b>N.B.:</b> implementors should <em>not</em> consume the response
	 * body unless they check that the underlying {@link org.apache.http.HttpEntity}
	 * is repeatable first.
	 * 
	 * @param response Is this HttpResponse a failure or not
	 * @return boolean indication of failure
	 */
	boolean isFailure(HttpResponse response);
}
