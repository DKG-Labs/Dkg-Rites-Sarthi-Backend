package com.sarthi.Sms.repository.sms;

import com.sarthi.Sms.entity.sms.HeatDtlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HeatDtlRepository extends JpaRepository<HeatDtlEntity, String> {
        Optional<HeatDtlEntity> findByHeatNo(String heatNo);

        @Query("SELECT h FROM HeatDtlEntity h LEFT JOIN BloomDtlEntity b ON h.heatNo = b.castNo WHERE b.castNo IS NULL")
        List<HeatDtlEntity> findHeatDetailsNotInBloomDetails();

        // @Query(value = "SELECT " +
        //                 "date, sms, caster_number, rail_grade, " +
        //                 "COUNT(heat_number), " +
        //                 "COUNT(rejected_blooms), " +
        //                 "COUNT(is_diverted), " +
        //                 "GROUP_CONCAT(rejected_heat_numbers), " +
        //                 "SUM(total_cast_wt), " +
        //                 "SUM(weight_of_prime_blooms), " +
        //                 "SUM(weight_of_co_blooms), " +
        //                 "SUM(total_good_blooms), " +
        //                 "SUM(weight_of_rejected_blooms), " +
        //                 "GROUP_CONCAT(remark) " +
        //                 "FROM (" +
        //                     "SELECT sd.date, sd.sms, shd.caster_number, sd.rail_grade, " +
        //                     "shd.heat_number, " +
        //                     "CASE WHEN bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 THEN 1 END as rejected_blooms, " +
        //                     "CASE WHEN shd.is_diverted = TRUE THEN 1 END as is_diverted, " +
        //                     "CASE WHEN bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 THEN shd.heat_number END as rejected_heat_numbers, " +
        //                     "shd.total_cast_wt, " +
        //                     "shd.weight_of_prime_blooms, " +
        //                     "shd.weight_of_co_blooms, " +
        //                     "(shd.weight_of_prime_blooms + shd.weight_of_co_blooms) as total_good_blooms, " +
        //                     "shd.weight_of_rejected_blooms, " +
        //                     "bd.remark " +
        //                     "FROM sms_duty sd " +
        //                     "JOIN duty_heat_sms2 dh ON sd.duty_id = dh.duty_id " +
        //                     "JOIN heat_detail_sms2 shd ON dh.heat_number = shd.heat_number " +
        //                     "LEFT JOIN bloom_detail_sms2 bd ON shd.heat_number = bd.cast_number " +
        //                     "WHERE sd.date BETWEEN :startDate AND :endDate " +
        //                 "UNION ALL " +
        //                     "SELECT sd.date, sd.sms, shd.caster_number, sd.rail_grade, " +
        //                     "shd.heat_number, " +
        //                     "CASE WHEN bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 THEN 1 END as rejected_blooms, " +
        //                     "CASE WHEN shd.is_diverted = TRUE THEN 1 END as is_diverted, " +
        //                     "CASE WHEN bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 THEN shd.heat_number END as rejected_heat_numbers, " +
        //                     "shd.total_cast_wt, " +
        //                     "shd.weight_of_prime_blooms, " +
        //                     "shd.weight_of_co_blooms, " +
        //                     "(shd.weight_of_prime_blooms + shd.weight_of_co_blooms) as total_good_blooms, " +
        //                     "shd.weight_of_rejected_blooms, " +
        //                     "bd.remark " +
        //                     "FROM sms_duty sd " +
        //                     "JOIN duty_heat_sms3 dh ON sd.duty_id = dh.duty_id " +
        //                     "JOIN heat_detail_sms3 shd ON dh.heat_number = shd.heat_number " +
        //                     "LEFT JOIN bloom_detail_sms3 bd ON shd.heat_number = bd.cast_number " +
        //                     "WHERE sd.date BETWEEN :startDate AND :endDate" +
        //                 ") combined_data " +
        //                 "GROUP BY date, sms, caster_number, rail_grade", nativeQuery = true)
        // List<Object[]> getRecordDtl(@Param("startDate") LocalDateTime startDate,
        //                 @Param("endDate") LocalDateTime endDate);



        @Query(value = "SELECT " +
                        "DATE_FORMAT(date, '%Y-%m-%d') as formatted_date, " +
                        "sms, " +
                        "shift, " +
                        "GROUP_CONCAT(DISTINCT caster_number SEPARATOR ', ') as caster_numbers, " +
                        "rail_grade, " +
                        "COUNT(DISTINCT heat_number) as total_heats_cast, " +
                        "SUM(CASE WHEN is_heat_rejected = 1 THEN 1 ELSE 0 END) as rejected_heat_count, " +
                        "SUM(CASE WHEN is_diverted = 1 THEN 1 ELSE 0 END) as diverted_heat_count, " +
                        "GROUP_CONCAT(DISTINCT CASE WHEN is_heat_rejected = 1 THEN heat_number END SEPARATOR ', ') as rejected_heat_numbers, " +
                        "SUM(total_cast_wt) as total_weight_cast, " +
                        "SUM(weight_of_prime_blooms) as total_prime_blooms_weight, " +
                        "SUM(weight_of_co_blooms) as total_co_blooms_weight, " +
                        "SUM(weight_of_prime_blooms + weight_of_co_blooms) as total_accepted_blooms_weight, " +
                        "SUM(weight_of_rejected_blooms) as total_rejected_blooms_weight, " +
                        "GROUP_CONCAT(DISTINCT CASE WHEN is_heat_rejected = 1 THEN rejection_reason END SEPARATOR '; ') as reason_for_rejection " +
                        "FROM (" +
                            "SELECT sd.date, sd.sms, sd.shift, shd.caster_number, sd.rail_grade, " +
                            "shd.heat_number, " +
                            "CASE WHEN (bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 OR " +
                            "     shd.hydris > 1.6 OR shd.oxygen > 20 OR shd.nitrogen > 0.009 OR " +
                            "     shd.heat_remark LIKE '%reject%' OR shd.heat_remark LIKE '%Reject%') THEN 1 ELSE 0 END as is_heat_rejected, " +
                            "CASE WHEN shd.is_diverted = TRUE THEN 1 ELSE 0 END as is_diverted, " +
                            "CASE " +
                            "  WHEN shd.hydris > 1.6 THEN 'Rejected for Hydrogen' " +
                            "  WHEN shd.oxygen > 20 THEN 'Rejected for Oxygen' " +
                            "  WHEN shd.nitrogen > 0.009 THEN 'Rejected for Nitrogen' " +
                            "  WHEN bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 THEN COALESCE(bd.remark, 'Bloom Rejection') " +
                            "  WHEN shd.heat_remark LIKE '%reject%' OR shd.heat_remark LIKE '%Reject%' THEN shd.heat_remark " +
                            "  ELSE NULL " +
                            "END as rejection_reason, " +
                            "shd.total_cast_wt, " +
                            "shd.weight_of_prime_blooms, " +
                            "shd.weight_of_co_blooms, " +
                            "shd.weight_of_rejected_blooms " +
                            "FROM sms_duty sd " +
                            "JOIN duty_heat_sms2 dh ON sd.duty_id = dh.duty_id " +
                            "JOIN heat_detail_sms2 shd ON dh.heat_number = shd.heat_number " +
                            "LEFT JOIN bloom_detail_sms2 bd ON shd.heat_number = bd.cast_number " +
                            "WHERE sd.date BETWEEN :startDate AND :endDate " +
                        "UNION ALL " +
                            "SELECT sd.date, sd.sms, sd.shift, shd.caster_number, sd.rail_grade, " +
                            "shd.heat_number, " +
                            "CASE WHEN (bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 OR " +
                            "     shd.hydris > 1.6 OR shd.oxygen > 20 OR shd.nitrogen > 0.009 OR " +
                            "     shd.heat_remark LIKE '%reject%' OR shd.heat_remark LIKE '%Reject%') THEN 1 ELSE 0 END as is_heat_rejected, " +
                            "CASE WHEN shd.is_diverted = TRUE THEN 1 ELSE 0 END as is_diverted, " +
                            "CASE " +
                            "  WHEN shd.hydris > 1.6 THEN 'Rejected for Hydrogen' " +
                            "  WHEN shd.oxygen > 20 THEN 'Rejected for Oxygen' " +
                            "  WHEN shd.nitrogen > 0.009 THEN 'Rejected for Nitrogen' " +
                            "  WHEN bd.number_of_prime_blooms_rejected > 0 OR bd.number_of_co_blooms_rejected > 0 THEN COALESCE(bd.remark, 'Bloom Rejection') " +
                            "  WHEN shd.heat_remark LIKE '%reject%' OR shd.heat_remark LIKE '%Reject%' THEN shd.heat_remark " +
                            "  ELSE NULL " +
                            "END as rejection_reason, " +
                            "shd.total_cast_wt, " +
                            "shd.weight_of_prime_blooms, " +
                            "shd.weight_of_co_blooms, " +
                            "shd.weight_of_rejected_blooms " +
                            "FROM sms_duty sd " +
                            "JOIN duty_heat_sms3 dh ON sd.duty_id = dh.duty_id " +
                            "JOIN heat_detail_sms3 shd ON dh.heat_number = shd.heat_number " +
                            "LEFT JOIN bloom_detail_sms3 bd ON shd.heat_number = bd.cast_number " +
                            "WHERE sd.date BETWEEN :startDate AND :endDate " +
                        ") combined_data " +
                        "GROUP BY date, sms, shift, rail_grade " +
                        "ORDER BY date DESC, sms, shift", nativeQuery = true)
List<Object[]> getRecordDtl(@Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);





        // @Query(value = "SELECT * FROM (" +
        //                 "SELECT " +
        //                 "shd.heat_number, " +
        //                 "sd.sms, " +
        //                 "shd.caster_number, " +
        //                 "sd.rail_grade, " +
        //                 "CONCAT(sd.date, ' ', sd.shift), " +
        //                 "shd.sequence_number, " +
        //                 "shd.turn_down_temp, " +
        //                 "shd.degassing_vacuum, " +
        //                 "shd.degassing_duration, " +
        //                 "shd.casting_temp, " +
        //                 "shd.hydris, " +
        //                 "shd.nitrogen, " +
        //                 "shd.oxygen, " +
        //                 "shd.sent_to_ladle, " +
        //                 "shd.number_of_prime_blooms, " +
        //                 "shd.number_of_co_blooms, " +
        //                 "shd.number_of_rejected_blooms, " +
        //                 "shd.total_cast_wt, " +
        //                 "shd.heat_remark, " +
        //                 "bd.remark, " +
        //                 "CAST(shd.is_hydrogen_bw_80_and_100 AS CHAR) AS Is_Hydris_Measured_bw_80_to_100m_of_casting, " +
        //                 "CAST(shd.is_probe_dipped AS CHAR) AS Is_Probe_Dipped_Below_300mm_from_Slag_metal_Interface, " +
        //                 "sd.make_of_casting_powder AS Use_of_Imported_Casting_Powder, " +
        //                 "CAST(sd.is_ems_functioning AS CHAR) AS Is_EMS_Functioning, " +
        //                 "CAST(sd.is_slag_detector_functioning AS CHAR) AS Is_Slag_Detector_Working, " +
        //                 "CAST(sd.is_amlc_functioning AS CHAR) AS Is_AMLC_Functioning, " +
        //                 "CAST(sd.is_hydrogen_measurement_automatic AS CHAR) AS Is_Hydrogen_Measurement_Automatic, " +
        //                 "CAST(sd.is_ladle_to_tundish_used AS CHAR) AS Is_Ladle_to_Tundish_Used, " +
        //                 "CAST(sd.is_tundish_to_mould_used AS CHAR) AS Is_Tundish_to_Mould_Used " +
        //                 "FROM sms_duty sd " +
        //                 "JOIN duty_heat_sms2 dh ON sd.duty_id = dh.duty_id " +
        //                 "JOIN heat_detail_sms2 shd ON dh.heat_number = shd.heat_number " +
        //                 "LEFT JOIN bloom_detail_sms2 bd ON shd.heat_number = bd.cast_number " +
        //                 "WHERE sd.date BETWEEN :startDate AND :endDate " +
        //                 "AND sd.sms = 'SMS 2'" +
        //                 "UNION ALL " +
        //                 "SELECT " +
        //                 "shd.heat_number, " +
        //                 "sd.sms, " +
        //                 "shd.caster_number, " +
        //                 "sd.rail_grade, " +
        //                 "CONCAT(sd.date, ' ', sd.shift), " +
        //                 "shd.sequence_number, " +
        //                 "shd.turn_down_temp, " +
        //                 "shd.degassing_vacuum, " +
        //                 "shd.degassing_duration, " +
        //                 "shd.casting_temp, " +
        //                 "shd.hydris, " +
        //                 "shd.nitrogen, " +
        //                 "shd.oxygen, " +
        //                 "shd.sent_to_ladle, " +
        //                 "shd.number_of_prime_blooms, " +
        //                 "shd.number_of_co_blooms, " +
        //                 "shd.number_of_rejected_blooms, " +
        //                 "shd.total_cast_wt, " +
        //                 // In the second part of the UNION query, update these lines:
        //                 "shd.heat_remark, " +
        //                 "bd.remark, " +
        //                 "CAST(shd.is_hydrogen_bw_80_and_100 AS CHAR) AS Is_Hydris_Measured_bw_80_to_100m_of_casting, " +
        //                 "CAST(shd.is_probe_dipped AS CHAR) AS Is_Probe_Dipped_Below_300mm_from_Slag_metal_Interface, " +
        //                 "sd.make_of_casting_powder AS Use_of_Imported_Casting_Powder, " +
        //                 "CAST(sd.is_ems_functioning AS CHAR) AS Is_EMS_Functioning, " +
        //                 "CAST(sd.is_slag_detector_functioning AS CHAR) AS Is_Slag_Detector_Working, " +
        //                 "CAST(sd.is_amlc_functioning AS CHAR) AS Is_AMLC_Functioning, " +
        //                 "CAST(sd.is_hydrogen_measurement_automatic AS CHAR) AS Is_Hydrogen_Measurement_Automatic, " +
        //                 "CAST(sd.is_ladle_to_tundish_used AS CHAR) AS Is_Ladle_to_Tundish_Used, " +
        //                 "CAST(sd.is_tundish_to_mould_used AS CHAR) AS Is_Tundish_to_Mould_Used " +
        //                 "FROM sms_duty sd " +
        //                 "JOIN duty_heat_sms3 dh ON sd.duty_id = dh.duty_id " +
        //                 "JOIN heat_detail_sms3 shd ON dh.heat_number = shd.heat_number " +
        //                 "LEFT JOIN bloom_detail_sms3 bd ON shd.heat_number = bd.cast_number " +
        //                 "WHERE sd.date BETWEEN :startDate AND :endDate " +
        //                 "AND sd.sms = 'SMS 3'" +
        //             ") combined_data " +
        //             "ORDER BY heat_number", nativeQuery = true)

        // List<Object[]> getHeatReport(@Param("startDate") LocalDateTime startDate,
        //                 @Param("endDate") LocalDateTime endDate);



        @Query(value = """
SELECT * FROM (
    SELECT 
        shd.heat_number,
        sd.sms,
        shd.caster_number,
        sd.rail_grade,
        CONCAT(sd.date, ' ', sd.shift) AS date_and_shift_of_casting,
        shd.sequence_number,
        shd.turn_down_temp,
        shd.degassing_vacuum,
        shd.degassing_duration,
        shd.casting_temp,
        shd.hydris,
        shd.nitrogen,
        shd.oxygen,
        shd.sent_to_ladle,
        shd.number_of_prime_blooms,
        shd.number_of_co_blooms,
        shd.number_of_rejected_blooms,
        shd.total_cast_wt,
        shd.heat_remark,
        COALESCE(bd.remark, '') AS reason_for_rejection,
        sd.make_of_hydris_probe,
        CAST(shd.is_hydrogen_bw_80_and_100 AS CHAR) AS is_hydris_measured_bw80_to_100m_of_casting,
        CAST(shd.is_probe_dipped AS CHAR) AS is_probe_dipped_below300mm_from_slag_metal_interface,
        sd.make_of_casting_powder,
        CAST(sd.is_ems_functioning AS CHAR) AS is_ems_functioning,
        CAST(sd.is_slag_detector_functioning AS CHAR) AS is_slag_detector_functioning,
        CAST(sd.is_amlc_functioning AS CHAR) AS is_amlc_functioning,
        CAST(sd.is_hydrogen_measurement_automatic AS CHAR) AS is_hydrogen_measurement_automatic,
        CAST(sd.is_ladle_to_tundish_used AS CHAR) AS is_ladle_to_tundish_used,
        CAST(sd.is_tundish_to_mould_used AS CHAR) AS is_tundish_to_mould_used
    FROM sms_duty sd
    JOIN duty_heat_sms2 dh ON sd.duty_id = dh.duty_id
    JOIN heat_detail_sms2 shd ON dh.heat_number = shd.heat_number
    LEFT JOIN bloom_detail_sms2 bd ON shd.heat_number = bd.cast_number
    WHERE sd.date BETWEEN :startDate AND :endDate
    AND sd.sms = 'SMS 2'

    UNION ALL

    SELECT 
        shd.heat_number,
        sd.sms,
        shd.caster_number,
        sd.rail_grade,
        CONCAT(sd.date, ' ', sd.shift) AS date_and_shift_of_casting,
        shd.sequence_number,
        shd.turn_down_temp,
        shd.degassing_vacuum,
        shd.degassing_duration,
        shd.casting_temp,
        shd.hydris,
        shd.nitrogen,
        shd.oxygen,
        shd.sent_to_ladle,
        shd.number_of_prime_blooms,
        shd.number_of_co_blooms,
        shd.number_of_rejected_blooms,
        shd.total_cast_wt,
        shd.heat_remark,
        COALESCE(bd.remark, '') AS reason_for_rejection,
        sd.make_of_hydris_probe,
        CAST(shd.is_hydrogen_bw_80_and_100 AS CHAR) AS is_hydris_measured_bw80_to_100m_of_casting,
        CAST(shd.is_probe_dipped AS CHAR) AS is_probe_dipped_below300mm_from_slag_metal_interface,
        sd.make_of_casting_powder,
        CAST(sd.is_ems_functioning AS CHAR) AS is_ems_functioning,
        CAST(sd.is_slag_detector_functioning AS CHAR) AS is_slag_detector_functioning,
        CAST(sd.is_amlc_functioning AS CHAR) AS is_amlc_functioning,
        CAST(sd.is_hydrogen_measurement_automatic AS CHAR) AS is_hydrogen_measurement_automatic,
        CAST(sd.is_ladle_to_tundish_used AS CHAR) AS is_ladle_to_tundish_used,
        CAST(sd.is_tundish_to_mould_used AS CHAR) AS is_tundish_to_mould_used
    FROM sms_duty sd
    JOIN duty_heat_sms3 dh ON sd.duty_id = dh.duty_id
    JOIN heat_detail_sms3 shd ON dh.heat_number = shd.heat_number
    LEFT JOIN bloom_detail_sms3 bd ON shd.heat_number = bd.cast_number
    WHERE sd.date BETWEEN :startDate AND :endDate
    AND sd.sms = 'SMS 3'
) combined_data
ORDER BY heat_number
""", nativeQuery = true)
List<Object[]> getHeatReport(@Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);


}
