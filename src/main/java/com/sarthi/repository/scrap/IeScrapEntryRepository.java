package com.sarthi.repository.scrap;

import com.sarthi.entity.scrap.IeScrapEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IeScrapEntryRepository extends JpaRepository<IeScrapEntry, Integer> {

    List<IeScrapEntry> findByPoiCodeAndStatusOrderByCreatedDateDesc(String poiCode, String status);

    List<IeScrapEntry> findByPoiCodeAndUnitLineShedNoAndStatusOrderByCreatedDateDesc(
            String poiCode, String unitLineShedNo, String status);

    @Query("SELECT COALESCE(SUM(e.noOfPiecesScrapped), 0) FROM IeScrapEntry e " +
           "WHERE e.poiCode = :poiCode AND e.unitLineShedNo = :unitLineShedNo AND e.status = 'ACTIVE'")
    Integer sumScrappedQty(@Param("poiCode") String poiCode,
                           @Param("unitLineShedNo") String unitLineShedNo);

    @Query("SELECT COALESCE(SUM(e.noOfPiecesScrapped), 0) FROM IeScrapEntry e " +
           "WHERE e.poiCode = :poiCode AND e.status = 'ACTIVE' AND e.scrappingDate <= :toDate")
    Integer sumScrappedQtyByPoiUpToDate(@Param("poiCode") String poiCode,
                                        @Param("toDate") LocalDate toDate);

    @Query("SELECT COUNT(e) FROM IeScrapEntry e WHERE e.scrappingDate = CURRENT_DATE")
    Long countTodayEntries();

    boolean existsByScrapVerificationId(String scrapVerificationId);



    @Query(value = """
        SELECT poi_code AS poiCode,
               COALESCE(SUM(no_of_pieces_scrapped), 0) AS totalScrapped,
               MAX(scrapping_date) AS lastScrappingDate
        FROM ie_scrap_entry
        WHERE status = 'ACTIVE'
          AND poi_code IS NOT NULL
        GROUP BY poi_code
    """, nativeQuery = true)
    List<Object[]> sumScrappedGroupByPoi();

}
