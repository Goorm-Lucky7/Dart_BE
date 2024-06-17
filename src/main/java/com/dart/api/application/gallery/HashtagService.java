package com.dart.api.application.gallery;

import static com.dart.global.common.util.GlobalConstant.*;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Hashtag;
import com.dart.api.domain.gallery.repository.HashtagRepository;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HashtagService {

	private final HashtagRepository hashtagRepository;

	public void saveHashtags(List<String> hashTags, Gallery gallery) {
		if (hashTags != null) {
			validateHashtagsSize(hashTags);
			validateHashtagsLength(hashTags);

			final List<Hashtag> hashtags = hashTags.stream()
				.map(tag -> Hashtag.builder().tag(tag).gallery(gallery).build())
				.collect(Collectors.toList());

			hashtagRepository.saveAll(hashtags);
		}
	}

	public List<String> findHashtagsByGallery(Gallery gallery) {
		return hashtagRepository.findTagByGallery(gallery);
	}

	public void deleteHashtagsByGallery(Gallery gallery) {
		List<Hashtag> hashtags = hashtagRepository.findByGallery(gallery);
		hashtagRepository.deleteAll(hashtags);
	}

	private void validateHashtagsSize(List<String> hashtags) {
		if (hashtags.size() > MAX_HASHTAG_SIZE) {
			throw new BadRequestException(ErrorCode.FAIL_HASHTAG_SIZE_EXCEEDED);
		}
	}

	private void validateHashtagsLength(List<String> hashtags) {
		final Pattern pattern = Pattern.compile("^[^\\s]{1,10}$");

		boolean invalidTagFound = hashtags.parallelStream().anyMatch(tag -> !pattern.matcher(tag).matches());

		if (invalidTagFound) {
			throw new BadRequestException(ErrorCode.FAIL_TAG_CONTAINS_SPACE_OR_INVALID_LENGTH);
		}
	}
}
