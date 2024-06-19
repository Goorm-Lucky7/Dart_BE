package com.dart.api.domain.gallery.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {
	private final TrieNode root;

	public Trie() {
		this.root = new TrieNode();
	}

	public void insert(String word) {
		TrieNode node = root;

		for (char c : word.toCharArray()) {
			node = node.getChildren().computeIfAbsent(c, k -> new TrieNode());
		}
		node.setEndOfWord(true);
	}

	public List<String> search(String prefix) {
		List<String> results = new ArrayList<>();
		TrieNode node = root;

		for (char c : prefix.toCharArray()) {
			node = node.getChildren().get(c);
			if (node == null) {
				return results;
			}
		}

		findAllWords(node, new StringBuilder(prefix), results);
		return results;
	}

	private void findAllWords(TrieNode node, StringBuilder prefix, List<String> results) {
		if (node.isEndOfWord()) {
			results.add(prefix.toString());
		}

		for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
			prefix.append(entry.getKey());
			findAllWords(entry.getValue(), prefix, results);
			prefix.deleteCharAt(prefix.length() - 1);
		}
	}
}
