package com.sarthi.repository;

import com.sarthi.entity.InspectionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionImageRepository extends JpaRepository<InspectionImage, Long> {

    List<InspectionImage> findByInspectionCallNoAndTypeOfCall(String inspectionCallNo, String typeOfCall);

    List<InspectionImage> findByInspectionCallNo(String inspectionCallNo);

    void deleteByInspectionCallNoAndTypeOfCall(String inspectionCallNo, String typeOfCall);

    List<InspectionImage> findByInspectionCallNoAndTypeOfCallAndShiftAndDateOfInspection(
            String inspectionCallNo, String typeOfCall, String shift, String dateOfInspection);
}
