package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.BenchMouldLongAndStress.BMLongLineDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BMLongLineDetailsRepository extends JpaRepository<BMLongLineDetails, Long> {
    List<BMLongLineDetails> findByBmMasterId(Long id);

    void deleteByBmMasterId(Long id);

    @Query("SELECT b FROM BMLongLineDetails b WHERE b.gangNumber IN :gangNumbers")
    List<BMLongLineDetails> findByGangNumbers(@Param("gangNumbers") List<Integer> gangNumbers);
}
