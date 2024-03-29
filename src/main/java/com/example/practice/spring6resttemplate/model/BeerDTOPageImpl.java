package com.example.practice.spring6resttemplate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@JsonIgnoreProperties(ignoreUnknown = true, value = "pageable")
public class BeerDTOPageImpl extends PageImpl<BeerDTO> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BeerDTOPageImpl(
        @JsonProperty("content") List<BeerDTO> content,
        @JsonProperty("number") int page,
        @JsonProperty("size") int size,
        @JsonProperty("totalElements") long total
    ) {
        super(content, PageRequest.of(page, size), total);
    }


    public BeerDTOPageImpl(List<BeerDTO> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }


    public BeerDTOPageImpl(List<BeerDTO> content) {
        super(content);
    }
}
