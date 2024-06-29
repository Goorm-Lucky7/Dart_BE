package com.dart.api.application.gallery;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.dart.api.domain.gallery.repository.TrieRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutocompleteService {

	private final TrieRedisRepository trieRedisRepository;

	@Cacheable(value = "autocompleteCache", key = "#category + '_' + #keyword")
	public List<String> autocomplete(String category, String keyword) {
		List<String> results = trieRedisRepository.search(category, keyword);
		return results;
	}
}
