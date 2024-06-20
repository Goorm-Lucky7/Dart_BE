package com.dart.api.infrastructure.s3;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final AmazonS3Client amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${cloud.aws.cloudfront.domain}")
	private String cloudFrontDomain;

	public String uploadFile(MultipartFile multipartFile) {
		String originalFilename = multipartFile.getOriginalFilename();
		String fileName = generateUniqueFilename(originalFilename);

		if (!isValidImageFile(fileName)) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_IMAGE_EXTENSION);
		}

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getSize());

		String contentType;
		if (fileName.contains(".")) {
			contentType = getContentTypeFromExtension(fileName);
			if (contentType != null) {
				metadata.setContentType(contentType);
			}
		}

		try {
			amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata));
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}

		return convertToCloudFrontUrl(fileName);
	}

	public void deleteFile(String fileUrl) {
		String fileKey = extractFileKeyFromUrl(fileUrl);
		amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
	}

	private boolean isValidImageFile(String fileName) {
		String ext = getFileExtension(fileName);
		return ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("png");
	}

	private String extractFileKeyFromUrl(String fileUrl) {
		try {
			URL url = new URL(fileUrl);
			String path = url.getPath();
			String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
			return decodedPath.substring(1);
		} catch (MalformedURLException e) {
			throw new BadRequestException(ErrorCode.FAIL_INTERNAL_SERVER_ERROR);
		}
	}

	private String generateUniqueFilename(String originalFilename) {
		String uuid = UUID.randomUUID().toString();
		if (originalFilename.contains(".")) {
			String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
			return uuid + extension;
		} else {
			return uuid;
		}
	}

	private String getContentTypeFromExtension(String filename) {
		String ext = getFileExtension(filename);
		return switch (ext.toLowerCase()) {
			case "jpg", "jpeg" -> "image/jpeg";
			case "png" -> "image/png";
			default -> null;
		};
	}

	private String getFileExtension(String filename) {
		int lastIndex = filename.lastIndexOf('.');
		if (lastIndex == -1) {
			return "";
		}
		return filename.substring(lastIndex + 1);
	}

	private String convertToCloudFrontUrl(String fileName) {
		return String.format("%s/%s", cloudFrontDomain, fileName);
	}
}
