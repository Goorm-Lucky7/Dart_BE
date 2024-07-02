package com.dart.api.infrastructure.s3;

import static com.dart.global.common.util.GlobalConstant.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
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
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

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
		return processAndUploadFile(multipartFile, false);
	}

	public String uploadThumbnail(MultipartFile imageFile) {
		return processAndUploadFile(imageFile, true);
	}

	public void deleteFile(String fileUrl) {
		String fileKey = extractFileKeyFromUrl(fileUrl);
		amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
	}

	private String processAndUploadFile(MultipartFile multipartFile, boolean isThumbnail) {
		String fileName = generateUniqueFilename(multipartFile.getOriginalFilename());

		if (!isValidImageFile(fileName)) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_IMAGE_EXTENSION);
		}

		try {
			BufferedImage image = ImageIO.read(multipartFile.getInputStream());
			if (image == null) {
				throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
			}

			BufferedImage processedImage = isThumbnail ? resizeImageIfNeeded(image) : image;
			String fileExtension = getFileExtension(fileName);

			BufferedImage finalImage =
				isThumbnail ? processedImage : addWatermark(processedImage, WATERMARK_TEXT, fileExtension);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			writeImage(finalImage, fileExtension, os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());

			ObjectMetadata metadata = createMetadata(os.size(), getContentTypeFromExtension(fileName));
			amazonS3.putObject(new PutObjectRequest(bucket, fileName, is, metadata));

			return convertToCloudFrontUrl(fileName);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
	}

	private boolean isValidImageFile(String fileName) {
		String ext = getFileExtension(fileName).toLowerCase();
		return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png");
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
		int width = image.getWidth();
		int height = image.getHeight();

		if (width <= THUMBNAIL_RESIZING_SIZE && height <= THUMBNAIL_RESIZING_SIZE) {
			return image;
		}

		Dimension newSize = calculateNewSize(width, height);
		return resizeImage(image, newSize.width, newSize.height);
	}

	private Dimension calculateNewSize(int width, int height) {
		if (width > height) {
			return new Dimension(THUMBNAIL_RESIZING_SIZE, (THUMBNAIL_RESIZING_SIZE * height) / width);
		} else {
			return new Dimension((THUMBNAIL_RESIZING_SIZE * width) / height, THUMBNAIL_RESIZING_SIZE);
		}
	}

	private BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight) {
		int imageType = (image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, imageType);
		Graphics2D g2d = resizedImage.createGraphics();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
		} finally {
			g2d.dispose();
		}
		return resizedImage;
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

	private BufferedImage addWatermark(BufferedImage sourceImage, String watermarkText, String fileExtension) {
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		int imageType =
			fileExtension.equalsIgnoreCase("png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

		BufferedImage watermarked = new BufferedImage(width, height, imageType);
		Graphics2D g2d = watermarked.createGraphics();

		g2d.drawImage(sourceImage, 0, 0, null);

		AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		g2d.setComposite(alphaChannel);
		g2d.setColor(Color.GRAY);

		int fontSize = Math.min(width, height) / 40;
		g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));

		FontMetrics fontMetrics = g2d.getFontMetrics();
		Rectangle2D rect = fontMetrics.getStringBounds(watermarkText, g2d);

		int centerX = width - (int)rect.getWidth() - 10;
		int centerY = height - (int)rect.getHeight() - 10;

		g2d.drawString(watermarkText, centerX, centerY);
		g2d.dispose();

		return watermarked;
	}

	private void writeImage(BufferedImage image, String fileExtension, ByteArrayOutputStream os) throws IOException {
		if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
			ImageIO.write(image, "jpg", os);
		} else if (fileExtension.equalsIgnoreCase("png")) {
			ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			ImageOutputStream ios = ImageIO.createImageOutputStream(os);
			writer.setOutput(ios);
			writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
			writer.dispose();
		}
	}
}
