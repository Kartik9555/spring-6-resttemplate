package com.example.practice.spring6resttemplate.client.impl;

import com.example.practice.spring6resttemplate.client.BeerClient;
import com.example.practice.spring6resttemplate.model.BeerDTO;
import com.example.practice.spring6resttemplate.model.BeerDTOPageImpl;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class BeerClientImpl implements BeerClient {
    public static final String GET_BEER_PATH = "/api/v1/beer";
    public static final String GET_BEER_BY_ID_PATH = "/api/v1/beer/{beerId}";

    private final RestTemplateBuilder restTemplateBuilder;


    @Override
    public Page<BeerDTO> findAllBeers() {
        return findAllBeers(null,null, null, null, null);
    }

    @Override
    public Page<BeerDTO> findAllBeers(
        String beerName,
        String beerStyle,
        Boolean showInventory,
        Integer pageNumber,
        Integer pageSize
    ) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(GET_BEER_PATH);
        if(StringUtils.hasLength(beerName)){
            uriComponentsBuilder.queryParam("name", beerName);
        }
        if(StringUtils.hasLength(beerStyle)){
            uriComponentsBuilder.queryParam("beerStyle", beerStyle);
        }
        if(showInventory != null){
            uriComponentsBuilder.queryParam("showInventory", showInventory);
        }
        if(pageNumber != null){
            uriComponentsBuilder.queryParam("pageNumber", pageNumber);
        }
        if(pageSize != null){
            uriComponentsBuilder.queryParam("pageSize", pageSize);
        }
        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<BeerDTOPageImpl> response = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), BeerDTOPageImpl.class);
        return response.getBody();
    }


    @Override
    public BeerDTO findBeerById(UUID beerId) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate.getForObject(GET_BEER_BY_ID_PATH, BeerDTO.class, beerId);
    }


    @Override
    public BeerDTO addBeer(BeerDTO beer) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = restTemplate.postForLocation(GET_BEER_PATH, beer);
        assert uri != null;
        return restTemplate.getForObject(uri.getPath(), BeerDTO.class);
    }


    @Override
    public BeerDTO updateBeer(BeerDTO beer) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.put(GET_BEER_BY_ID_PATH, beer, beer.getId());
        return findBeerById(beer.getId());
    }


    @Override
    public void deleteById(UUID beerId) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.delete(GET_BEER_BY_ID_PATH, beerId);
    }
}
