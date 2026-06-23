package com.sarthi.Sms.repository.sms.sequence;


import com.sarthi.Sms.entity.sms.sequence.DutySequenceEntity;
import com.sarthi.Sms.entity.sms.sequence.DutySequenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DutySequenceRepository extends JpaRepository<DutySequenceEntity, DutySequenceId> {
    DutySequenceEntity findByDutySequenceId(DutySequenceId dutySequenceId);
}
