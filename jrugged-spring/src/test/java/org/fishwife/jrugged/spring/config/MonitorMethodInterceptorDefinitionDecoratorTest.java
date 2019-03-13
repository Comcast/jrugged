/* MonitorMethodInterceptorDefinitionDecoratorTest.java
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
package org.fishwife.jrugged.spring.config;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MonitorMethodInterceptorDefinitionDecoratorTest {

    private MonitorMethodInterceptorDefinitionDecorator decorator;

    @Before
    public void setUp() {
        decorator = new MonitorMethodInterceptorDefinitionDecorator();
    }

    @Test
    public void testParseMethodListNoName() {

        List<String> methods = decorator.parseMethodList("");

        Assert.assertNotNull(methods);
        Assert.assertTrue(methods.size() == 0);
    }

    @Test
    public void testParseMethodListOneName() {

        List<String> methods = decorator.parseMethodList("foo");

        Assert.assertNotNull(methods);
        Assert.assertTrue(methods.size() == 1);
        Assert.assertTrue(methods.contains("foo"));
    }

    @Test
    public void testParseMethodListTwoNames() {

        List<String> methods = decorator.parseMethodList("foo, bar");

        Assert.assertNotNull(methods);
        Assert.assertTrue(methods.size() == 2);
        Assert.assertTrue(methods.contains("foo"));
        Assert.assertTrue(methods.contains("bar"));
    }

    @Test
    public void testParseMethodListThreeNames() {

        List<String> methods = decorator.parseMethodList("foo, bar, baz");

        Assert.assertNotNull(methods);
        Assert.assertTrue(methods.size() == 3);
        Assert.assertTrue(methods.contains("foo"));
        Assert.assertTrue(methods.contains("bar"));
        Assert.assertTrue(methods.contains("baz"));
    }
}
