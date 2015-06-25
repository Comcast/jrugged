/* Copyright 2009-2015 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

public class AnnotatedMethodScanner {

    private final ClassLoader classLoader;
    private final ClassPathScanningCandidateComponentProvider provider;

    public AnnotatedMethodScanner() {
        classLoader = AnnotatedMethodScanner.class.getClassLoader();
        provider = new ClassPathScanningCandidateComponentProvider(false);
    }

    // package private for testing only
    AnnotatedMethodScanner(ClassLoader classLoader, ClassPathScanningCandidateComponentProvider provider) {
        this.classLoader = classLoader;
        this.provider = provider;
    }

    /**
     * Find all methods on classes under scanBase that are annotated with annotationClass.
     *
     * @param scanBase Package to scan recursively, in dot notation (ie: org.jrugged...)
     * @param annotationClass Class of the annotation to search for
     * @return Set&lt;Method&gt; The set of all @{java.lang.reflect.Method}s having the annotation
     */
    public Set<Method> findAnnotatedMethods(String scanBase, Class<? extends Annotation> annotationClass) {
        Set<BeanDefinition> filteredComponents = findCandidateBeans(scanBase, annotationClass);
        return extractAnnotatedMethods(filteredComponents, annotationClass);
    }

    Set<Method> extractAnnotatedMethods(
            Set<BeanDefinition> filteredComponents,
            Class<? extends Annotation> annoClass) {
        Set<Method> annotatedMethods = new HashSet<Method>();
        for (BeanDefinition bd : filteredComponents) {
            try {
                String className = bd.getBeanClassName();
                Class<?> beanClass = classLoader.loadClass(className);
                for (Method m : beanClass.getMethods()) {
                    if (m.getAnnotation(annoClass) != null) {
                        annotatedMethods.add(m);
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                // no-op
            }
        }

        return annotatedMethods;
    }

    synchronized Set<BeanDefinition> findCandidateBeans(String scanBase,
            Class<? extends Annotation> annotatedClass) {
        provider.resetFilters(false);
        provider.addIncludeFilter(new AnnotatedMethodFilter(annotatedClass));

        String basePackage = scanBase.replace('.', '/');
        return provider.findCandidateComponents(basePackage);
    }
}
