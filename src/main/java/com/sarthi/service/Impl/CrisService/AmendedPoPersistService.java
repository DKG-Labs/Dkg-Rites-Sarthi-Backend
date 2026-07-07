package com.sarthi.service.Impl.CrisService;

import com.sarthi.repository.AmendmentPoHeaderRepository;
import com.sarthi.repository.AmendmentPoItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AmendedPoPersistService {

    @Autowired
    private AmendmentPoHeaderRepository headerRepo;

    @Autowired
    private AmendmentPoItemRepository itemRepo;

    @Transactional
    public void save(Map<String, Object> data) {

        Map<String, Object> hdr = (Map<String, Object>) data.get("PoHdr");
        if (hdr == null) return;

        String pokey = hdr.get("POKEY").toString();

        if (headerRepo.existsByPoKey(pokey)) {
            return;
        }

        List<Map<String, Object>> dtls =
                (List<Map<String, Object>>) data.get("PoDtl");

      /*  AmendmentPoHeader h = new AmendmentPoHeader();
        h.setPokey(pokey);
        h.setPoNo((String) hdr.get("PO_NO"));
        h.setL5noPo((String) hdr.get("L5NO_PO"));
        h.setPoDate(parsePoDate(hdr.get("PO_DT")));
        h.setRecvDate(parseTs(hdr.get("RECV_DT")));
        h.setRlyCode((String) hdr.get("RLY_CD"));
        h.setRlyShortname((String) hdr.get("RLY_SHORTNAME"));
        h.setInspectingAgency((String) hdr.get("INSPECTING_AGENCY"));
        h.setPoStatus((String) hdr.get("PO_STATUS"));
        h.setItemCat((String) hdr.get("ITEM_CAT"));
        h.setItemCatDescr((String) hdr.get("ITEM_CAT_DESCR"));
        h.setPoPdfPath((String) hdr.get("PO_PDF_PATH"));

        headerRepo.save(h);*/
/*
        if (dtls != null) {
            for (Map<String, Object> m : dtls) {

                AmendmentPoItem item = new AmendmentPoItem();
                item.setHeader(h);
                h.getItems().add(item);

                item.setPlNo((String) m.get("PL_NO"));
                item.setItemSrno((String) m.get("ITEM_SRNO"));
                item.setItemDesc((String) m.get("ITEM_DESC"));
                item.setQty(toBD(m.get("QTY")));
                item.setRate(toBD(m.get("RATE")));
                item.setUom((String) m.get("UOM"));
                item.setValue(toBD(m.get("VALUE")));
                item.setDeliveryDate(parsePoDate(m.get("DELV_DT")));
                item.setExtDeliveryDate(parsePoDate(m.get("EXT_DELV_DT")));

                itemRepo.save(item);
            }*/
        //}
    }


 /*   private BigDecimal toBD(Object v) {
        return v == null ? null : new BigDecimal(v.toString());
    }

    private LocalDateTime parsePoDate(Object v) {
        if (v == null) return null;
        return LocalDateTime.parse(
                v.toString(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        );
    }

    private LocalDateTime parseTs(Object v) {
        if (v == null) return null;
        return LocalDateTime.parse(
                v.toString(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
        );
    }*/


}




