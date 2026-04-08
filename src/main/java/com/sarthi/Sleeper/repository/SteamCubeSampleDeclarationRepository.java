package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SteamCubeSampleDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SteamCubeSampleDeclarationRepository extends JpaRepository<SteamCubeSampleDeclaration, Long> {
    @Query("""
SELECT COUNT(s) > 0 
FROM SteamCubeSampleDeclaration s 
WHERE s.batchNo = :batchNo
""")
    boolean existsSteamCube(String batchNo);
}
