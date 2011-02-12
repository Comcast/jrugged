package org.fishwife.jrugged.spring.config;

import java.util.List;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.fishwife.jrugged.ServiceWrapper;

/**
 * Rework of the ServiceWrapperInterceptor that uses a list of methods instead
 * of a map and only supports one ServiceWrapper. 
 */
public class SingleServiceWrapperInterceptor implements MethodInterceptor {
    
    private List<String> methodList;
    private ServiceWrapper serviceWrapper;

    /** See if the given method invocation is one that needs to be
     * called through a {@link ServiceWrapper}, and if so, do so.
     * @param invocation the {@link MethodInvocation} in question
     * @return whatever the underlying method call would normally
     * return
     * @throws Throwable that the method call would generate, or
     *   that the {@link ServiceWrapper} would generate when tripped.
     */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();

        if (!shouldWrapMethodCall(methodName)) {
            return invocation.proceed();
        }
        else {
            ServiceWrapper wrapper = serviceWrapper;

            return wrapper.invoke(new Callable<Object>() {
                    public Object call() throws Exception {
                        try {
                            return invocation.proceed();
                        } catch (Throwable e) {
                            if (e instanceof Exception)
                                throw (Exception) e;
                            else if (e instanceof Error)
                                throw (Error) e;
                            else
                                throw new RuntimeException(e);
                        }
                    }
                });
        }
    }

    private boolean shouldWrapMethodCall(String methodName) {
        if (methodList == null) {
            return true; // Wrap all by default
        }

        if (methodList.contains(methodName)) {
            return true; //Wrap a specific method
        }

        // If I get to this point, I should not wrap the call.
        return false;
    }

    /** Specifies which methods will be wrapped with the ServiceWrapper.
     *  @param methodList the methods!
     */
    public void setMethods(List<String> methodList)  {
        this.methodList = methodList;
    }
    
    public ServiceWrapper getServiceWrapper() {
        return serviceWrapper;
    }

    public void setServiceWrapper(ServiceWrapper serviceWrapper) {
        this.serviceWrapper = serviceWrapper;
    }
}
