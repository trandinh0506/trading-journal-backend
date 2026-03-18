package com.trader.journal_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "trade_images")
@Data
public class TradeImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trade_id")
    @JsonBackReference
    private Trade trade;

    private String fileName;
    private String fileType;
    
    private LocalDateTime uploadedAt = LocalDateTime.now();
}