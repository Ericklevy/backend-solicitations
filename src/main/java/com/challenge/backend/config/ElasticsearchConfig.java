package com.challenge.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.challenge.backend.infrastructure.search.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:localhost:9201}")
    private String elasticsearchUris;

    @Override
    public ClientConfiguration clientConfiguration() {
        String hostAndPort = elasticsearchUris.replace("http://", "").replace("https://", "");
        return ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .withConnectTimeout(5000)
                .withSocketTimeout(30000)
                .build();
    }
}