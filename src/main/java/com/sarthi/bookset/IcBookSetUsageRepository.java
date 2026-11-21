package com.sarthi.bookset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IcBookSetUsageRepository extends JpaRepository<IcBookSetUsage, Long> {

    Optional<IcBookSetUsage> findByBookNoAndSetNo(String bookNo, String setNo);

    List<IcBookSetUsage> findByIcKey(String icKey);
}
