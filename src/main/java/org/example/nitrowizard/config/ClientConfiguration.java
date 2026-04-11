package org.example.nitrowizard.config;

import org.example.nitrowizard.client.WizardWorldClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.example.nitrowizard.config.SynonymConfig;

import java.net.http.HttpClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ApiConfig.class, SynonymConfig.class})
public class ClientConfiguration {
    @Bean
    public JdkClientHttpRequestFactory requestFactory(ApiConfig apiConfig) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(apiConfig.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(apiConfig.getRequestTimeout());
        return factory;
    }

    @Bean
    public RestClient restClient(ApiConfig apiConfig, JdkClientHttpRequestFactory requestFactory) {
        return RestClient.builder()
                .baseUrl(apiConfig.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public WizardWorldClient wizardWorldClient(RestClient restClient, ApiConfig apiConfig) {
        return new WizardWorldClient(restClient, apiConfig);
    }

}
