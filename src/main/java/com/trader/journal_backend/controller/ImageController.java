package com.trader.journal_backend.controller;

import com.trader.journal_backend.model.Trade;
import com.trader.journal_backend.model.TradeImage;
import com.trader.journal_backend.repository.TradeImageRepository;
import com.trader.journal_backend.repository.TradeRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private TradeImageRepository imageRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Value("${minio.bucketName}")
    private String bucketName;

    @PostMapping("/upload/{tradeId}")
    public TradeImage uploadToMinio(
            @PathVariable Long tradeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        
        try {
            Trade trade = tradeRepository.findById(tradeId)
                    .orElseThrow(() -> new RuntimeException("Trade do not exist"));

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

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
            tradeImage.setUrl(bucketName + "/" + fileName);

            return imageRepository.save(tradeImage);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload lên MinIO: " + e.getMessage());
        }
    }
}