package com.sarthi.Sleeper.repository.scrap;

import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerificationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SleeperScrapVerificationImageRepository extends JpaRepository<SleeperScrapVerificationImage, Long> {

    List<SleeperScrapVerificationImage> findByScrapVerificationIdOrderByIdAsc(Long scrapVerificationId);

    List<SleeperScrapVerificationImage> findByScrapVerificationIdInOrderByIdAsc(Collection<Long> scrapVerificationIds);

    List<SleeperScrapVerificationImage> findByScrapVerificationIdAndPhotoType(Long scrapVerificationId, String photoType);
}
