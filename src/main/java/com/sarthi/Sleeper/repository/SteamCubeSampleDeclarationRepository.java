package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCubeSampleDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SteamCubeSampleDeclarationRepository extends JpaRepository<SteamCubeSampleDeclaration, Long> {
    @Query("""
SELECT COUNT(s) > 0 
FROM SteamCubeSampleDeclaration s 
WHERE s.batchNo = :batchNo
""")
    boolean existsSteamCube(String batchNo);



  /*  @Query("""
    SELECT s FROM SteamCubeSampleDeclaration s
    WHERE s.plantId = :plantId
    AND s.vendorCode = :vendorCode
    AND s.shift = :shift
    AND s.createdBy = :createdBy
    AND s.createdAt BETWEEN :start AND :end
""")
    List<SteamCubeSampleDeclaration> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime start,
            LocalDateTime end
    );*/
  @Query("""
    SELECT s FROM SteamCubeSampleDeclaration s
    WHERE s.plantId = :plantId
    AND s.vendorCode = :vendorCode
    AND s.shift = :shift
    AND s.createdBy = :createdBy
    AND s.createdAt BETWEEN :start AND :end
    AND (s.status IS NULL OR s.status <> 'COMPLETED')
""")
  List<SteamCubeSampleDeclaration> findByDate(
          String plantId,
          String vendorCode,
          String shift,
          int createdBy,
          LocalDateTime start,
          LocalDateTime end
  );

}
