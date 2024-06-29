package com.dart.api.domain.gallery.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.dart.api.infrastructure.redis.ValueRedisRepository;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;
import com.dart.global.common.util.CharacterProcessor;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AutocompleteRedisRepository {

	private static final int MAX_SEARCH_RESULTS = 10;
	private static final int INITIAL_SCORE = 1;

	private final ValueRedisRepository valueRedisRepository;
	private final ZSetRedisRepository zSetRedisRepository;

	public void insert(String keyword, String category) {
		keyword = CharacterProcessor.splitString(keyword);

		for (int i = 1; i <= keyword.length(); i++) {
			String prefix = keyword.substring(0, i);
			String key = generateCategoryPrefixKey(category, prefix);
			Double score = CharacterProcessor.getUnicodeScore(keyword);

			zSetRedisRepository.addElement(key, keyword, score);
			valueRedisRepository.increment(REDIS_COUNT_PREFIX + key, 1);
		}
	}

	public List<String> search(String keyword, String category) {
		keyword = CharacterProcessor.splitString(keyword);
		String key = generateCategoryPrefixKey(keyword, category);
		String keyRemovedSpace = generateCategoryPrefixKey(keyword, category).trim();

		SortedSet<String> combinedSet = getCombinedSearchResults(key, keyRemovedSpace);

		return combinedSet.stream()
			.map(CharacterProcessor::mergeString)
			.limit(10)
			.collect(Collectors.toList());
	}

	public void remove(String keyword, String category) {
		keyword = CharacterProcessor.splitString(keyword);

		for (int i = 1; i <= keyword.length(); i++) {
			String prefix = keyword.substring(0, i);
			String key = generateCategoryPrefixKey(keyword, category);

			valueRedisRepository.increment(REDIS_COUNT_PREFIX + key, -1);

			if(getKeywordCount(key) < 1) {
				zSetRedisRepository.removeElement(key, keyword);
				valueRedisRepository.deleteValue(REDIS_COUNT_PREFIX + key);
			}
		}
	}

	private String generateCategoryPrefixKey(String category, String prefix) {
		switch (category) {
			case TITLE:
				return REDIS_TITLE_PREFIX + prefix;
			case AUTHOR:
				return REDIS_AUTHOR_PREFIX + prefix;
			case HASHTAG:
				return REDIS_HASHTAG_PREFIX + prefix;
			default:
				throw new NotFoundException(ErrorCode.FAIL_CATEGORY_NOT_FOUND);
		}
	}

	public boolean exists(String keyword, String category) {
		String key = generateCategoryPrefixKey(keyword, category);
		Double score = zSetRedisRepository.score(key, keyword);
		return score != null && score > 0;
	}

	private SortedSet<String> getCombinedSearchResults(String key, String keyRemovedSpace) {
		SortedSet<String> resultSet1 = new TreeSet<>(zSetRedisRepository.getRange(key, 0, MAX_SEARCH_RESULTS - 1));
		SortedSet<String> resultSet2 = new TreeSet<>(zSetRedisRepository.getRange(keyRemovedSpace, 0, MAX_SEARCH_RESULTS - 1));
		SortedSet<String> combinedSet = new TreeSet<>(resultSet1);
		combinedSet.addAll(resultSet2);
		return combinedSet;
	}

	private Long getKeywordCount(String key) {
		String count = valueRedisRepository.getValue(REDIS_COUNT_PREFIX + key);
		if (count == null) { return 0L; }
		else { return Long.valueOf(count); }
	}
}
