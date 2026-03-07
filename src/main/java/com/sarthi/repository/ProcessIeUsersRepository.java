package com.sarthi.repository;

import com.sarthi.entity.ProcessIeUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessIeUsersRepository extends JpaRepository<ProcessIeUsers, Long> {
    Optional<ProcessIeUsers> findByIeUserId(Long ieUserId);

    List<ProcessIeUsers> findAllByProcessUserId(Integer processIeUserId);

 /*   @Query("""
SELECT piu.ieUserId
FROM ProcessIeUsers piu
JOIN IePoiMapping ipm
     ON ipm.ieUserId = piu.ieUserId
WHERE piu.processUserId = :processIeUserId
AND ipm.poiCode = :poiCode
""")
    List<Long> findIeUsersByProcessIeAndPoi(
            @Param("processIeUserId") Integer processIeUserId,
            @Param("poiCode") String poiCode
    );*/
 @Query(value = """
SELECT DISTINCT user_id
FROM (
        SELECT ipm.ie_user_id AS user_id
        FROM ie_poi_mapping ipm
        WHERE ipm.poi_code = :poiCode

        UNION

        SELECT piu.process_user_id AS user_id
        FROM process_ie_users piu
        WHERE piu.ie_user_id IN (
                SELECT ie_user_id
                FROM ie_poi_mapping
                WHERE poi_code = :poiCode
        )

        UNION

        SELECT piu.ie_user_id AS user_id
        FROM process_ie_users piu
        WHERE piu.process_user_id = :processIeUserId
) x
""", nativeQuery = true)
 List<Long> findIeUsersByProcessIeAndPoi(
         @Param("processIeUserId") Integer processIeUserId,
         @Param("poiCode") String poiCode
 );

    Optional<ProcessIeUsers>
    findTopByIeUserIdOrderByCreatedDateDesc(Long ieUserId);
}
