package com.sarthi.Sleeper.repository.scrap;

import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerificationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SleeperScrapVerificationItemRepository extends JpaRepository<SleeperScrapVerificationItem, Long> {

    List<SleeperScrapVerificationItem> findByScrapVerificationIdAndActiveKeyIsNotNullOrderByIdAsc(Long scrapVerificationId);

    List<SleeperScrapVerificationItem> findByScrapVerificationIdInAndActiveKeyIsNotNull(Collection<Long> scrapVerificationIds);

    /** Puts every sleeper of a verification back in the pending basket (one statement) */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE sleeper_scrap_verification_item SET active_key = NULL
            WHERE scrap_verification_id = :verificationId AND active_key IS NOT NULL
            """, nativeQuery = true)
    int releaseAll(@Param("verificationId") Long verificationId);

    /** Puts the given items' sleepers back in the pending basket (one statement) */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE sleeper_scrap_verification_item SET active_key = NULL
            WHERE id IN (:ids) AND active_key IS NOT NULL
            """, nativeQuery = true)
    int releaseByIds(@Param("ids") Collection<Long> ids);
}
