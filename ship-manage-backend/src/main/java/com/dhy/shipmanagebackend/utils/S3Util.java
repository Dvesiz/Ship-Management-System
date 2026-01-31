package com.dhy.shipmanagebackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Component
public class S3Util {

    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.region}")
    private String regionStr;

    public String uploadFile(String originalFilename, InputStream inputStream) {
        // 1. 初始化 S3 客户端
        S3Client s3 = S3Client.builder()
                .region(Region.of(regionStr))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                // 开启 Path-Style 以兼容 ClawCloud
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString() + extension;

        try {
            // 2. 上传
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    // .acl("public-read") // 根据需要开启
                    .build();

            s3.putObject(putOb, RequestBody.fromInputStream(inputStream, inputStream.available()));

            // 3. 返回 URL
            // 确保 endpoint 没有结尾的 /
            String cleanEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
            return cleanEndpoint + "/" + bucketName + "/" + objectName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            s3.close();
        }
    }
}