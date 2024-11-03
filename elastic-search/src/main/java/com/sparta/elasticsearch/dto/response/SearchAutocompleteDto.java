package com.sparta.elasticsearch.dto.response;

import com.sparta.elasticsearch.dto.SearchAutoTitleDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SearchAutocompleteDto {
    private List<SearchAutoTitleDto> titles;

    public static SearchAutocompleteDto of(List<SearchAutoTitleDto> titles) {
        return new SearchAutocompleteDto(titles);
    }
}
