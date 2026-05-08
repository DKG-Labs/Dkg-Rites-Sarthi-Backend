package com.sarthi.SRailPad.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
@Entity
@Table(name = "RAIL_TRANSITION_MASTER")
@Data
public class RailTransitionMaster {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "TRANSITIONID")
        private Integer transitionId;

        @Column(name = "TRANSITIONNAME", nullable = false)
        private String transitionName;

        @Column(name = "WORKFLOWID", nullable = false)
        private Integer workflowId;

        @Column(name = "CURRENTROLEID", nullable = false)
        private Integer currentRoleId;

        @Column(name = "NEXTROLEID", nullable = false)
        private Integer nextRoleId;

        @Column(name = "TRANSITIONORDER")
        private Integer transitionOrder;
//    @Column(name = "condition_id")
//    private Integer conditionId;

        @Column(name = "CREATEDBY")
        private String createdBy;
        @Column(name="CURRENT_ACTION")
        private String currentAction;

        @Column(name = "NEXT_ACTION")
        private String nextAction;


        @Column(name = "CREATEDDATE")
        private Date createdDate = new Date();
    }

