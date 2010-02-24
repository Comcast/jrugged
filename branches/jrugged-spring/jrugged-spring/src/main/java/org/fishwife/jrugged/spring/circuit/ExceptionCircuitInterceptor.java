/* CircuitBreaker.java
 *
 * Copyright 2009 Comcast Interactive Media, LLC.
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
package org.fishwife.jrugged.spring.circuit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInvocation;
import org.fishwife.jrugged.ExceptionFailureInterpreter;
import org.fishwife.jrugged.circuit.CircuitBreaker;
import org.fishwife.jrugged.circuit.CircuitBreakerException;
import org.fishwife.jrugged.FailureInterpreter;
import org.fishwife.jrugged.spring.BaseJruggedInterceptor;
import org.fishwife.jrugged.spring.circuit.CircuitBreakerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExceptionCircuitInterceptor extends BaseJruggedInterceptor {

    /**
     * Used to grab properties.
     */
    private static final String CONFIG_KEY_PREFIX = "circuit.";

    // configuration keys
    private static final String FREQUENCY_KEY = "frequency";
    private static final String PERIOD_KEY = "period";
    private static final String RESET_KEY = "reset";

    /**
     * Maps names to CircuitBreakers.
     */
    private final Map<String, CircuitBreaker> circuits = new HashMap<String, CircuitBreaker>();

    /**
     * Built from provided properties.
     */
    private final Map<String, CircuitConfig> circuitConfigs = new HashMap<String, CircuitConfig>();
    private final Map<String, Boolean> initializedCircuits = new HashMap<String, Boolean>();

    @SuppressWarnings("unchecked")
    private final Class<? extends Exception>[] defaultTrigger = new Class[]{Exception.class};

    private List<Class<? extends Exception>> ignore = null;
    private List<Class<? extends Exception>> triggers = Arrays.asList(defaultTrigger);

    private Map<String, String> methodMap = new HashMap<String, String>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Properties props;

    /**
     * Allows user-provided class to map a {@link CircuitBreakerException} to
     * an application-specific exception class.
     */
    private CircuitBreakerExceptionMapper<? extends Exception> circuitBreakerExceptionMapper;

    public CircuitBreakerExceptionMapper<? extends Exception> getCircuitBreakerExceptionMapper() {
        return circuitBreakerExceptionMapper;
    }

    public void setCircuitBreakerExceptionMapper(CircuitBreakerExceptionMapper<? extends Exception> circuitBreakerExceptionMapper) {
        this.circuitBreakerExceptionMapper = circuitBreakerExceptionMapper;
    }

    public ExceptionCircuitInterceptor() {}

    public Object invoke(final MethodInvocation invocation) throws Throwable {
        String methodName = (getInvocationTraceName(invocation).split("\\."))[1];
        String name = methodMap.get(methodName);

        CircuitBreaker circuit = null;

        // locks on circuit map to ensure that I don't create 2 copies of a
        // CircuitBreaker if 2 simultanous threads with the same circuit
        // name hit this block for the 1st time
        synchronized (this.circuits) {
            if (circuits.containsKey(name)) {
                circuit = circuits.get(name);
            }

            // sets kind for circuit if it hasn't been initialized
            if (!this.initializedCircuits.containsKey(name)) {
                final FailureInterpreter interpreter = circuit.getFailureInterpreter();

                if(interpreter instanceof ExceptionFailureInterpreter) {
                    //logger.debug("setting kind for circuit '{}' to {}", name,
                            //kind.getName());
                    Set<Class<? extends Exception>> kindSet = new HashSet<Class<? extends Exception>>(this.triggers);
                    ((ExceptionFailureInterpreter) interpreter).setKind(kindSet);

                    Set<Class<? extends Exception>> ignoreSet = new HashSet<Class<? extends Exception>>(this.ignore);
                    ((ExceptionFailureInterpreter) interpreter).setIgnore(ignoreSet);
                }
                this.initializedCircuits.put(name, Boolean.TRUE);
            }
        }


        //this.logger.debug("circuit:{}, status:{}", name, circuit.getStatus());

        try {
            return circuit.invoke(new Callable<Object>() {
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
        } catch (CircuitBreakerException e) {
            if (circuitBreakerExceptionMapper != null) {
                throw circuitBreakerExceptionMapper.map(invocation, e);
            } else {
                throw e;
            }
        }
    }

    public Set<String> getCircuitBreakerNames() {
        return this.circuits.keySet();
    }

    /**
     * @return <code>true</code> if named CircuitBreaker exists,
     *  <code>false</code> otherwise.
     *
     * @param name name of the circuit to ask if a break exists
     */
    public boolean hasCircuitBreaker(String name) {
        return this.circuits.containsKey(name);
    }

    public CircuitBreaker getCircuitBreaker(String name) {
        if (!this.circuits.containsKey(name))
            throw new IllegalArgumentException(String.format(
                    "circuit '%s' not found", name));
        return this.circuits.get(name);
    }

    public void setProperties(Properties props) {
        this.props = props;
    }

    private void loadCircuitConfig() {
        for (final Map.Entry<Object, Object> entry : this.props.entrySet()) {
            try {
                final String key = (String) entry.getKey();
                final String value = (String) entry.getValue();

                if (!key.startsWith(CONFIG_KEY_PREFIX)) {
                    continue;
                }

                final String[] parts = key.substring(CONFIG_KEY_PREFIX.length()).split("\\.", 2);

                if (parts.length != 2)
                    continue;

                final String circuitName = parts[0];
                CircuitConfig config;

                if (this.circuitConfigs.containsKey(circuitName)) {
                    config = this.circuitConfigs.get(circuitName);
                } else {
                    config = new CircuitConfig();
                    this.circuitConfigs.put(circuitName, config);
                }

                final String propName = parts[1];

                if (propName.equals(FREQUENCY_KEY))
                    config.frequency = Integer.parseInt(value);
                else if (propName.equals(PERIOD_KEY))
                    config.period = Integer.parseInt(value);
                else if (propName.equals(RESET_KEY))
                    config.reset = Integer.parseInt(value);
                else
                    this.logger.warn("unrecognized property: "
                            + key);
            } catch (Exception e) {
                this.logger.warn("error parsing config", e);
            }
        }

        for (final Map.Entry<String, CircuitConfig> e : this.circuitConfigs.entrySet()) {
            final String circuitName = e.getKey();
            final CircuitConfig config = e.getValue();

            // if these 3 values were set, we can inject a
            // FailureInterpreter
            if (config.frequency > 0 && config.period > 0 && config.reset > 0) {
                final CircuitBreaker circuit = new CircuitBreaker();

                // defaults to Exception for kind;
                // I make it more specific when we process the annotation
                circuit.setFailureInterpreter(new ExceptionFailureInterpreter(
                        Exception.class, config.frequency, config.period,
                        TimeUnit.MILLISECONDS));

                // only sets values if > 0,
                // since default values in annotation are -1
                if (config.reset > 0)
                    circuit.setResetMillis(config.reset);

                this.circuits.put(circuitName, circuit);
                this.logger.info(
                        "circuit '{}' -> frequency:{}, period:{}, reset:{}",
                        new Object[] {circuitName, config.frequency, config.period, config.reset});
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (this.props == null)
            throw new IllegalArgumentException("[properties] must be provided.");
        this.loadCircuitConfig();
    }

    public void setIgnore(List<Class<? extends Exception>> ignoreMe)  {
        this.ignore = ignoreMe;
    }

    public void setTriggers(List<Class<? extends Exception>> triggerMe)  {
        this.triggers = triggerMe;
    }

    public void setMethods(Map<String, String> methodMap)  {
        this.methodMap = methodMap;
    }

    private static final class CircuitConfig {
        long reset = -1, period = -1;
        int frequency = -1;
    }
}
