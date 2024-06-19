package com.dart.api.domain.gallery.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TrieRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void insert(String type, String word) {
		String key = REDIS_TRIE_PREFIX + type.toLowerCase();
		String nodeKey = key;

		for (char c : word.toCharArray()) {
			nodeKey += ":" + c;
			redisTemplate.opsForHash().putIfAbsent(nodeKey, IS_END_OF_WORD, false);
			System.out.println("Inserted node: " + nodeKey);
		}
		redisTemplate.opsForHash().put(nodeKey, IS_END_OF_WORD, true);
		System.out.println("Word end at: " + nodeKey);
	}

	public List<String> search(String type, String prefix) {
		String key = REDIS_TRIE_PREFIX + type.toLowerCase();
		String nodeKey = key;

		System.out.println("Searching for prefix: " + prefix);
		for (char c : prefix.toCharArray()) {
			nodeKey += ":" + c;
			if (!redisTemplate.hasKey(nodeKey)) {
				System.out.println("Node not found: " + nodeKey);
				return List.of();
			}
			System.out.println("Found node: " + nodeKey);
		}

		return findAllWords(nodeKey, new StringBuilder(prefix));
	}

	private List<String> findAllWords(String nodeKey, StringBuilder prefix) {
		List<String> results = new ArrayList<>();
		Boolean isEndOfWord = (Boolean) redisTemplate.opsForHash().get(nodeKey, IS_END_OF_WORD);

		if (Boolean.TRUE.equals(isEndOfWord)) {
			results.add(prefix.toString());
		}

		Set<Object> children = redisTemplate.opsForHash().keys(nodeKey);
		for (Object child : children) {
			if (!child.equals(IS_END_OF_WORD)) {
				prefix.append(child.toString().charAt(0));
				results.addAll(findAllWords(nodeKey + ":" + child, prefix));
				prefix.deleteCharAt(prefix.length() - 1);
			}
		}
		System.out.println("Results: " + results);
		return results;
	}
}
