package com.dart.api.domain.gallery.entity;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
	private boolean isEndOfWord;
	private Map<Character, TrieNode> children = new HashMap<>();

	public TrieNode() {
		this.isEndOfWord = false;
	}

	public boolean isEndOfWord() {
		return isEndOfWord;
	}

	public void setEndOfWord(boolean endOfWord) {
		isEndOfWord = endOfWord;
	}

	public Map<Character, TrieNode> getChildren() {
		return children;
	}
}
