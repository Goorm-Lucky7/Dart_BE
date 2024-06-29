package com.dart.api.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.dart.api.application.gallery.AutocompleteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/autocomplete")
public class AutocompleteController {

	private final AutocompleteService autocompleteService;

	@GetMapping
	public List<String> autocomplete(@RequestParam String keyword, @RequestParam String category) {
		return autocompleteService.autocomplete(category, keyword);
	}
}
