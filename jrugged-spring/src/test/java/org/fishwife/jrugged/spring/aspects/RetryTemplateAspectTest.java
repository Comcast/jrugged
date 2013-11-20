package org.fishwife.jrugged.spring.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryException;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class RetryTemplateAspectTest {

    private static final String TEST_RETRY_TEMPLATE = "TestRetryTemplate";
    private static final String TEST_RETRY_TEMPLATE_RECOVERY = "TestRetryTemplateRecovery";

    private RetryTemplateAspect aspect;

    @Mock
    private RetryTemplate mockAnnotation;

    @Mock
    private Signature mockSignature;

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private ProceedingJoinPoint mockPjp;

    @Mock
    private RecoveryCallback recoveryCallback;

    @Before
    public void setUp() {
        aspect = new RetryTemplateAspect();
        aspect.setBeanFactory(beanFactory);
        Mockito.doReturn("Signature").when(mockSignature).getName();
        Mockito.doReturn(TEST_RETRY_TEMPLATE).when(mockAnnotation).name();
        Mockito.doReturn(TEST_RETRY_TEMPLATE_RECOVERY).when(mockAnnotation).recoveryCallbackName();
        Mockito.doReturn("Target").when(mockPjp).getTarget();
        Mockito.doReturn(mockSignature).when(mockPjp).getSignature();
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testRetryWithMissingBean() throws Throwable {
        Mockito.doThrow(new NoSuchBeanDefinitionException("")).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE, org.springframework.retry.support.RetryTemplate.class);
        try {
            aspect.retry(mockPjp, mockAnnotation);
        }
        finally {
            Mockito.verify(mockPjp, Mockito.never()).proceed();
        }
    }

    @Test(expected = BeanNotOfRequiredTypeException.class)
    public void testRetryWithWrongBeanType() throws Throwable {
        Mockito.doThrow(new BeanNotOfRequiredTypeException("", String.class, String.class)).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE, org.springframework.retry.support.RetryTemplate.class);
        try {
            aspect.retry(mockPjp, mockAnnotation);
        }
        finally {
            Mockito.verify(mockPjp, Mockito.never()).proceed();
        }
    }


    @Test
    public void testRetry() throws Throwable {
        org.springframework.retry.support.RetryTemplate template =
                new org.springframework.retry.support.RetryTemplate();
        Mockito.doReturn(template).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE, org.springframework.retry.support.RetryTemplate.class);
        Mockito.doReturn("a").when(mockPjp).proceed();
        Assert.assertEquals("a", aspect.retry(mockPjp, mockAnnotation));
        Mockito.verify(mockPjp, Mockito.times(1)).proceed();
    }

    @Test
    public void testRetryExceptionWithRecovery() throws Throwable {
        Mockito.doReturn(TEST_RETRY_TEMPLATE_RECOVERY).when(mockAnnotation).recoveryCallbackName();

        org.springframework.retry.support.RetryTemplate template =
                new org.springframework.retry.support.RetryTemplate();
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<Class<? extends Throwable>, Boolean>();
        exceptionMap.put(RuntimeException.class, Boolean.TRUE);
        template.setRetryPolicy(new SimpleRetryPolicy(1, exceptionMap));

        Mockito.doReturn(template).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE, org.springframework.retry.support.RetryTemplate.class);
        Mockito.doReturn(recoveryCallback).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE_RECOVERY, RecoveryCallback.class);
        Mockito.doThrow(new RuntimeException()).when(mockPjp).proceed();
        Mockito.doReturn("a").when(recoveryCallback).recover(Mockito.any(RetryContext.class));
        Assert.assertEquals("a", aspect.retry(mockPjp, mockAnnotation));
        Mockito.verify(mockPjp, Mockito.times(1)).proceed();
    }

    @Test(expected=RuntimeException.class)
    public void testRetryExceptionWithoutRecovery() throws Throwable {
        org.springframework.retry.support.RetryTemplate template =
                new org.springframework.retry.support.RetryTemplate();
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<Class<? extends Throwable>, Boolean>();
        exceptionMap.put(RuntimeException.class, Boolean.TRUE);
        template.setRetryPolicy(new SimpleRetryPolicy(1, exceptionMap));

        Mockito.doReturn(template).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE, org.springframework.retry.support.RetryTemplate.class);
        Mockito.doReturn(null).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE_RECOVERY, RecoveryCallback.class);
        Mockito.doThrow(new RuntimeException()).when(mockPjp).proceed();
        try {
            aspect.retry(mockPjp, mockAnnotation);
        }
        finally {
            Mockito.verify(mockPjp, Mockito.times(1)).proceed();
        }
    }

    @Test(expected=OutOfMemoryError.class)
    public void testRetryErrorWithoutRecovery() throws Throwable {
        org.springframework.retry.support.RetryTemplate template =
                new org.springframework.retry.support.RetryTemplate();
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<Class<? extends Throwable>, Boolean>();
        exceptionMap.put(RuntimeException.class, Boolean.TRUE);
        template.setRetryPolicy(new SimpleRetryPolicy(1, exceptionMap));

        Mockito.doReturn(template).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE, org.springframework.retry.support.RetryTemplate.class);
        Mockito.doReturn(null).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE_RECOVERY, RecoveryCallback.class);
        Mockito.doThrow(new OutOfMemoryError()).when(mockPjp).proceed();
        try {
            aspect.retry(mockPjp, mockAnnotation);
        }
        finally {
            Mockito.verify(mockPjp, Mockito.times(1)).proceed();
        }
    }

    @Test(expected=RuntimeException.class)
    public void testRetryThrowableWithoutRecovery() throws Throwable {
        org.springframework.retry.support.RetryTemplate template =
                new org.springframework.retry.support.RetryTemplate();
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<Class<? extends Throwable>, Boolean>();
        exceptionMap.put(RuntimeException.class, Boolean.TRUE);
        template.setRetryPolicy(new SimpleRetryPolicy(1, exceptionMap));

        Mockito.doReturn(template).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE, org.springframework.retry.support.RetryTemplate.class);
        Mockito.doReturn(null).when(beanFactory)
                .getBean(TEST_RETRY_TEMPLATE_RECOVERY, RecoveryCallback.class);
        Mockito.doThrow(new Throwable("")).when(mockPjp).proceed();
        try {
            aspect.retry(mockPjp, mockAnnotation);
        }
        finally {
            Mockito.verify(mockPjp, Mockito.times(1)).proceed();
        }
    }

}
