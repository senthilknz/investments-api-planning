package com.westpac.xapi.investments.filter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that enables the logging filter properties.
 */
@Configuration
@EnableConfigurationProperties(LoggingFilterProperties.class)
public class LoggingFilterConfig {
}
