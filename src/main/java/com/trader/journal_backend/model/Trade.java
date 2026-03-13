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
    
    private BigDecimal averageEntryPrice;
    private BigDecimal totalVolume;
    
    private String status;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Order> orders;

    private BigDecimal totalRealizedPnl; 
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
}