package org.fishwife.jrugged.spring;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.eq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

public class TestAnnotatedMethodScanner {

    private ClassLoader mockClassLoader;
    private ClassPathScanningCandidateComponentProvider mockProvider;
    
    private AnnotatedMethodScanner impl;
    
    @Before
    public void setUp() {
        mockClassLoader = createMock(ClassLoader.class);
        mockProvider = createMock(ClassPathScanningCandidateComponentProvider.class);
        impl = new AnnotatedMethodScanner(mockClassLoader, mockProvider);
    }
    
    @Test
    public void testFindCandidateBeansAppliesAnnotatedMethodFilter() {
        String basePackage = "faux.package";
        Set<BeanDefinition> filteredComponents = new HashSet<BeanDefinition>();
        
        mockProvider.resetFilters(false);
        expectLastCall();
        mockProvider.addIncludeFilter(EasyMock.isA(AnnotatedMethodFilter.class));
        expectLastCall();
        
        expect(mockProvider.findCandidateComponents(eq(basePackage.replace('.', '/')))).
                   andReturn(filteredComponents);
        
        replayMocks();
        impl.findCandidateBeans(basePackage, SomeAnno.class);
        verifyMocks();
    }
    
    void replayMocks() {
        replay(mockClassLoader);
        replay(mockProvider);
    }
    
    void verifyMocks() {
        verify(mockClassLoader);
        verify(mockProvider);
    }
    
    // Dummy anno for test
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface SomeAnno {
        
    }
}
