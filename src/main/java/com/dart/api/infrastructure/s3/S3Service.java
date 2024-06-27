package com.dart.api.infrastructure.s3;

import static com.dart.global.common.util.GlobalConstant.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

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
		String fileName = generateUniqueFilename(multipartFile.getOriginalFilename());

		if (!isValidImageFile(fileName)) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_IMAGE_EXTENSION);
		}

		try {
			ObjectMetadata metadata = createMetadata(multipartFile.getSize(), getContentTypeFromExtension(fileName));
			amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata));
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}

		return convertToCloudFrontUrl(fileName);
	}

	public String uploadThumbnail(MultipartFile imageFile) {
		String fileName = generateUniqueFilename(imageFile.getOriginalFilename());

		try {
			BufferedImage image = ImageIO.read(imageFile.getInputStream());
			BufferedImage thumbnail = resizeImageIfNeeded(image);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(thumbnail, getFileExtension(fileName), os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			ObjectMetadata metadata = createMetadata(os.size(), getContentTypeFromExtension(fileName));
			amazonS3.putObject(new PutObjectRequest(bucket, fileName, is, metadata));

			return convertToCloudFrontUrl(fileName);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
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
			return URLDecoder.decode(path, StandardCharsets.UTF_8).substring(1);
		} catch (MalformedURLException e) {
			throw new BadRequestException(ErrorCode.FAIL_INTERNAL_SERVER_ERROR);
		}
	}

	private String generateUniqueFilename(String originalFilename) {
		String uuid = UUID.randomUUID().toString();
		String extension = getFileExtension(originalFilename);
		if (extension.isEmpty()) {
			return uuid;
		} else {
			return uuid + "." + extension;
		}
	}

	private ObjectMetadata createMetadata(long contentLength, String contentType) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentLength);
		if (contentType != null) {
			metadata.setContentType(contentType);
		}
		return metadata;
	}

	private BufferedImage resizeImageIfNeeded(BufferedImage image) throws IOException {
		if (isNeedsResizing(image)) {
			return Thumbnails.of(image)
				.size(THUMBNAIL_RESIZING_SIZE, THUMBNAIL_RESIZING_SIZE)
				.asBufferedImage();
		} else {
			return image;
		}
	}

	private boolean isNeedsResizing(BufferedImage image) {
		return image.getWidth() > THUMBNAIL_RESIZING_SIZE || image.getHeight() > THUMBNAIL_RESIZING_SIZE;
	}

	private String getContentTypeFromExtension(String filename) {
		String ext = getFileExtension(filename).toLowerCase();
		return switch (ext) {
			case "jpg", "jpeg" -> "image/jpeg";
			case "png" -> "image/png";
			default -> null;
		};
	}

	private String getFileExtension(String filename) {
		int lastIndex = filename.lastIndexOf('.');
		return (lastIndex == -1) ? "" : filename.substring(lastIndex + 1);
	}

	private String convertToCloudFrontUrl(String fileName) {
		return String.format("%s/%s", cloudFrontDomain, fileName);
	}
}
