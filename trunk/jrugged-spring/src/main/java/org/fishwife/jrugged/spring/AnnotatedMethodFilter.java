/* Copyright 2009-2011 Comcast Interactive Media, LLC.

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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * TypeFilter to find classes based on annotations on
 * {@link java.lang.reflect.Method}s.
 */
public class AnnotatedMethodFilter implements TypeFilter {

    private final Class<? extends Annotation> annotatedClass;
    
    /**
     * Create filter for classes with {@link java.lang.reflect.Method}s
     * annotated with specified annotation.
     *
     * @param annotatedClass The annotated Class
     */
    public AnnotatedMethodFilter(Class<? extends Annotation> annotatedClass) {
        this.annotatedClass = annotatedClass;
    }
    
    public boolean match(MetadataReader metadataReader,
            MetadataReaderFactory metadataReaderFactory) throws IOException {
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        Set<MethodMetadata> annotatedMethods = annotationMetadata
                .getAnnotatedMethods(annotatedClass.getCanonicalName());
        return !annotatedMethods.isEmpty();
    }

}
