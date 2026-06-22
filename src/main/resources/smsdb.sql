CREATE TABLE `duty_sequence` (
  `duty_type` varchar(50) NOT NULL,
  `date` date NOT NULL,
  `sequence` int NOT NULL,
  PRIMARY KEY (`duty_type`,`date`)
) ;


CREATE TABLE `bloom_detail` (
  `cast_number` varchar(50) NOT NULL,
  `duty_id` varchar(50) NOT NULL,
  `bloom_identification` varchar(50) NOT NULL,
  `length_of_blooms` decimal(10,2) NOT NULL,
  `surface_condition_of_blooms` varchar(50) NOT NULL,
  `number_of_prime_blooms_rejected` int NOT NULL,
  `number_of_co_blooms_rejected` int NOT NULL,
  `remark` varchar(255) NOT NULL,
  PRIMARY KEY (`cast_number`),
  KEY `duty_id` (`duty_id`),
  CONSTRAINT `bloom_detail_ibfk_1` FOREIGN KEY (`cast_number`) REFERENCES `sms_heat_detail` (`heat_number`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `bloom_detail_ibfk_2` FOREIGN KEY (`duty_id`) REFERENCES `sms_duty` (`duty_id`) ON DELETE CASCADE ON UPDATE CASCADE
);



CREATE TABLE `bloom_detail_sms2` (
  `cast_number` varchar(50) NOT NULL,
  `duty_id` varchar(50) NOT NULL,
  `bloom_identification` varchar(50) NOT NULL,
  `length_of_blooms` decimal(10,2) NOT NULL,
  `surface_condition_of_blooms` varchar(50) NOT NULL,
  `number_of_prime_blooms_rejected` int NOT NULL,
  `number_of_co_blooms_rejected` int NOT NULL,
  `remark` varchar(255) NOT NULL,
  PRIMARY KEY (`cast_number`),
  KEY `duty_id` (`duty_id`),
  CONSTRAINT `bloom_detail_sms2_ibfk_1` FOREIGN KEY (`cast_number`) REFERENCES `heat_detail_sms2` (`heat_number`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `bloom_detail_sms2_ibfk_2` FOREIGN KEY (`duty_id`) REFERENCES `sms_duty` (`duty_id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `bloom_detail_sms3` (
  `cast_number` varchar(50) NOT NULL,
  `duty_id` varchar(50) NOT NULL,
  `bloom_identification` varchar(50) NOT NULL,
  `length_of_blooms` decimal(10,2) NOT NULL,
  `surface_condition_of_blooms` varchar(50) NOT NULL,
  `number_of_prime_blooms_rejected` int NOT NULL,
  `number_of_co_blooms_rejected` int NOT NULL,
  `remark` varchar(255) NOT NULL,
  PRIMARY KEY (`cast_number`),
  KEY `duty_id` (`duty_id`),
  CONSTRAINT `bloom_detail_sms3_ibfk_1` FOREIGN KEY (`cast_number`) REFERENCES `heat_detail_sms3` (`heat_number`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `bloom_detail_sms3_ibfk_2` FOREIGN KEY (`duty_id`) REFERENCES `sms_duty` (`duty_id`) ON DELETE CASCADE ON UPDATE CASCADE
);

 CREATE TABLE `sms_duty_heat` (
  `duty_id` varchar(50) NOT NULL,
  `heat_number` varchar(50) NOT NULL,
  `heat_procurement_stage` varchar(50) DEFAULT NULL,
  `heat_surrender_stage` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`duty_id`,`heat_number`),
  KEY `heat_number` (`heat_number`),
  CONSTRAINT `sms_duty_heat_ibfk_1` FOREIGN KEY (`duty_id`) REFERENCES `sms_duty` (`duty_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `sms_duty_heat_ibfk_2` FOREIGN KEY (`heat_number`) REFERENCES `sms_heat_detail` (`heat_number`) ON DELETE CASCADE ON UPDATE CASCADE
)


CREATE TABLE `duty_heat_sms2` (
  `duty_id` varchar(50) NOT NULL,
  `heat_number` varchar(50) NOT NULL,
  `heat_procurement_stage` varchar(50) DEFAULT NULL,
  `heat_surrender_stage` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`duty_id`,`heat_number`),
  KEY `heat_number` (`heat_number`),
  CONSTRAINT `duty_heat_sms2_ibfk_1` FOREIGN KEY (`duty_id`) REFERENCES `sms_duty` (`duty_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `duty_heat_sms2_ibfk_2` FOREIGN KEY (`heat_number`) REFERENCES `heat_detail_sms2` (`heat_number`) ON DELETE CASCADE ON UPDATE CASCADE
);



CREATE TABLE `duty_heat_sms3` (
  `duty_id` varchar(50) NOT NULL,
  `heat_number` varchar(50) NOT NULL,
  `heat_procurement_stage` varchar(50) DEFAULT NULL,
  `heat_surrender_stage` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`duty_id`,`heat_number`),
  KEY `heat_number` (`heat_number`),
  CONSTRAINT `duty_heat_sms3_ibfk_1` FOREIGN KEY (`duty_id`) REFERENCES `sms_duty` (`duty_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `duty_heat_sms3_ibfk_2` FOREIGN KEY (`heat_number`) REFERENCES `heat_detail_sms3` (`heat_number`) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE `sms_heat_detail` (
  `heat_number` varchar(50) NOT NULL,
  `heat_stage` varchar(50) NOT NULL,
  `heat_remark` varchar(50) DEFAULT NULL,
  `turn_down_temp` int DEFAULT NULL,
  `degassing_vacuum` decimal(10,2) DEFAULT NULL,
  `degassing_duration` decimal(10,2) DEFAULT NULL,
  `casting_temp` int DEFAULT NULL,
  `caster_number` varchar(10) DEFAULT NULL,
  `sequence_number` varchar(10) DEFAULT NULL,
  `hydris` decimal(10,2) DEFAULT NULL,
  `is_probe_dipped` tinyint(1) DEFAULT '0',
  `is_hydrogen_bw_80_and_100` tinyint(1) DEFAULT '0',
  `nitrogen` decimal(10,2) DEFAULT NULL,
  `oxygen` decimal(10,2) DEFAULT NULL,
  `number_of_prime_blooms` int DEFAULT NULL,
  `prime_blooms_length` decimal(10,2) DEFAULT NULL,
  `prime_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `number_of_co_blooms` int DEFAULT NULL,
  `co_blooms_length` decimal(10,2) DEFAULT NULL,
  `co_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `number_of_rejected_blooms` int DEFAULT NULL,
  `rejected_blooms_length` decimal(10,2) DEFAULT NULL,
  `rejected_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `weight_of_prime_blooms` decimal(10,2) DEFAULT NULL,
  `weight_of_co_blooms` decimal(10,2) DEFAULT NULL,
  `weight_of_rejected_blooms` decimal(10,2) DEFAULT NULL,
  `total_cast_wt` decimal(10,2) DEFAULT NULL,
  `is_diverted` tinyint(1) DEFAULT '0',
  `sent_to_ladle` varchar(50) DEFAULT NULL,
  `degassing_vacuum_wv` varchar(50) DEFAULT NULL,
  `turn_down_temp_wv` varchar(50) DEFAULT NULL,
  `degassing_duration_wv` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`heat_number`)
);


CREATE TABLE `heat_detail_sms2` (
  `heat_number` varchar(50) NOT NULL,
  `heat_stage` varchar(50) NOT NULL,
  `heat_remark` varchar(50) DEFAULT NULL,
  `turn_down_temp` int DEFAULT NULL,
  `degassing_vacuum` decimal(10,2) DEFAULT NULL,
  `degassing_duration` decimal(10,2) DEFAULT NULL,
  `turn_down_temp_wv` varchar(20) DEFAULT NULL,
  `degassing_vacuum_wv` varchar(20) DEFAULT NULL,
  `degassing_duration_wv` varchar(20) DEFAULT NULL,
  `casting_temp` int DEFAULT NULL,
  `casting_temp_2` int DEFAULT NULL,
  `caster_number` varchar(10) DEFAULT NULL,
  `sequence_number` varchar(10) DEFAULT NULL,
  `hydris` decimal(10,2) DEFAULT NULL,
  `is_probe_dipped` tinyint(1) DEFAULT '0',
  `is_hydrogen_bw_80_and_100` tinyint(1) DEFAULT '0',
  `nitrogen` decimal(10,4) DEFAULT NULL,
  `oxygen` decimal(10,2) DEFAULT NULL,
  `number_of_prime_blooms` int DEFAULT NULL,
  `prime_blooms_length` decimal(10,2) DEFAULT NULL,
  `prime_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `number_of_co_blooms` int DEFAULT NULL,
  `co_blooms_length` decimal(10,2) DEFAULT NULL,
  `co_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `number_of_rejected_blooms` int DEFAULT NULL,
  `rejected_blooms_length` decimal(10,2) DEFAULT NULL,
  `rejected_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `weight_of_prime_blooms` decimal(10,2) DEFAULT NULL,
  `weight_of_co_blooms` decimal(10,2) DEFAULT NULL,
  `weight_of_rejected_blooms` decimal(10,2) DEFAULT NULL,
  `total_cast_wt` decimal(10,2) DEFAULT NULL,
  `is_diverted` tinyint(1) DEFAULT '0',
  `sent_to_ladle` varchar(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `other_remark` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`heat_number`)
);

CREATE TABLE `heat_detail_sms3` (
  `heat_number` varchar(50) NOT NULL,
  `heat_stage` varchar(50) NOT NULL,
  `heat_remark` varchar(50) DEFAULT NULL,
  `turn_down_temp` int DEFAULT NULL,
  `degassing_vacuum` decimal(10,2) DEFAULT NULL,
  `degassing_duration` decimal(10,2) DEFAULT NULL,
  `degassing_vacuum_wv` varchar(20) DEFAULT NULL,
  `turn_down_temp_wv` varchar(20) DEFAULT NULL,
  `degassing_duration_wv` varchar(20) DEFAULT NULL,
  `casting_temp` int DEFAULT NULL,
  `casting_temp_2` int DEFAULT NULL,
  `caster_number` varchar(10) DEFAULT NULL,
  `sequence_number` varchar(10) DEFAULT NULL,
  `hydris` decimal(10,2) DEFAULT NULL,
  `is_probe_dipped` tinyint(1) DEFAULT '0',
  `is_hydrogen_bw_80_and_100` tinyint(1) DEFAULT '0',
  `nitrogen` decimal(10,4) DEFAULT NULL,
  `oxygen` decimal(10,2) DEFAULT NULL,
  `number_of_prime_blooms` int DEFAULT NULL,
  `prime_blooms_length` decimal(10,2) DEFAULT NULL,
  `prime_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `number_of_co_blooms` int DEFAULT NULL,
  `co_blooms_length` decimal(10,2) DEFAULT NULL,
  `co_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `number_of_rejected_blooms` int DEFAULT NULL,
  `rejected_blooms_length` decimal(10,2) DEFAULT NULL,
  `rejected_blooms_total_length` decimal(10,2) DEFAULT NULL,
  `weight_of_prime_blooms` decimal(10,2) DEFAULT NULL,
  `weight_of_co_blooms` decimal(10,2) DEFAULT NULL,
  `weight_of_rejected_blooms` decimal(10,2) DEFAULT NULL,
  `total_cast_wt` decimal(10,2) DEFAULT NULL,
  `is_diverted` tinyint(1) DEFAULT '0',
  `sent_to_ladle` varchar(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `other_remark` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`heat_number`)
);


CREATE TABLE `sms_duty` (
  `user_id` INT NOT NULL,
  `duty_id` varchar(50) NOT NULL,
  `date` date NOT NULL,
  `shift` varchar(50) NOT NULL,
  `sms` varchar(50) NOT NULL,
  `rail_grade` varchar(50) NOT NULL,
  `shift_remarks` varchar(100) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `is_ems_functioning` tinyint(1) DEFAULT '0',
  `is_slag_detector_functioning` tinyint(1) DEFAULT '0',
  `is_amlc_functioning` tinyint(1) DEFAULT '0',
  `is_hydrogen_measurement_automatic` tinyint(1) DEFAULT '0',
  `is_ladle_to_tundish_used` tinyint(1) DEFAULT '0',
  `is_tundish_to_mould_used` tinyint(1) DEFAULT '0',
  `make_of_casting_powder` varchar(50) DEFAULT NULL,
  `make_of_hydris_probe` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`duty_id`),
  KEY `userid` (`user_id`),
  CONSTRAINT `sms_duty_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_master` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
)



DESC user_master;