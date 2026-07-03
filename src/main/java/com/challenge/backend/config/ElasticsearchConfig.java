package com.challenge.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.elasticsearch.support.HttpHeaders;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.challenge.backend.infrastructure.search.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:localhost:9201}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.api-key:}")
    private String apiKey;

    @Override
    public ClientConfiguration clientConfiguration() {
        String hostAndPort = elasticsearchUris.replace("http://", "").replace("https://", "");
        boolean useSsl = elasticsearchUris.startsWith("https://");

        HttpHeaders headers = new HttpHeaders();
        if (apiKey != null && !apiKey.isBlank()) {
            headers.add("Authorization", "ApiKey " + apiKey);
        }

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(hostAndPort);

        ClientConfiguration.TerminalClientConfigurationBuilder terminalBuilder;
        if (useSsl) {
            terminalBuilder = builder.usingSsl()
                    .withDefaultHeaders(headers)
                    .withConnectTimeout(5000)
                    .withSocketTimeout(30000);
        } else {
            terminalBuilder = builder
                    .withDefaultHeaders(headers)
                    .withConnectTimeout(5000)
                    .withSocketTimeout(30000);
        }

        return terminalBuilder.build();
    }
}