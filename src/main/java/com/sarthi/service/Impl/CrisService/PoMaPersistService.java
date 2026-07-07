package com.sarthi.service.Impl.CrisService;

import com.sarthi.entity.CricsPos.PoMaDetail;
import com.sarthi.entity.CricsPos.PoMaHeader;
import com.sarthi.repository.PoMaDetailRepository;
import com.sarthi.repository.PoMaHeaderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PoMaPersistService {

    @Autowired
    private PoMaHeaderRepository headerRepo;
    @Autowired
    private PoMaDetailRepository detailRepo;

  /*  public void saveMa(Map<String, Object> data) {

        Map<String, Object> hdr =
                (Map<String, Object>) data.get("MMP_POMA_HDR");

        List<Map<String, Object>> dtls =
                (List<Map<String, Object>>) data.get("MMP_POMA_DTL");

        String maKey = (String) hdr.get("MAKEY");

        if (headerRepo.existsByMaKey(maKey)) return;

        PoMaHeader h = new PoMaHeader();
        h.setMaKey(maKey);
        h.setRly((String) hdr.get("RLY"));
        h.setPoKey((String) hdr.get("POKEY"));
        h.setPoNo((String) hdr.get("PO_NO"));
        h.setMaNo((String) hdr.get("MA_NO"));
        h.setSubject((String) hdr.get("SUBJECT"));
       // h.setOldPoValue(new BigDecimal(hdr.get("OLD_PO_VALUE").toString()));
       // h.setNewPoValue(new BigDecimal(hdr.get("NEW_PO_VALUE").toString()));

        headerRepo.save(h);

        if (dtls != null) {
            for (Map<String, Object> m : dtls) {
                PoMaDetail d = new PoMaDetail();
                d.setMaHeader(h);
                d.setRly((String) m.get("RLY"));
                d.setSlno((String) m.get("SLNO"));
                d.setMaFld((String) m.get("MA_FLD"));
                d.setMaFldDescr((String) m.get("MA_FLD_DESCR"));
                d.setOldValue((String) m.get("OLD_VALUE"));
                d.setNewValue((String) m.get("NEW_VALUE"));
                d.setPoSr((String) m.get("PO_SR"));
                detailRepo.save(d);
            }
        }
    }*/
}

