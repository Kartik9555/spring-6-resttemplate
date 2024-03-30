package com.example.practice.spring6resttemplate.client.impl;

import com.example.practice.spring6resttemplate.client.BeerClient;
import com.example.practice.spring6resttemplate.model.BeerDTO;
import com.example.practice.spring6resttemplate.model.BeerStyle;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClient beerClient;

    @Test
    void findAllBeers() {
        beerClient.findAllBeers();
    }

    @Test
    void findAllBeersByName() {
        beerClient.findAllBeers("ALE", null, null, null, null);
    }

    @Test
    void findAllBeersByBeerStyle() {
        beerClient.findAllBeers(null, BeerStyle.ALE.name(), null, null, null);
    }

    @Test
    void getBeerById() {
        BeerDTO beer = beerClient.findAllBeers().getContent().get(0);
        BeerDTO foundBeer = beerClient.findBeerById(beer.getId());
        assertThat(foundBeer).isNotNull();
        assertThat(foundBeer.getId()).isEqualTo(beer.getId());
    }

    @Test
    void addBeer() {
        BeerDTO beer = BeerDTO.builder()
            .name("Monster Beer")
            .beerStyle(BeerStyle.IPA)
            .upc("1233455666")
            .price(new BigDecimal("12.35"))
            .quantityOnHand(120)
            .build();

        BeerDTO savedBeer = beerClient.addBeer(beer);
        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getName()).isEqualTo(beer.getName());
    }


    @Test
    void updateBeer() {
        BeerDTO beer = BeerDTO.builder()
            .name("Monster Beer")
            .beerStyle(BeerStyle.IPA)
            .upc("1233455666")
            .price(new BigDecimal("12.35"))
            .quantityOnHand(120)
            .build();

        BeerDTO savedBeer = beerClient.addBeer(beer);
        final String name = "Monster Beer 1";
        savedBeer.setName(name);
        BeerDTO beerDTO = beerClient.updateBeer(savedBeer);
        assertThat(beerDTO.getName()).isEqualTo(name);
    }

    @Test
    void deleteBeer() {
        BeerDTO beer = BeerDTO.builder()
            .name("Monster Beer")
            .beerStyle(BeerStyle.IPA)
            .upc("1233455666")
            .price(new BigDecimal("12.35"))
            .quantityOnHand(120)
            .build();

        BeerDTO savedBeer = beerClient.addBeer(beer);
        final String name = "Monster Beer 1";
        savedBeer.setName(name);
        beerClient.deleteById(savedBeer.getId());
        assertThrows(HttpClientErrorException.class, () -> beerClient.findBeerById(savedBeer.getId()));
    }
}