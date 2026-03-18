package com.trader.journal_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "trades")
@Data
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String symbol;
    private String side;

    private BigDecimal currentSl; 
    private BigDecimal currentTp;

    @Column(precision = 18, scale = 8)
    private BigDecimal averageEntryPrice;

    @Column(precision = 18, scale = 8)
    private BigDecimal totalExecutedVolume;
    
    @Column(precision = 18, scale = 8)
    private BigDecimal totalEntryVolume;

    @Column(name = "total_volume", precision = 18, scale = 8)
    private BigDecimal totalVolume;
    
    private String status;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Order> orders;

    @Column(name = "total_realized_pnl", precision = 18, scale = 8)
    private BigDecimal totalRealizedPnl; 

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TradeImage> images;
    
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

}