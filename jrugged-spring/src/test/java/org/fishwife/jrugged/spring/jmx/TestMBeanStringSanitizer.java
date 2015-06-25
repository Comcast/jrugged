/* TestMBeanStringSanitizer.java
 *
 * Copyright 2009-2015 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.spring.jmx;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static junit.framework.Assert.assertEquals;

public class TestMBeanStringSanitizer {

    private MBeanStringSanitizer sanitizer;

    @Before
    public void setUp() {
        sanitizer = new MBeanStringSanitizer();
    }

    @Test
    public void testUrlDecodeDecodesSlashes() throws Exception {
        String testString = "this%2Fhas%2Fslashes";
        String sanitizedString = sanitizer.urlDecode(testString, "UTF-8");
        assertEquals("this/has/slashes", sanitizedString);
    }

    @Test(expected= UnsupportedEncodingException.class)
    public void testUrlDecodeThrowsUnsupportedEncodingException() throws Exception {
        sanitizer.urlDecode("some_string_with_encoding_%2F", "unsupported_encoding");
    }

    @Test
    public void testEscapeValueEscapesValues() {
        String testString = "this<contains>evil&characters";
        String escapedString = sanitizer.escapeValue(testString);
        assertEquals("this&lt;contains&gt;evil&amp;characters", escapedString);
    }

    @Test
    public void testEscapeValueEscapesNulls() {
        assertEquals("&lt;null&gt;", sanitizer.escapeValue(null));
    }
}
