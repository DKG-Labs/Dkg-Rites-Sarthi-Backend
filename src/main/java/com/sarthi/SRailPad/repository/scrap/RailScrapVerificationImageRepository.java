package com.sarthi.SRailPad.repository.scrap;

import com.sarthi.SRailPad.entity.scrap.RailScrapVerificationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RailScrapVerificationImageRepository extends JpaRepository<RailScrapVerificationImage, Long> {

    List<RailScrapVerificationImage> findByScrapVerificationIdOrderByIdAsc(Long scrapVerificationId);

    List<RailScrapVerificationImage> findByScrapVerificationIdIn(Collection<Long> scrapVerificationIds);

    List<RailScrapVerificationImage> findByScrapVerificationIdAndPhotoType(Long scrapVerificationId, String photoType);
}
