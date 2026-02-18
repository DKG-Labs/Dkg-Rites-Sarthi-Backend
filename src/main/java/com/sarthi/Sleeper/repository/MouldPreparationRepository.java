package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.MouldPreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MouldPreparationRepository extends JpaRepository<MouldPreparation, Long> {
}
