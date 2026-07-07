package com.sarthi.service.Impl;

import com.sarthi.entity.CricsPos.CrisSyncStatus;
import com.sarthi.repository.CrisSyncStatusRepository;
import com.sarthi.service.Impl.CrisService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CrisPoAsyncService {

    @Autowired
    private CrisPoListService listService;
    @Autowired
    private CrisPoDetailsService detailsService;
    @Autowired
    private PoPersistService persistService;
    @Autowired
    private CrisPoMaDetailsService maDetailsService;
    @Autowired
    private CrisPoMaListService maListService;
    @Autowired
    private PoMaPersistService maPersistService;
    @Autowired
    private CrisSyncStatusRepository  statusRepo;
    @Autowired
    private CrisAmendedPoListService amendedPoListService;
    @Autowired
    private CrisAmendedPoDetailsService amendedPoDetailsService;
    @Autowired
    private AmendedPoPersistService amendedPoPersistService;

    @Autowired
    private CrisPoCancellationListService cancellationListService;

    @Autowired
    private CrisPoCancellationDetailsService cancellationDetailsService;

    @Autowired
    private PoCancellationPersistService cancellationPersistService;




    @Async("poSyncExecutor")
    public void syncPos(String date) {

        List<Map<String, String>> poList = listService.getPoList(date);

        System.out.println(poList);

        for (Map<String, String> po : poList) {
            try {

                if (statusRepo.existsByRefTypeAndRefKey("PO", po.get("POKEY"))) {
                    continue;
                }

                CrisSyncStatus status = new CrisSyncStatus();
                status.setRefType("PO");
                status.setRefKey(po.get("POKEY"));
                status.setRly(po.get("RLY"));
                status.setStatus("FETCHED");
                status.setFetchedAt(LocalDateTime.now());
                statusRepo.save(status);
//                Map<String, Object> data =
//                        detailsService.getPoDetails(po.get("RLY"), po.get("POKEY"));
//
//                System.out.print(data);

                try {
                    Map<String, Object> data =
                            detailsService.getPoDetails(po.get("RLY"), po.get("POKEY"));

                    persistService.savePo(data);

                    status.setStatus("SAVED");
                    status.setProcessedAt(LocalDateTime.now());
                    statusRepo.save(status);

                } catch (Exception e) {

                    status.setStatus("FAILED");
                    status.setErrorMessage(e.getMessage());
                    statusRepo.save(status);
                }
                Thread.sleep(300); // rate limit
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Async("poSyncExecutor")
    public void syncMa(String date) {

        List<Map<String, String>> maList = maListService.getMaList(date);

        for (Map<String, String> ma : maList) {
            try {
                if (statusRepo.existsByRefTypeAndRefKey("MA", ma.get("MAKEY"))) {
                    continue;
                }

                CrisSyncStatus status = new CrisSyncStatus();
                status.setRefType("MA");
                status.setRefKey( ma.get("MAKEY"));
                status.setRly( ma.get("RLY"));
                status.setStatus("FETCHED");
                status.setFetchedAt(LocalDateTime.now());
                statusRepo.save(status);
//                Map<String, Object> data =
//                        maDetailsService.getMaDetails(ma.get("RLY"), ma.get("MAKEY"));
//                maPersistService.saveMa(data);
                try {
                    Map<String, Object> data =
                            maDetailsService.getMaDetails(ma.get("RLY"), ma.get("MAKEY"));

                  //  maPersistService.saveMa(data);

                    status.setStatus("SAVED");
                    status.setProcessedAt(LocalDateTime.now());
                    statusRepo.save(status);

                } catch (Exception e) {

                    status.setStatus("FAILED");
                    status.setErrorMessage(e.getMessage());
                    statusRepo.save(status);
                }
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Async("poSyncExecutor")
    public void syncAmendedPo(String date) {

        List<Map<String, String>> list =
                amendedPoListService.getAmendedPoList(date);

        System.out.print(list);
        for (Map<String, String> po : list) {

//            if (statusRepo.existsByRefTypeAndRefKey("APO", po.get("POKEY")))
//                continue;

//            CrisSyncStatus status = new CrisSyncStatus();
//            status.setRefType("APO");
//            status.setRefKey(po.get("POKEY"));
//            status.setRly(po.get("RLY"));
//            status.setStatus("FETCHED");
//            status.setFetchedAt(LocalDateTime.now());
//            statusRepo.save(status);

            try {
                Map<String, Object>  data =
                        amendedPoDetailsService.getDetails(
                              //  po.get("RLY"),
                             //   po.get("POKEY")
                                "08","4664990"
                        );
                System.out.print("data"+ data);

                amendedPoPersistService.save(data);

//                status.setStatus("SAVED");
//                status.setProcessedAt(LocalDateTime.now());

            } catch (Exception e) {
//                status.setStatus("FAILED");
//                status.setErrorMessage(e.getMessage());
            }

          //  statusRepo.save(status);
        }
    }

    @Async("poSyncExecutor")
    public void syncPoCancellations(String date) {

        List<Map<String, String>> list =
                cancellationListService.getCancellationList(date);

        for (Map<String, String> c : list) {
            try {

                if (statusRepo.existsByRefTypeAndRefKey("CA", c.get("CAKEY")))
                    continue;

                CrisSyncStatus status = new CrisSyncStatus();
                status.setRefType("CA");
                status.setRefKey(c.get("CAKEY"));
                status.setRly(c.get("RLY"));
                status.setStatus("FETCHED");
                status.setFetchedAt(LocalDateTime.now());
                statusRepo.save(status);

                try {
                    Map<String, Object> data =
                            cancellationDetailsService.getDetails(
                                    c.get("RLY"),
                                    c.get("CAKEY")
                            );

                    cancellationPersistService.save(data);

                    status.setStatus("SAVED");
                    status.setProcessedAt(LocalDateTime.now());

                } catch (Exception e) {
                    status.setStatus("FAILED");
                    status.setErrorMessage(e.getMessage());
                }

                statusRepo.save(status);
                Thread.sleep(300);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
