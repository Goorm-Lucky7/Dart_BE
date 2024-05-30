package luckyseven.dart.global.common.util;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final AmazonS3Client amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public String uploadFile(MultipartFile multipartFile) throws IOException {
		String originalFilename = multipartFile.getOriginalFilename();
		String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(multipartFile.getSize());
		metadata.setContentType(multipartFile.getContentType());

		amazonS3.putObject(new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata));
		return amazonS3.getUrl(bucket, fileName).toString();
	}

	public void deleteFile(String fileUrl) {
		String fileKey = extractFileKeyFromUrl(fileUrl);
		amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
	}

	private String extractFileKeyFromUrl(String fileUrl) {
		try {
			URL url = new URL(fileUrl);
			String path = url.getPath();
			String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
			return decodedPath.substring(1);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid file URL: " + fileUrl, e);
		}
	}
}
