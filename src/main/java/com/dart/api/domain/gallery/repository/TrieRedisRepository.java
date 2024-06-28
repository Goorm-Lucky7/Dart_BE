package com.dart.api.domain.gallery.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.dart.api.infrastructure.redis.ValueRedisRepository;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;
import com.dart.global.common.util.CharacterSplitter;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TrieRedisRepository {

	private final ValueRedisRepository valueRedisRepository;
	private final ZSetRedisRepository zSetRedisRepository;

	public void insert(String type, String keyword) {
		keyword = CharacterSplitter.splitString(keyword);
		for (int i = 1; i <= keyword.length(); i++) {
			String prefix = keyword.substring(0, i);
			String key = generateTypePrefixKey(type, prefix);
			System.out.println("insert key :" + i +" : "+key);
			Double score = CharacterSplitter.getUnicodeScore(keyword);

			zSetRedisRepository.addElement(key, keyword, score);
			valueRedisRepository.increment(REDIS_COUNT_PREFIX + key, 1);
		}
	}

	public List<String> search(String type, String prefix) {
		String splitPrefix = CharacterSplitter.splitString(prefix);
		String splitConvertedPrefix = CharacterSplitter.splitAndConvertString(prefix);

		String key1 = generateTypePrefixKey(type, splitPrefix);
		String key2 = generateTypePrefixKey(type, splitConvertedPrefix);

		SortedSet<String> resultSet1 = new TreeSet<>(zSetRedisRepository.getRange(key1, 0, 9));
		SortedSet<String> resultSet2 = new TreeSet<>(zSetRedisRepository.getRange(key2, 0, 9));

		SortedSet<String> combinedSet = new TreeSet<>(resultSet1);
		combinedSet.addAll(resultSet2);

		return combinedSet.stream()
			.map(CharacterSplitter::mergeKoreanString)
			.limit(10)
			.collect(Collectors.toList());
	}

	public void remove(String type, String keyword) {
		keyword = CharacterSplitter.splitString(keyword);

		for (int i = 1; i <= keyword.length(); i++) {
			String prefix = keyword.substring(0, i);
			String key = generateTypePrefixKey(type, prefix);

			valueRedisRepository.increment(REDIS_COUNT_PREFIX + key, -1);

			if(getKeywordCount(key) < 1) {
				zSetRedisRepository.removeElement(key, keyword);
				valueRedisRepository.deleteValue(REDIS_COUNT_PREFIX + key);
			}
		}
	}

	private Long getKeywordCount(String key) {
		String count = valueRedisRepository.getValue(REDIS_COUNT_PREFIX + key);
		if (count == null) { return 0L; }
		else { return Long.valueOf(count); }
	}

	public boolean exists(String type, String keyword) {
		String key = generateTypePrefixKey(type, keyword);
		Double score = zSetRedisRepository.score(key, keyword);
		return score != null && score > 0;
	}

	private String generateTypePrefixKey(String type, String prefix) {
		type = type.trim();
		switch (type) {
			case TITLE:
				return REDIS_TITLE_PREFIX + prefix;
			case AUTHOR:
				return REDIS_AUTHOR_PREFIX + prefix;
			case HASHTAG:
				return REDIS_HASHTAG_PREFIX + prefix;
			default:
				throw new NotFoundException(ErrorCode.FAIL_TYPE_NOT_FOUND);
		}
	}
}
