package com.sarthi.bookset;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One row per Book No + Set No in use, across ERC, Sleeper and Rail Pad.
 * A pair can belong to only one IC (unique book_no + set_no). Filled from the day this was released;
 * older ICs are not back-filled.
 */
@Entity
@Table(name = "ic_book_set_usage")
@Getter
@Setter
public class IcBookSetUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_no", nullable = false, length = 50)
    private String bookNo;

    @Column(name = "set_no", nullable = false, length = 20)
    private String setNo;

    /** Call number of the IC (certificate "R/CALL/USER" is reduced to CALL), so call no and certificate no match. */
    @Column(name = "ic_key", nullable = false, length = 100)
    private String icKey;

    @Column(name = "ic_number", length = 150)
    private String icNumber;

    @Column(name = "product_stage", length = 30)
    private String productStage;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
