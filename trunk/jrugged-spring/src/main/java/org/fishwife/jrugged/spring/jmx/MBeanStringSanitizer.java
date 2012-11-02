/* MBeanStringSanitizer.java
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
package org.fishwife.jrugged.spring.jmx;

import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * The MBeanStringSanitizer is used to turn MBean object, attribute, and operation names and values
 * into web-friendly Strings.
 */
public class MBeanStringSanitizer {

    /**
     * Convert a URL Encoded name back to the original form.
     * @param name the name to URL urlDecode.
     * @param encoding the string encoding to be used (i.e. UTF-8)
     * @return the name in original form.
     * @throws UnsupportedEncodingException if the encoding is not supported.
     */
    String urlDecode(String name, String encoding) throws UnsupportedEncodingException {
        return URLDecoder.decode(name, encoding);
    }

    /**
     * Escape a value to be HTML friendly.
     * @param value the Object value.
     * @return the HTML-escaped String, or <null> if the value is null.
     */
    String escapeValue(Object value) {
        return HtmlUtils.htmlEscape(value != null ? value.toString() : "<null>");
    }
}
