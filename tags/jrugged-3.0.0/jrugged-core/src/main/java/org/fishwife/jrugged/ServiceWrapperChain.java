package org.fishwife.jrugged;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class ServiceWrapperChain implements ServiceWrapper {
    
    private List<ServiceWrapper> wrappers;
    
    public ServiceWrapperChain(Collection<ServiceWrapper> wrappers) {
       ArrayList<ServiceWrapper> rev = new ArrayList<ServiceWrapper>();
       for(ServiceWrapper wrapper : wrappers) {
           rev.add(0, wrapper);
       }
       this.wrappers = rev;
    }

    private <T> Callable<T> wrap(final Callable<T> c, final ServiceWrapper wrapper) {
        return new Callable<T>() {
            public T call() throws Exception {
                return wrapper.invoke(c);
            }
        };
    }
    
    private Runnable wrap(final Runnable r, final ServiceWrapper wrapper) {
        return new Runnable() {
            public void run() {
                try {
                    wrapper.invoke(r);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
    
    public <T> T invoke(Callable<T> c) throws Exception {
        for(ServiceWrapper wrapper : wrappers) {
            c = wrap(c, wrapper);
        }
        return c.call();
    }

    public void invoke(Runnable r) throws Exception {
        for(ServiceWrapper wrapper : wrappers) {
            r = wrap(r, wrapper);
        }
        r.run();
    }

    public <T> T invoke(Runnable r, T result) throws Exception {
        invoke(r);
        return result;
    }

}
