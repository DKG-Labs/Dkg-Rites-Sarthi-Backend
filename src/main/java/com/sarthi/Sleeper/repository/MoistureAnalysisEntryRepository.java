package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.MoistureAnalysisEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MoistureAnalysisEntryRepository extends JpaRepository<MoistureAnalysisEntry, Long> {

    @EntityGraph(attributePaths = {"sections"})
    List<MoistureAnalysisEntry> findAll();

    List<MoistureAnalysisEntry> findTop5ByOrderByIdDesc();

    // Paging a collection fetch would happen in memory, so page the ids first, then load those rows
    @org.springframework.data.jpa.repository.Query(
            value = "select e.id from MoistureAnalysisEntry e " +
                    "where (:plantId is null or lower(replace(replace(e.plantId, ':', ''), ' ', '')) = :plantId) " +
                    "and (:fromDate is null or e.entryDate >= :fromDate) " +
                    "order by e.id desc",
            countQuery = "select count(e) from MoistureAnalysisEntry e " +
                    "where (:plantId is null or lower(replace(replace(e.plantId, ':', ''), ' ', '')) = :plantId) " +
                    "and (:fromDate is null or e.entryDate >= :fromDate)")
    org.springframework.data.domain.Page<Long> findIdsForPage(
            @org.springframework.data.repository.query.Param("plantId") String plantId,
            @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDate fromDate,
            org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"sections"})
    List<MoistureAnalysisEntry> findByIdIn(Collection<Long> ids);

    // plantId must already be normalised (colons/spaces removed, lower-case) or null
    @EntityGraph(attributePaths = {"sections"})
    @org.springframework.data.jpa.repository.Query(
            "select e from MoistureAnalysisEntry e " +
            "where (:plantId is null or lower(replace(replace(e.plantId, ':', ''), ' ', '')) = :plantId) " +
            "and (:fromDate is null or e.entryDate >= :fromDate) " +
            "order by e.id asc")
    List<MoistureAnalysisEntry> findForList(
            @org.springframework.data.repository.query.Param("plantId") String plantId,
            @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDate fromDate);
}
