package com.trader.journal_backend.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.model.TradeImage;
import com.trader.journal_backend.repository.TradeImageRepository;
import com.trader.journal_backend.repository.TradeRepository;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final MinioClient minioClient;
    private final TradeImageRepository imageRepository;
    private final TradeRepository tradeRepository;

    @Value("${minio.bucketName}") private String bucketName;

    @Transactional
    public TradeImage uploadTradeImage(Long tradeId, MultipartFile file, String type) {
        log.info("IMAGE_UPLOAD_START | TradeID: {} | FileName: {} | Type: {}", tradeId, file.getOriginalFilename(), type);
        try {
            Trade trade = tradeRepository.findById(tradeId)
                    .orElseThrow(() -> new RuntimeException("Trade does not exist"));

            ensureBucketExists();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            TradeImage tradeImage = new TradeImage();
            tradeImage.setTrade(trade);
            tradeImage.setFileName(fileName);
            tradeImage.setFileType(type);

            TradeImage saved = imageRepository.save(tradeImage);
            log.info("IMAGE_UPLOAD_SUCCESS | ImageID: {} | FileName: {}", saved.getId(), fileName);
            return saved;
        } catch (Exception e) {
            log.error("IMAGE_UPLOAD_FAILED | TradeID: {} | Error: {}", tradeId, e.getMessage());
            throw new RuntimeException("Error occurred while uploading file: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteTradeImage(Long imageId) {
        log.info("IMAGE_DELETE_START | ImageID: {}", imageId);
        TradeImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image does not exist"));

        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(image.getFileName())
                    .build()
            );

            imageRepository.delete(image);
            log.info("IMAGE_DELETE_SUCCESS | ImageID: {} | FileName: {}", imageId, image.getFileName());
        } catch (Exception e) {
            log.error("IMAGE_DELETE_FAILED | ImageID: {} | Error: {}", imageId, e.getMessage());
            throw new RuntimeException("Error occurred while deleting file from MinIO: " + e.getMessage());
        }
    }

    public String generatePresignedUrl(String fileName) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(fileName)
                    .expiry(1, TimeUnit.DAYS)
                    .build()
            );
            log.debug("PRESIGNED_URL_GENERATED | FileName: {}", fileName);
            return url;
        } catch (Exception e) {
            log.error("PRESIGNED_URL_FAILED | FileName: {} | Error: {}", fileName, e.getMessage());
            throw new RuntimeException("Error occurred while generating pre-signed URL: " + e.getMessage());
        }
    }

    private void ensureBucketExists() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.warn("MINIO_BUCKET_MISSING | Creating bucket: {}", bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}