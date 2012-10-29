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

/**
 * The MBeanStringSanitizer is used to turn MBean object names and values into
 * web-friendly Strings.
 */
public class MBeanStringSanitizer {

    /**
     * Make an object name web-friendly.
     * @param objectName the object name to sanitize.
     * @return the sanitized name.
     */
    String sanitizeObjectName(String objectName) {
        return objectName.replace("/", "[slash]");
    }

    /**
     * Convert a web-friendly object name back to the original form.
     * @param objectName the object name to desanitize.
     * @return the name in original form.
     */
    String desanitizeObjectName(String objectName) {
        return objectName.replace("[slash]", "/");
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
