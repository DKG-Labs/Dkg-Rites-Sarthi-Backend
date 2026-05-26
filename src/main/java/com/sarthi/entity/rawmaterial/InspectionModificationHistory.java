package com.sarthi.entity.rawmaterial;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_modification_history")
@Data
public class InspectionModificationHistory {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "inspection_call_id", nullable = false)
        private Long inspectionCallId;

        @Column(name = "ic_number", nullable = false)
        private String icNumber;

        @Column(name = "modification_version")
        private Integer modificationVersion;

        @Column(name = "table_name")
        private String tableName;

        @Column(name = "field_name")
        private String fieldName;

        @Column(name = "old_value", columnDefinition = "TEXT")
        private String oldValue;

        @Column(name = "new_value", columnDefinition = "TEXT")
        private String newValue;

        @Column(name = "change_type")
        private String changeType;

        @Column(name = "modified_by")
        private String modifiedBy;

        @Column(name = "modified_at")
        private LocalDateTime modifiedAt;


}
