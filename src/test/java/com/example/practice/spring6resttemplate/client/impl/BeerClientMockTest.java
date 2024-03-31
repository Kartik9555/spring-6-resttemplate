package com.example.practice.spring6resttemplate.client.impl;

import com.example.practice.spring6resttemplate.client.BeerClient;
import com.example.practice.spring6resttemplate.config.RestTemplateBuilderConfig;
import com.example.practice.spring6resttemplate.config.interceptor.OAuthClientInterceptor;
import com.example.practice.spring6resttemplate.model.BeerDTO;
import com.example.practice.spring6resttemplate.model.BeerDTOPageImpl;
import com.example.practice.spring6resttemplate.model.BeerStyle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.example.practice.spring6resttemplate.client.impl.BeerClientImpl.GET_BEER_BY_ID_PATH;
import static com.example.practice.spring6resttemplate.client.impl.BeerClientImpl.GET_BEER_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080";
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_HEADER_VALUE = "Bearer test";

    BeerClient beerClient;

    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO dto;
    String dtoJson;

    @MockBean
    OAuth2AuthorizedClientManager clientManager;

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @TestConfiguration
    public static class TestConfig{

        @Bean
        ClientRegistrationRepository clientRegistrationRepository(){
            return new InMemoryClientRegistrationRepository(
                ClientRegistration.withRegistrationId("springauth")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("test")
                    .tokenUri("test")
                    .build()
            );
        }

        @Bean
        OAuth2AuthorizedClientService oAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository){
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }

        @Bean
        OAuthClientInterceptor oAuthClientInterceptor(
            OAuth2AuthorizedClientManager manager,
            ClientRegistrationRepository clientRegistrationRepository
        ){
            return new OAuthClientInterceptor(manager, clientRegistrationRepository);
        }
    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("springauth");
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "test", Instant.MIN, Instant.MAX);

        Mockito.when(clientManager.authorize(Mockito.any())).thenReturn(new OAuth2AuthorizedClient(clientRegistration, "test", accessToken));

        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        Mockito.when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);
        dto = getBeerDTO();
        dtoJson = objectMapper.writeValueAsString(dto);
    }


    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());
        server.expect(method(HttpMethod.GET))
            .andExpect(header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE))
            .andExpect(requestTo(URL + GET_BEER_PATH))
            .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> beers = beerClient.findAllBeers();
        assertThat(beers.getContent().size()).isGreaterThan(0);
    }


    @Test
    void testListBeersWithQueryParams() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());
        URI uri = UriComponentsBuilder.fromHttpUrl(URL + GET_BEER_PATH)
            .queryParam("name", "ALE")
            .build().toUri();

        server.expect(method(HttpMethod.GET))
            .andExpect(requestTo(uri))
            .andExpect(header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE))
            .andExpect(queryParam("name", "ALE"))
            .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> beers = beerClient.findAllBeers("ALE", null, null, null, null);
        assertThat(beers.getContent().size()).isEqualTo(1);
    }


    @Test
    void testBeerById() {
        mockGetOperation();

        BeerDTO beer = beerClient.findBeerById(dto.getId());
        assertThat(beer.getId()).isEqualTo(dto.getId());
    }


    @Test
    void testAddBeer() {
        URI uri = UriComponentsBuilder.fromPath(GET_BEER_BY_ID_PATH).build(dto.getId());

        server.expect(method(HttpMethod.POST))
            .andExpect(requestTo(URL + GET_BEER_PATH))
            .andExpect(header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE))
            .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO beer = beerClient.addBeer(dto);
        assertThat(beer.getId()).isEqualTo(dto.getId());
    }


    @Test
    void testUpdateBeer() {
        server.expect(method(HttpMethod.PUT))
            .andExpect(requestToUriTemplate(URL + GET_BEER_BY_ID_PATH, dto.getId()))
            .andExpect(header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE))
            .andRespond(withNoContent());

        mockGetOperation();

        BeerDTO beer = beerClient.updateBeer(dto);
        assertThat(beer.getId()).isEqualTo(dto.getId());
    }


    @Test
    void testDeleteBeer() {
        server.expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(URL + GET_BEER_BY_ID_PATH, dto.getId()))
            .andExpect(header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE))
            .andRespond(withNoContent());

        beerClient.deleteById(dto.getId());

        server.verify();
    }


    @Test
    void testDeleteBeerNotFound() {
        server.expect(method(HttpMethod.DELETE))
            .andExpect(requestToUriTemplate(URL + GET_BEER_BY_ID_PATH, dto.getId()))
            .andExpect(header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE))
            .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> beerClient.deleteById(dto.getId()));

        server.verify();
    }


    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
            .andExpect(requestToUriTemplate(URL + GET_BEER_BY_ID_PATH, dto.getId()))
            .andExpect(header(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE))
            .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }


    BeerDTO getBeerDTO() {
        return BeerDTO.builder()
            .id(UUID.randomUUID())
            .name("Monster Beer")
            .beerStyle(BeerStyle.IPA)
            .upc("1233455666")
            .price(new BigDecimal("12.35"))
            .quantityOnHand(120)
            .build();
    }


    BeerDTOPageImpl getPage() {
        return new BeerDTOPageImpl(Collections.singletonList(getBeerDTO()), 1, 25, 1);
    }
}
