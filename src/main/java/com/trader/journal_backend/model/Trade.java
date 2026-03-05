package com.trader.journal_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String side; 
    
    private BigDecimal averageEntryPrice;
    private BigDecimal totalVolume;
    
    private BigDecimal currentSl; 
    private BigDecimal currentTp;

    private String status;
    
    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Order> orders;

    @OneToMany(mappedBy = "trade")
    private List<TradeImage> images;
}