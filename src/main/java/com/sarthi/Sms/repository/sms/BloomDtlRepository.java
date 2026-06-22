package com.sarthi.Sms.repository.sms;


import com.sarthi.Sms.entity.sms.BloomDtlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloomDtlRepository extends JpaRepository<BloomDtlEntity, String> {
    Optional<BloomDtlEntity> findByCastNo(String castNo);

    @Query(value = """
            SELECT
                shd.heat_number, shd.heat_stage, shd.heat_remark, shd.turn_down_temp,
                shd.degassing_vacuum, shd.degassing_duration, shd.casting_temp, shd.caster_number,
                shd.sequence_number, shd.hydris, shd.is_probe_dipped, shd.is_hydrogen_bw_80_and_100,
                shd.nitrogen, shd.oxygen, shd.number_of_prime_blooms, shd.prime_blooms_length,
                shd.prime_blooms_total_length, shd.number_of_co_blooms, shd.co_blooms_length,
                shd.co_blooms_total_length, shd.number_of_rejected_blooms, shd.rejected_blooms_length,
                shd.rejected_blooms_total_length, shd.weight_of_prime_blooms, shd.weight_of_co_blooms,
                shd.weight_of_rejected_blooms, shd.total_cast_wt, shd.is_diverted, shd.sent_to_ladle,
                bd.cast_number, bd.bloom_identification, bd.length_of_blooms, bd.surface_condition_of_blooms,
                bd.number_of_prime_blooms_rejected, bd.number_of_co_blooms_rejected, bd.remark, shd.turn_down_temp_wv, shd.degassing_vacuum_wv, shd.degassing_duration_wv
            FROM sms_heat_detail shd
            JOIN sms_duty_heat sdh ON shd.heat_number = sdh.heat_number
            JOIN sms_duty sd ON sdh.duty_id = sd.duty_id
            LEFT JOIN bloom_detail bd ON shd.heat_number = bd.cast_number
            WHERE sd.date = :date
            AND sd.shift = :shift
            AND sd.sms = :sms
            AND sd.rail_grade = :railGrade
            """, nativeQuery = true)
    List<Object[]> getHeatDtls(
            @Param("date") LocalDate date,
            @Param("shift") String shift,
            @Param("sms") String sms,
            @Param("railGrade") String railGrade);

    @Query(value = """
            SELECT
                shd.heat_number,
                shd.sequence_number,
                shd.nitrogen,
                shd.oxygen,
                shd.hydris,
                shd.degassing_vacuum,
                shd.degassing_duration,
                shd.heat_remark
            FROM sms_heat_detail shd
            JOIN sms_duty_heat sdh ON shd.heat_number = sdh.heat_number
            JOIN sms_duty sd ON sdh.duty_id = sd.duty_id
            WHERE sd.date = :date
            AND sd.shift = :shift
            AND sd.sms = :sms
            """, nativeQuery = true)
    List<Object[]> getSmsChemicalAnalysis(@Param("date") LocalDate date,
            @Param("shift") String shift,
            @Param("sms") String sms);
}
