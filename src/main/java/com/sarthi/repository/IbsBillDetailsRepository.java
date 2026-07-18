package com.sarthi.repository;

import com.sarthi.entity.IBS.IbsBillDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IbsBillDetailsRepository extends JpaRepository<IbsBillDetails, Long> {
    boolean existsByBillNoAndCallSno(
            String billNo,
            Integer callSno
    );
}
