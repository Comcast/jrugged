package org.fishwife.jrugged.spring;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

public class TestAnnotatedMethodFilter {
    
    private MetadataReader mockMetadataReader;
    private MetadataReaderFactory mockMetadataReaderFactory;
    private AnnotationMetadata mockAnnotationMetadata;
    private MethodMetadata mockMethodMetadata;
    
    private AnnotatedMethodFilter impl;
    
    @Before
    public void setUp() {
        impl = new AnnotatedMethodFilter(SomeAnno.class);
        mockMetadataReader = createMock(MetadataReader.class);
        mockMetadataReaderFactory = createMock(MetadataReaderFactory.class);
        mockAnnotationMetadata = createMock(AnnotationMetadata.class);
        mockMethodMetadata = createMock(MethodMetadata.class);
        
        expect(mockMetadataReader.getAnnotationMetadata()).andReturn(mockAnnotationMetadata);
    }
    
    @Test
    public void testMatchReturnsFalseIfNoAnnotatedMethodsFound() throws IOException {
        Set<MethodMetadata> foundMethods = new HashSet<MethodMetadata>();
        
        expect(mockAnnotationMetadata.getAnnotatedMethods(SomeAnno.class.getCanonicalName())).andReturn(foundMethods);
        
        replayMocks();
        assertFalse(impl.match(mockMetadataReader, mockMetadataReaderFactory));
        verifyMocks();
    }
    
    @Test
    public void testMatchReturnsFalseIfAnnotatedMethodsFound() throws IOException {
        Set<MethodMetadata> foundMethods = new HashSet<MethodMetadata>();
        foundMethods.add(mockMethodMetadata);
        
        expect(mockAnnotationMetadata.getAnnotatedMethods(SomeAnno.class.getCanonicalName())).andReturn(foundMethods);
     
        replayMocks();
        assertTrue(impl.match(mockMetadataReader, mockMetadataReaderFactory));
        verifyMocks();
    }
    
    void replayMocks() {
        replay(mockMetadataReader);
        replay(mockMetadataReaderFactory);
        replay(mockAnnotationMetadata);
        replay(mockMethodMetadata);
    }
    
    void verifyMocks() {
        verify(mockMetadataReader);
        verify(mockMetadataReaderFactory);
        verify(mockAnnotationMetadata);
        verify(mockMethodMetadata);
    }
    
    // Dummy anno for test
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface SomeAnno {
        
    }
    
}
