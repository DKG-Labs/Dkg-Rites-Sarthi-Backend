package com.sarthi.udmcomplaint.repository;

import com.sarthi.udmcomplaint.entity.UdmComplaint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/** Read-only access to udm_complaint_dropdown_master (SRS dropdown values). */
@org.springframework.stereotype.Repository
public interface UdmComplaintDropdownRepository extends Repository<UdmComplaint, Long> {

    /** [0] category, [1] value — active rows in display order. */
    @Query(value = """
        SELECT category, value FROM udm_complaint_dropdown_master
        WHERE is_active = 1
        ORDER BY category, sort_order, value
        """, nativeQuery = true)
    List<Object[]> findActiveValues();
}
