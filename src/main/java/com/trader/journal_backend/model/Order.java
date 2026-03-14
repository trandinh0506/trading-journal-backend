package com.trader.journal_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "trading_orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trade_id", referencedColumnName = "id")
    @JsonBackReference
    private Trade trade;

    private String side;

    private BigDecimal sl;
    private BigDecimal tp;

    @Column(precision = 18, scale = 8)
    private BigDecimal price;

    @Column(precision = 18, scale = 8)
    private BigDecimal volume; 
    
    private BigDecimal fee;
    private String feeAsset;

    @Column(name = "realized_pnl", precision = 18, scale = 8)
    private BigDecimal realizedPnl;
    
    @Column(unique = true)
    private String externalOrderId;

    private LocalDateTime executedAt;
}