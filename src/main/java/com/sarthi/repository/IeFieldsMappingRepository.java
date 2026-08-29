package com.sarthi.repository;

import com.sarthi.entity.IEFieldsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IeFieldsMappingRepository extends JpaRepository<IEFieldsMapping, Long> {
    Optional<IEFieldsMapping> findByPinCodeAndProductAndStage(String pinCode, String product, String stage);

    boolean existsByPinCodeAndProduct(String pinCode, String product);

    Optional<IEFieldsMapping> findFirstByPinCodeAndProduct(String pinCode, String product);

    List<IEFieldsMapping> findByPinCodeInAndProduct(List<String> pinCodes, String product);

    @Query("""
SELECT m FROM IEFieldsMapping m
WHERE m.pinCode = :pinCode
  AND m.product = :product
  AND (
       m.stage = :stage
       OR m.stage LIKE %:stage%
  )
""")
    Optional<IEFieldsMapping> findByPinCodeProductAndStageMatch(
            String pinCode,
            String product,
            String stage
    );


    @Query("""
SELECT i
FROM IEFieldsMapping i
WHERE i.plantPincode = :plantPincode
AND i.product = :product
AND i.stage LIKE %:stage%
""")
    Optional<IEFieldsMapping> findByPlantPincodeAndProductAndStageMatch(
            @Param("plantPincode") String plantPincode,
            @Param("product") String product,
            @Param("stage") String stage
    );
}
