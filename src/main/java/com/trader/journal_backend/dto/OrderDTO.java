package com.trader.journal_backend.dto;

import lombok.Data;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;

@Data
public class OrderDTO {
    private String symbol;
    private String side;
    private BigDecimal price;
    @Column(precision = 18, scale = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal volume;
    private BigDecimal sl;
    private BigDecimal tp;
}