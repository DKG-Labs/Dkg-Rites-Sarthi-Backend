package com.sarthi.repository;

import com.sarthi.entity.UdmRejection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UdmRejectionRepository extends JpaRepository<UdmRejection, Long> {

    Optional<UdmRejection> findByRejectionNumber(String rejectionNumber);

    List<UdmRejection> findByFetchedDate(LocalDate fetchedDate);

    List<UdmRejection> findByFetchedDateAndTypeOfRejectionIgnoreCase(LocalDate fetchedDate, String type);

    List<UdmRejection> findByFetchedDateAndVendorCodeContaining(LocalDate fetchedDate, String vendorCode);

    List<UdmRejection> findByFetchedDateAndPoNumberContaining(LocalDate fetchedDate, String poNumber);

}
