package com.example.practice.spring6resttemplate.config;

import com.example.practice.spring6resttemplate.config.interceptor.OAuthClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateBuilderConfig {

    @Value("${rest.template.rootUrl}")
    String rootUrl;

    /* Spring Http Basic Connection
        @Value("${rest.template.username}")
        String username;
        @Value("${rest.template.password}")
        String password;
    */


    @Bean
    public OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientService oAuth2AuthorizedClientService
    ) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            oAuth2AuthorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }


    @Bean
    public RestTemplateBuilder restTemplateBuilder(
        RestTemplateBuilderConfigurer configurer,
        OAuthClientInterceptor interceptor
    ) {
        assert rootUrl != null;

        /* Spring Http Basic Connection
            return configurer.configure(new RestTemplateBuilder())
                .basicAuthentication(username, password)
                .uriTemplateHandler(new DefaultUriBuilderFactory(rootUrl));
        */

        return configurer.configure(new RestTemplateBuilder())
            .additionalInterceptors(interceptor)
            .uriTemplateHandler(new DefaultUriBuilderFactory(rootUrl));
    }
}
