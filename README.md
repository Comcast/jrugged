# jrugged [![Build Status](https://travis-ci.org/Comcast/jrugged.png)](https://travis-ci.org/Comcast/jrugged)

A Java libary of robustness design patterns

## About JRugged
The JRugged library implements some common patterns needed for building robust, production-ready server code in Java. It provides straightforward add-ons to existing code to make it more tolerant of failures and easier to manage. In other words, it makes your Java code more _rugged_!

Getting started is easy, just check out our [Examples](https://github.com/Comcast/jrugged/wiki/Examples) page.  If you would just like to browse the wiki you can start on our [Main Page](https://github.com/Comcast/jrugged/wiki).

## News
- *3 March 2013*: Version 3.1.0 of the JRugged Library released!
    - Modified performance monitor to return 0's instead of EmptySet Exception
    - Removed a terrible linkage to log4j
    - Changed the way statuses were reported to add the service currently reporting a given status
    - Minor bug fixes from the 3.0.3 version

- *14 March 2012*: Version 3.0.3 of the JRugged Library released!
    - Fixed issue #55: Use `InitalizingBean` extension rather than @Autowired
    - Fixed issue #54: Non-threadsafe usage of `LatencyTracker` in `PerformanceMonitor`
    - Fixed issue #53: Copyright year update
    - Fixed issue #50: Tread Safety in `SampledQuantile`
    - Issue #49: Updated Java doc information to clarify usage of `Initializer`
    - Issue #48: Autodiscover `@CircuitBreaker` Annotations

- *19 September 2011*: Version 3.0.0 of the JRugged Library released!
    - Fixed issue #35: Cleaned up and added Java Doc where it was missing
    - Fixed issue #37: Fixed an NPE in `PerformanceMonitor`
    - Fixed issue #38: NPE from `DefaultFailureInterpreter` (synchronization issue)
    - Enhancement issue #40: Allows a `CircuitBreaker` to start/initialize in a hard trip state.
    - Enhancement issue #41: Provide an exception mapper patter for the `ConstantFlowRegulator`
    - Fixed issue #42: Compilation error due to log4j dependency
    - Enhancement issue #43: The annotations available in the aspects package can now make use of the beans for `PerformanceMonitor`s and `CircuitBreaker`s allowing for a much cleaner implementation.  The annotations are now much more useful and robust.
    - Enhancement issue #44: `RolledUpStatus` was improved to allow a more concise status reporting.  The naming of the classes associated with the status have changed as well - to improve the readability and understandability of the code base.
    - Enhancement issue #45: Changed the pom's to include a more recent version of Spring (3.0.5)
    - Enhancement issue #46: Added a way in the spring integration to automatically 'discover' those classes with the annotations included in the aspects package to auto-publish their JMX information if there is a JMX server already registered in the spring config.

    Many thanks to walter\_eggert at comcast dot com, coby\_young at comcast dot com, raghushankar\_ramalingam at comcast dot com and michajlo\_matijkiw at comcast dot com (Mishu) who contributed to this next great release of the JRugged Library.

## Latest Stable Release

- [jrugged-core-3.1.0.jar](http://jrugged.s3.amazonaws.com/downloads/jrugged-core-3.1.0.jar)
- [jrugged-aspects-3.1.0.jar](http://jrugged.s3.amazonaws.com/downloads/jrugged-aspects-3.1.0.jar)
- [jrugged-spring-3.1.0.jar](http://jrugged.s3.amazonaws.com/downloads/jrugged-spring-3.1.0.jar)
- [jrugged-httpclient-3.1.0.jar](http://jrugged.s3.amazonaws.com/downloads/jrugged-httpclient-3.1.0.jar)

## Latest Java Doc
- [Core 3.1.0 Java Doc](http://jrugged.s3.amazonaws.com/jrugged-core-3.1.0/index.html)
- [Aspects 3.1.0 Java Doc](http://jrugged.s3.amazonaws.com/jrugged-aspects-3.1.0/index.html)
- [Spring 3.1.0 Java Doc](http://jrugged.s3.amazonaws.com/jrugged-spring-3.1.0/index.html)
- [HTTP Client 3.1.0 Java Doc](http://jrugged.s3.amazonaws.com/jrugged-httpclient-3.1.0/index.html)
