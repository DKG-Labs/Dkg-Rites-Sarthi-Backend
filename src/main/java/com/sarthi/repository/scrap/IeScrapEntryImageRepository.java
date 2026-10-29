package com.sarthi.repository.scrap;

import com.sarthi.entity.scrap.IeScrapEntryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IeScrapEntryImageRepository extends JpaRepository<IeScrapEntryImage, Integer> {

    List<IeScrapEntryImage> findByScrapEntryId(Integer scrapEntryId);

    List<IeScrapEntryImage> findByScrapEntryIdIn(List<Integer> scrapEntryIds);

    void deleteByScrapEntryId(Integer scrapEntryId);
}
