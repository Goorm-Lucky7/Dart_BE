package com.dart.api.application.gallery;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.dart.api.domain.gallery.repository.AutocompleteRedisRepository;
import com.dart.api.dto.gallery.response.AutocompleteResDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutocompleteService {

	private final AutocompleteRedisRepository autocompleteRedisRepository;

	@Cacheable(value = "autocompleteCache", key = "#category + '_' + #keyword")
	public AutocompleteResDto autocomplete(String category, String keyword) {
		return new AutocompleteResDto(autocompleteRedisRepository.search(category, keyword));
	}
}