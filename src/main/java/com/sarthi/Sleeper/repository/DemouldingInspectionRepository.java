package com.sarthi.Sleeper.repository;



import com.sarthi.Sleeper.entity.DemouldingInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemouldingInspectionRepository extends JpaRepository<DemouldingInspection, Long> {
}
