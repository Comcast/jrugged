package org.fishwife.jrugged.spring.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;

import com.google.common.base.Strings;

@Aspect
public class RetryTemplateAspect implements BeanFactoryAware {

	private static final Logger logger = LoggerFactory.getLogger(RetryTemplateAspect.class);

	private BeanFactory beanFactory;

	/** Default constructor. */
	public RetryTemplateAspect() {
	}

	@Autowired
	@Required
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Runs a method call through the spring managed
	 * {@link org.springframework.retry.support.RetryTemplate} instance indicated by
	 * the annotations "name" attribute.
	 *
	 * @param pjp                     a {@link org.aspectj.lang.ProceedingJoinPoint}
	 *                                representing an annotated method call.
	 * @param retryTemplateAnnotation the
	 *                                {@link org.fishwife.jrugged.spring.aspects.RetryTemplate}
	 *                                annotation that wrapped the method.
	 * @throws Throwable if the method invocation itself or the wrapping
	 *                   {@link org.springframework.retry.support.RetryTemplate}
	 *                   throws one during execution.
	 * @return The return value from the method call.
	 */
	@Around("@annotation(retryTemplateAnnotation)")
	public Object retry(final ProceedingJoinPoint pjp, final RetryTemplate retryTemplateAnnotation) throws Throwable {
		final String name = retryTemplateAnnotation.name();
		final String recoveryCallbackName = retryTemplateAnnotation.recoveryCallbackName();

		org.springframework.retry.support.RetryTemplate retryTemplate = beanFactory.getBean(name,
				org.springframework.retry.support.RetryTemplate.class);

		RecoveryCallback<Object> recoveryCallback = null;
		if (!Strings.isNullOrEmpty(recoveryCallbackName)) {
			recoveryCallback = beanFactory.getBean(recoveryCallbackName,
					org.springframework.retry.RecoveryCallback.class);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(
					"Have @RetryTemplate method with retryTemplate name {} and callback name {}, "
							+ "wrapping call on method {} of target object {}",
					new Object[] { name, recoveryCallbackName, pjp.getSignature().getName(), pjp.getTarget() });
		}

		return retryTemplate.execute(new RetryCallback<Object, Exception>() {
			public Object doWithRetry(RetryContext context) throws Exception {
				try {
					return pjp.proceed();
				} catch (Error e) {
					throw e;
				} catch (Exception e) {
					throw e;
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		}, recoveryCallback);
	}
}
