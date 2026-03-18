package com.trader.journal_backend.controller;

import com.trader.journal_backend.model.TradeImage;
import com.trader.journal_backend.service.ImageService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload/{tradeId}")
    public ResponseEntity<TradeImage> upload(@PathVariable Long tradeId,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam("type") String type) {
        return ResponseEntity.ok(imageService.uploadTradeImage(tradeId, file, type));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable Long imageId) {
        imageService.deleteTradeImage(imageId);
        return ResponseEntity.ok("Delete image successfully!");
    }
}