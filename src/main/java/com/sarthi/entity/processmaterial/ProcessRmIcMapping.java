package com.sarthi.entity.processmaterial;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "process_rm_ic_mapping")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProcessRmIcMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ---- FOREIGN KEYS ----
    private Long processIcId;
    private Long rmIcId;

    // ---- RM IC INFORMATION ----
    @Column(columnDefinition = "TEXT")
    private String rmIcNumber;
    private String heatNumber;
    private String manufacturer;
    private String bookSetNo;

    // ---- QUANTITY INFORMATION ----
    private Integer rmQtyAccepted;

    // ---- RM IC DATE ----
    private LocalDate rmIcDate;

    // ---- TIMESTAMPS ----
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
