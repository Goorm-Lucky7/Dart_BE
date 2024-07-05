package com.dart.api.application.gallery;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dart.api.domain.gallery.repository.AutocompleteRedisRepository;
import com.dart.api.dto.gallery.response.AutocompleteResDto;
import com.dart.api.infrastructure.redis.ValueRedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutocompleteService {

	private final AutocompleteRedisRepository autocompleteRedisRepository;
	private final ValueRedisRepository valueRedisRepository;
	private final ObjectMapper objectMapper;

	public AutocompleteResDto autocomplete(String category, String keyword) {
		String cacheKey = "autocompleteCache::" + category + "_" + keyword;
		String cachedResult = valueRedisRepository.getValue(cacheKey);

		if (cachedResult != null) {
			try {
				AutocompleteResDto autocompleteResDto = objectMapper.readValue(cachedResult, AutocompleteResDto.class);
				return autocompleteResDto;
			} catch (Exception e) {
				log.info("Failed to deserialize cached result: " + e.getMessage());
			}
		}

		List<String> result = autocompleteRedisRepository.search(category, keyword);
		AutocompleteResDto resDto = new AutocompleteResDto(result);

		try {
			String serializedResult = objectMapper.writeValueAsString(resDto);
			valueRedisRepository.saveValueWithExpiry(cacheKey, serializedResult, 3600);
		} catch (Exception e) {
			log.info("Failed to serialize result for caching: " + e.getMessage());
		}

		return resDto;
	}
}