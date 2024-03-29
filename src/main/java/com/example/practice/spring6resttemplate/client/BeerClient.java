package com.example.practice.spring6resttemplate.client;

import com.example.practice.spring6resttemplate.model.BeerDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface BeerClient {

    Page<BeerDTO> findAllBeers();
    Page<BeerDTO> findAllBeers(String beerName, String beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize);

    BeerDTO findBeerById(UUID beerId);

    BeerDTO addBeer(BeerDTO beer);

    BeerDTO updateBeer(BeerDTO savedBeer);

    void deleteById(UUID beerId);
}
