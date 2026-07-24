package dev.madmmas.aimanager.provider;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LlmProviderProperties.class, PlaygroundProperties.class})
public class ProviderConfiguration {}
