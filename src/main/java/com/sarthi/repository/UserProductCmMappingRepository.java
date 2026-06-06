package com.sarthi.repository;

import com.sarthi.entity.UserProductCmMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductCmMappingRepository extends JpaRepository<UserProductCmMapping, Long> {
}
