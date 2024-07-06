package com.dart.api.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.gallery.AutocompleteService;
import com.dart.api.dto.gallery.response.AutocompleteResDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/autocomplete")
public class AutocompleteController {

	private final AutocompleteService autocompleteService;

	@GetMapping
	public AutocompleteResDto autocomplete(@RequestParam String category, @RequestParam String keyword) {
		return autocompleteService.autocomplete(category, keyword);
	}
}
