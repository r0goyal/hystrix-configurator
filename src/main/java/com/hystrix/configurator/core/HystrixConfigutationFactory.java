package com.hystrix.configurator.core;

import com.hystrix.configurator.config.*;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.*;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author phaneesh
 */
public class HystrixConfigutationFactory {

    private final HystrixConfig config;

    private HystrixDefaultConfig defaultConfig;

    private Map<String, HystrixCommandConfig> commandConfigMap;

    private Map<String, HystrixCommand.Setter> hystrixConfigCache;

    private static HystrixConfigutationFactory factory;

    private HystrixConfigutationFactory(final HystrixConfig config) {
        this.config = config;
    }

    public static void init(final HystrixConfig config) {
        if(factory == null) {
            factory = new HystrixConfigutationFactory(config);
        }
        factory.setup();
    }

    private void setup() {
        if(config != null) {
            defaultConfig = config.getDefaultConfig();
            if(defaultConfig == null) {
                defaultConfig = new HystrixDefaultConfig();
            }
            if(defaultConfig.getThreadPool() == null)
                defaultConfig.setThreadPool(new ThreadPoolConfig());
            if(defaultConfig.getCircuitBreaker() == null)
                defaultConfig.setCircuitBreaker(new CircuitBreakerConfig());
            if(defaultConfig.getMetrics() == null)
                defaultConfig.setMetrics(new MetricsConfig());
            commandConfigMap = config.getCommands().stream().collect(Collectors.toMap(HystrixCommandConfig::getName, (c) -> c));
            commandConfigMap.forEach( (k,v) -> {
                if(v.getCircuitBreaker() == null)
                    v.setCircuitBreaker(defaultConfig.getCircuitBreaker());
                if(v.getThreadPool() == null)
                    v.setThreadPool(defaultConfig.getThreadPool());
                if(v.getMetrics() == null) {
                    v.setMetrics(defaultConfig.getMetrics());
                }
            });
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.coreSize", defaultConfig.getThreadPool().getConcurrency());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.maxQueueSize", defaultConfig.getThreadPool().getMaxRequestQueueSize());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.queueSizeRejectionThreshold", defaultConfig.getThreadPool().getDynamicRequestQueueSize());

            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.execution.isolation.strategy", defaultConfig.getThreadPool().isSemaphoreIsolation() ? "SEMAPHORE" : "THREAD");
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.execution.thread.timeoutInMilliseconds", defaultConfig.getThreadPool().getTimeout());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.execution.timeout.enabled", true);
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.execution.isolation.thread.interruptOnTimeout", true);
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.execution.isolation.semaphore.macConcurrentRequests", defaultConfig.getThreadPool().getConcurrency());

            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.circuitBreaker.enabled", true);
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.circuitBreaker.requestVolumeThreshold", defaultConfig.getCircuitBreaker().getAcceptableFailuresInWindow());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.circuitBreaker.errorThresholdPercentage", defaultConfig.getCircuitBreaker().getErrorThreshold());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds", defaultConfig.getCircuitBreaker().getWaitTimeBeforeRetry());

            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingStats.timeInMilliseconds", defaultConfig.getMetrics().getStatsTimeInMillis());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingStats.numBuckets", defaultConfig.getMetrics().getNumBucketSize());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingPercentile.enabled", true);
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingPercentile.timeInMilliseconds", defaultConfig.getMetrics().getPercentileTimeInMillis());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingPercentile.numBuckets", defaultConfig.getMetrics().getNumBucketSize());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.rollingPercentile.bucketSize", defaultConfig.getMetrics().getPercentileBucketSize());
            ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", defaultConfig.getMetrics().getHealthCheckInterval());

            commandConfigMap.values().stream().forEach( c -> {
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.coreSize", c.getName()), c.getThreadPool().getConcurrency());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.maxQueueSize", c.getName()), c.getThreadPool().getMaxRequestQueueSize());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.queueSizeRejectionThreshold", c.getName()), c.getThreadPool().getDynamicRequestQueueSize());

                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.execution.isolation.strategy", c.getName()), c.getThreadPool().isSemaphoreIsolation() ? "SEMAPHORE" : "THREAD");
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.execution.thread.timeoutInMilliseconds", c.getName()), c.getThreadPool().getTimeout());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.execution.timeout.enabled", c.getName()), true);
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.execution.isolation.thread.interruptOnTimeout", c.getName()), true);
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.execution.isolation.semaphore.macConcurrentRequests", c.getName()), c.getThreadPool().getConcurrency());

                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.circuitBreaker.enabled", c.getName()), true);
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.circuitBreaker.requestVolumeThreshold", c.getName()), c.getCircuitBreaker().getAcceptableFailuresInWindow());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.circuitBreaker.errorThresholdPercentage", c.getName()), c.getCircuitBreaker().getErrorThreshold());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.circuitBreaker.sleepWindowInMilliseconds", c.getName()), c.getCircuitBreaker().getWaitTimeBeforeRetry());

                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.metrics.rollingStats.timeInMilliseconds", c.getName()), c.getMetrics().getStatsTimeInMillis());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.metrics.rollingStats.numBuckets", c.getName()), c.getMetrics().getNumBucketSize());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.enabled", c.getName()), true);
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.timeInMilliseconds", c.getName()), c.getMetrics().getPercentileTimeInMillis());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.numBuckets", c.getName()), c.getMetrics().getNumBucketSize());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.metrics.rollingPercentile.bucketSize", c.getName()), c.getMetrics().getPercentileBucketSize());
                ConfigurationManager.getConfigInstance().setProperty(String.format("hystrix.command.%s.metrics.healthSnapshot.intervalInMilliseconds", c.getName()), c.getMetrics().getHealthCheckInterval());
            });

            hystrixConfigCache = commandConfigMap.values().stream().collect(Collectors.toMap(HystrixCommandConfig::getName, (c) ->
                    HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(c.getName()))
                            .andCommandKey(HystrixCommandKey.Factory.asKey(c.getName()))
                            .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(c.getName()))
                            .andCommandPropertiesDefaults(
                                    HystrixCommandProperties.Setter()
                                            .withExecutionIsolationStrategy( c.getThreadPool().isSemaphoreIsolation() ? HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE: HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                            .withExecutionIsolationSemaphoreMaxConcurrentRequests(c.getThreadPool().getConcurrency())
                                            .withFallbackIsolationSemaphoreMaxConcurrentRequests(c.getThreadPool().getConcurrency())
                                            .withFallbackEnabled(c.isFallbackEnabled())
                                            .withCircuitBreakerErrorThresholdPercentage(c.getCircuitBreaker().getErrorThreshold())
                                            .withCircuitBreakerRequestVolumeThreshold(c.getCircuitBreaker().getAcceptableFailuresInWindow())
                                            .withCircuitBreakerSleepWindowInMilliseconds(c.getCircuitBreaker().getWaitTimeBeforeRetry())
                                            .withExecutionTimeoutInMilliseconds(c.getThreadPool().getTimeout())
                                            .withMetricsHealthSnapshotIntervalInMilliseconds(c.getMetrics().getHealthCheckInterval())
                                            .withMetricsRollingPercentileBucketSize(c.getMetrics().getPercentileBucketSize())
                                            .withMetricsRollingPercentileWindowInMilliseconds(c.getMetrics().getPercentileTimeInMillis())
                            )
                            .andThreadPoolPropertiesDefaults(
                                    HystrixThreadPoolProperties.Setter()
                                            .withCoreSize(c.getThreadPool().getConcurrency())
                                            .withMaxQueueSize(c.getThreadPool().getMaxRequestQueueSize())
                                            .withQueueSizeRejectionThreshold(c.getThreadPool().getDynamicRequestQueueSize())
                                            .withMetricsRollingStatisticalWindowBuckets(c.getMetrics().getNumBucketSize())
                                            .withMetricsRollingStatisticalWindowInMilliseconds(c.getMetrics().getStatsTimeInMillis())
                            )
                ));
        }
    }

    public static HystrixCommand.Setter getCommandConfiguration(final String key) {
        if(factory == null) throw new IllegalStateException("Factory not initialized");
        if(!factory.hystrixConfigCache.containsKey(key)) throw new InvalidParameterException("Invalid command key");
        return factory.hystrixConfigCache.get(key);
    }
}
