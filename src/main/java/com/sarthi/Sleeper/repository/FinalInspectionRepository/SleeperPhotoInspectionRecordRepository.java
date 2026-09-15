package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperPhotoInspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SleeperPhotoInspectionRecordRepository extends JpaRepository<SleeperPhotoInspectionRecord, Long> {

    List<SleeperPhotoInspectionRecord> findByInspectionCallNoAndTypeOfCall(String inspectionCallNo, String typeOfCall);

    List<SleeperPhotoInspectionRecord> findByInspectionCallNo(String inspectionCallNo);

    List<SleeperPhotoInspectionRecord> findByInspectionCallNoAndTypeOfCallAndShiftAndDateOfInspection(
            String inspectionCallNo, String typeOfCall, String shift, String dateOfInspection
    );
}
