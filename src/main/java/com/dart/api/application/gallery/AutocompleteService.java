package com.dart.api.application.gallery;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.dart.api.domain.gallery.repository.AutocompleteRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutocompleteService {

	private final AutocompleteRedisRepository autocompleteRedisRepository;

	@Cacheable(value = "autocompleteCache", key = "#category + '_' + #keyword", cacheManager = "cacheManager")
	public List<String> autocomplete(String keyword, String category) {
		List<String> results = autocompleteRedisRepository.search(category, keyword);
		return results;
	}
}
