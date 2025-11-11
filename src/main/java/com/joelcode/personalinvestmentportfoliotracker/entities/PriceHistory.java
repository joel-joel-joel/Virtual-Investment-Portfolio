package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;

@Entity
@Table (name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;




}
