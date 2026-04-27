package com.sarthi.service.Impl;

import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.dto.crisDtos.*;
import com.sarthi.entity.*;
import com.sarthi.entity.CricsPos.*;
import com.sarthi.repository.*;
import com.sarthi.service.UserService;
import com.sarthi.service.crisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@Service
public class crisServiceImpl implements crisService {

    @Autowired
    private CrisAuthServic authService;

    @Autowired
    private org.springframework.web.client.RestTemplate crisRestTemplate;

    @org.springframework.beans.factory.annotation.Value("${cris.base-url}")
    private String crisBaseUrl;

    @Autowired
    private PoHeaderRepository headerRepo;
    @Autowired
    private PoItemRepository itemRepo;
    @Autowired
    private VendorMasterRepository vendorMasterRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMasterRepository userMasterRepository;
    @Autowired
    private RoleMasterRepository roleMasterRepository;
    @Autowired
    private UserRoleMasterRepository userRoleMasterRepository;
    @Autowired
    private PoMaHeaderRepository  poMaHeaderRepository;
    @Autowired
    private PoMaDetailRepository poMaDetailRepository;
    @Autowired
    private PoCancellationHeaderRepository poCancellationHeaderRepo;

    @Autowired
    private PoCancellationDetailRepository poCancellationDetailRepo;

    @Autowired
    private CrisSyncStatusRepository statusRepo;
    @Transactional
    public void savePoFromFrontend(PoRequestDto request) {

        PoHeaderDto hdr = request.getPoHdr();
        List<PoItemDto> items = request.getPoDtl();

        // Validation
        if (hdr == null) {
            throw new RuntimeException("PoHdr missing");
        }

        // Validation
        if (hdr.getPOKEY() == null || hdr.getPOKEY().isBlank()) {
            throw new RuntimeException("POKEY is mandatory");
        }

        String poKey = hdr.getPOKEY();
        String rly = hdr.getRLY_CD();

        // Check already processed (STATUS TABLE)
        if (statusRepo.existsByRefTypeAndRefKey("PO", poKey)) {
            throw new RuntimeException("PO already processed: " + poKey);
        }

        // Create status (FETCHED)
        CrisSyncStatus status = new CrisSyncStatus();
        status.setRefType("PO");
        status.setRefKey(poKey);
        status.setRly(rly);
        status.setStatus("FETCHED");
        status.setFetchedAt(LocalDateTime.now());

        statusRepo.save(status);

        try {

            //  Duplicate check in main table
            if (headerRepo.existsByPoKey(poKey)) {
                throw new RuntimeException("PO already exists in DB: " + poKey);
            }

            //  Vendor Code cleanup
            String vendorCode = hdr.getIMMS_VENDOR_CODE();
            if (vendorCode != null) {
              //  vendorCode = vendorCode.replace(":", "");
                hdr.setIMMS_VENDOR_CODE(vendorCode);
            }

            //  Vendor + User creation
            createVendorIfNotExists(hdr);

            //  Save Header
            PoHeader header = buildPoHeaderFromDto(hdr);
            header.setSourceSystem("FRONTEND");
            headerRepo.save(header);

            // Item validation
            if (items == null || items.isEmpty()) {
                throw new RuntimeException("PoDtl cannot be empty");
            }

            for (PoItemDto m : items) {

                if (m.getITEM_SRNO() == null) {
                    throw new RuntimeException("ITEM_SRNO missing");
                }

                if (m.getPL_NO() == null) {
                    throw new RuntimeException("PL_NO missing");
                }

                PoItem item = buildPoItemFromDto(m, header);
                item.setSourceSystem("FRONTEND");
                itemRepo.save(item);
            }

            // SUCCESS STATUS
            status.setStatus("SAVED");
            status.setProcessedAt(LocalDateTime.now());
            statusRepo.save(status);

        } catch (Exception e) {

            // FAILURE STATUS
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            statusRepo.save(status);

            throw e; // optional (to send error to API)
        }
    }


    private PoHeader buildPoHeaderFromDto(PoHeaderDto m) {

        PoHeader h = new PoHeader();

        h.setPoKey(m.getPOKEY());
        h.setPoNo(m.getPO_NO());
        h.setL5PoNo(m.getL5NO_PO());

        h.setRlyCd(m.getRLY_CD());
        h.setRlyShortName(m.getRLY_SHORTNAME());

        h.setPurchaserCode(m.getIMMS_PURCHASER_CODE());
        h.setPurchaserDetail(m.getIMMS_PURCHASER_DETAIL());

       // h.setVendorCode(m.getIMMS_VENDOR_CODE());

        String vendor = m.getIMMS_VENDOR_CODE();

        if (vendor != null && !vendor.startsWith(":")) {
            vendor = ":" + vendor;
        }

        h.setVendorCode(vendor);
        h.setVendorDetails(m.getVENDOR_DETAILS());
        h.setFirmDetails(m.getFIRM_DETAILS());

        h.setStockNonStock(m.getSTOCK_NONSTOCK());
        h.setRlyNonRly(m.getRLY_NONRLY());
        h.setPoOrLetter(m.getPO_OR_LETTER());

        h.setPoStatus(m.getPO_STATUS());
        h.setInspectingAgency(m.getINSPECTING_AGENCY());
        h.setPdfPath(m.getPO_PDF_PATH());

        h.setRegionCode(m.getREGION_CODE());
        h.setRemarks(m.getREMARKS());

        h.setBillPayOff(m.getBILL_PAY_OFF());
        h.setBillPayOffName(m.getBILL_PAY_OFF_NAME());

        h.setPoiCd(m.getPOI_CD());

        h.setItemCat(m.getITEM_CAT());
        h.setItemCatDescr(m.getITEM_CAT_DESCR());

        if (m.getPO_DT() != null)
            h.setPoDate(LocalDateTime.parse(m.getPO_DT(), PO_DT_FMT));

        if (m.getRECV_DT() != null)
            h.setReceivedDate(LocalDateTime.parse(m.getRECV_DT(), TS_FMT));

        return h;
    }


    private PoItem buildPoItemFromDto(PoItemDto m, PoHeader header) {

        PoItem i = new PoItem();
        i.setPoHeader(header);

        i.setCaseNo(m.getPOKEY());
        i.setItemSrNo(m.getITEM_SRNO());
        i.setPlNo(m.getPL_NO());
        i.setItemDesc(m.getITEM_DESC());

        i.setConsigneeCd(m.getCONSIGNEE_CD());
        i.setImmsConsigneeCd(m.getIMMS_CONSIGNEE_CD());
        i.setImmsConsigneeName(m.getIMMS_CONSIGNEE_NAME());
        i.setConsigneeDetail(m.getCONSIGNEE_DETAIL());

        i.setConsigneeRly(m.getCONSIGNEE_RLY());
        i.setConsigneeRlyShortName(m.getCONSIGNEE_RLY_SHORTNAME());

        i.setPRly(m.getP_RLY());

        i.setBillPayOff(m.getBILL_PAY_OFF());
        i.setBillPayOffDesc(m.getBILL_PAY_OFF_DESC());
        i.setBillPassOff(m.getBILL_PASS_OFF());

        i.setUomCd(m.getUOM_CD());
        i.setUom(m.getUOM());

        if (m.getQTY() != null)
            i.setQty(new BigDecimal(m.getQTY()).setScale(0, java.math.RoundingMode.HALF_UP).intValue());

        if (m.getQTY_CANCELLED() != null)
            i.setQtyCancelled(new BigDecimal(m.getQTY_CANCELLED()).setScale(0, java.math.RoundingMode.HALF_UP).intValue());

        i.setRate(bd(m.getRATE()));
        i.setBasicValue(bd(m.getBASIC_VALUE()));
        i.setSalesTaxPercent(bd(m.getSALES_TAX_PER()));
        i.setSalesTax(bd(m.getSALES_TAX()));
        i.setDiscountType(m.getDISCOUNT_TYPE());
        i.setDiscountPercent(bd(m.getDISCOUNT_PER()));
        i.setDiscount(bd(m.getDISCOUNT()));
        i.setValue(bd(m.getVALUE()));

        if (m.getDELV_DT() != null)
            i.setDeliveryDate(LocalDateTime.parse(m.getDELV_DT(), PO_DT_FMT));

        if (m.getEXT_DELV_DT() != null)
            i.setExtendedDeliveryDate(LocalDateTime.parse(m.getEXT_DELV_DT(), PO_DT_FMT));

        if (m.getDATETIME() != null)
            i.setCrisTimestamp(LocalDateTime.parse(m.getDATETIME(), TS_FMT));

        i.setAllocation(m.getALLOCATION());
        i.setUserId(m.getUSER_ID());

        return i;
    }
    private static final DateTimeFormatter PO_DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
//    private static final DateTimeFormatter TS_FMT =
//            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

    private static final DateTimeFormatter TS_FMT =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .optionalStart()
                    .appendPattern(".S")
                    .optionalEnd()
                    .toFormatter();

    private BigDecimal bd(Object o) {
        return o == null ? null : new BigDecimal(o.toString());
    }


    private String getVendorRole(String itemCatDescr) {
        if (itemCatDescr == null) return "Vendor";

        switch (itemCatDescr.trim()) {
            case "Elastic Rail Clips":
                return "ERC Vendor";
            case "PSC Mainline Sleeper":
                return "Sleeper Vendor";
            case "Rail Pads":
                return "Rail Pad Vendor";
            default:
                return "Vendor";
        }
    }

    @Transactional
    public void createVendorIfNotExists(PoHeaderDto hdr) {

        //  Clean vendor code
        String vendorCode = hdr.getIMMS_VENDOR_CODE();
        if (vendorCode != null) {
            vendorCode = vendorCode.replace(":", "");
            hdr.setIMMS_VENDOR_CODE(vendorCode);
        }

        String vendorDetails = hdr.getVENDOR_DETAILS();
        String firmDetails = hdr.getFIRM_DETAILS();

        //  Validation
        if (vendorCode == null || vendorCode.isBlank()) return;

        //  Check Vendor exists
        Optional<VendorMaster> existing =
                vendorMasterRepository.findByVendorCode(vendorCode);

        if (existing.isPresent()) return;

        //  Save Vendor
        VendorMaster vendor = new VendorMaster();
        vendor.setVendorCode(vendorCode);
        vendor.setVendorName(firmDetails);
        vendor.setVendorDetails(vendorDetails);
        vendor.setCreatedDate(LocalDateTime.now());

        vendorMasterRepository.save(vendor);

        //  If user already exists → stop
        if (userMasterRepository.existsByUserName(vendorCode)) {
            return;
        }

        //  Determine Role
        String role = getVendorRole(hdr.getITEM_CAT_DESCR());

        //  Create User
        userRequestDto dto = new userRequestDto();
        dto.setUserName(vendorCode);
        dto.setPassword("Vendor@123"); // better default
        dto.setEmail(vendorCode + "@vendor.local");
        dto.setMobileNumber(null);
        dto.setRoleNames(List.of(role));

        dto.setCreatedBy("CRIS");
        dto.setEmployeeId(null);
        dto.setClusterName(null);
        dto.setRegionName(null);
        dto.setPriority(null);
        dto.setIeUserIds(null);

        userService.createUser(dto);

        //  Fetch user
        Optional<UserMaster> userOpt =
                userMasterRepository.findByUserName(vendorCode);

        if (userOpt.isEmpty()) return;

        Integer userId = userOpt.get().getUserId();

        //  Fetch role
        Optional<RoleMaster> roleOpt =
                roleMasterRepository.findByRoleName(role);

        if (roleOpt.isEmpty()) return; // avoid NPE

        RoleMaster roleEntity = roleOpt.get();

        //  Check mapping exists
        boolean existsMapping =
                userRoleMasterRepository.existsByUserIdAndRoleId(
                        userId, roleEntity.getRoleId()
                );

        //  Save mapping
        if (!existsMapping) {
            UserRoleMaster urm = new UserRoleMaster();
            urm.setUserId(userId);
            urm.setRoleId(roleEntity.getRoleId());
            urm.setCreatedBy("CRIS");

            userRoleMasterRepository.save(urm);
        }
    }

    @Transactional
    public void saveMaFromFrontend(MaRequestDto request) {

        MaHeaderDto hdr = request.getMMP_POMA_HDR();
        List<MaDetailDto> dtls = request.getMMP_POMA_DTL();

        if (hdr == null) throw new RuntimeException("Header missing");
        if (dtls == null || dtls.isEmpty()) throw new RuntimeException("Details missing");

        String maKey = hdr.getMAKEY();
        if (maKey == null || maKey.isBlank())
            throw new RuntimeException("MAKEY is mandatory");

        if (statusRepo.existsByRefTypeAndRefKey("MA", maKey)) {
            throw new RuntimeException("Already processed");
        }

        CrisSyncStatus status = new CrisSyncStatus();
        status.setRefType("MA");
        status.setRefKey(maKey);
        status.setRly(hdr.getRLY());
        status.setStatus("FETCHED");
        status.setFetchedAt(LocalDateTime.now());

        statusRepo.save(status);

        try {

            if (poMaHeaderRepository.existsByMaKey(maKey)) {
                throw new RuntimeException("Already exists in DB");
            }

            PoMaHeader h = new PoMaHeader();


            h.setMaKey(maKey);
            h.setRly(hdr.getRLY());
            h.setPoKey(hdr.getPOKEY());
            h.setPoNo(hdr.getPO_NO());
            h.setMaNo(hdr.getMA_NO());
            h.setSubject(hdr.getSUBJECT());


            h.setMaType(hdr.getMA_TYPE());
            h.setVcode(hdr.getVCODE());
            h.setRemarks(hdr.getREMARKS());
            h.setMaSignOff(hdr.getMA_SIGN_OFF());
            h.setStatus(hdr.getSTATUS());
            h.setPurDiv(hdr.getPUR_DIV());
            h.setPurSec(hdr.getPUR_SEC());
            h.setPoMaSrno(hdr.getPO_MA_SRNO());
            h.setPublishFlag(hdr.getPUBLISH_FLAG());


            h.setRefNo(hdr.getREF_NO());
            h.setRequestId(hdr.getREQUEST_ID());

            h.setAuthSeq(hdr.getAUTH_SEQ());
            h.setAuthSeqFin(hdr.getAUTH_SEQ_FIN());

            h.setCurUser(hdr.getCURUSER());
            h.setCurUserInd(hdr.getCURUSER_IND());

            h.setSignId(hdr.getSIGN_ID());
            h.setReqId(hdr.getREQ_ID());

            h.setRecInd(hdr.getREC_IND());
            h.setFlag(hdr.getFLAG());
            h.setReqFlag(hdr.getREQ_FLAG());


            if (hdr.getMA_DATE() != null && !hdr.getMA_DATE().isBlank())
                h.setMaDate(LocalDate.parse(hdr.getMA_DATE()));

            if (hdr.getMAKEY_DATE() != null && !hdr.getMAKEY_DATE().isBlank())
                h.setMaKeyDate(LocalDate.parse(hdr.getMAKEY_DATE()));

            if (hdr.getREF_DATE() != null && !hdr.getREF_DATE().isBlank())
                h.setRefDate(LocalDate.parse(hdr.getREF_DATE()));


            if (hdr.getOLD_PO_VALUE() != null && !hdr.getOLD_PO_VALUE().isBlank())
                h.setOldPoValue(new BigDecimal(hdr.getOLD_PO_VALUE()));

            if (hdr.getNEW_PO_VALUE() != null && !hdr.getNEW_PO_VALUE().isBlank())
                h.setNewPoValue(new BigDecimal(hdr.getNEW_PO_VALUE()));

            // SAVE HEADER
            poMaHeaderRepository.save(h);


            List<PoMaDetail> detailList = new ArrayList<>();

            for (MaDetailDto m : dtls) {

                PoMaDetail d = new PoMaDetail();

                d.setMaHeader(h);
                d.setMaKey(m.getMAKEY());
                d.setRly(m.getRLY());
                d.setSlno(m.getSLNO());
                d.setMaFld(m.getMA_FLD());
                d.setMaFldDescr(m.getMA_FLD_DESCR());
                d.setOldValue(m.getOLD_VALUE());
                d.setNewValue(m.getNEW_VALUE());

                d.setNewValueInd(m.getNEW_VALUE_IND());
                d.setNewValueFlag(m.getNEW_VALUE_FLAG());
                d.setPlNo(m.getPL_NO());
                d.setPoSr(m.getPO_SR());

                d.setCondSlno(m.getCOND_SLNO());
                d.setCondCode(m.getCOND_CODE());
                d.setMaSrNo(m.getMA_SR_NO());
                d.setStatus(m.getSTATUS());

                d.setExpSr(m.getEXP_SR());
                d.setExpCode(m.getEXP_CODE());
                d.setCondNo(m.getCOND_NO());

                if (m.getORIG_DP() != null && !m.getORIG_DP().isBlank())
                    d.setOrigDp(LocalDate.parse(m.getORIG_DP()));

                d.setPaymentYear(m.getPAYMENT_YEAR());
                d.setNewPosrData(m.getNEW_POSR_DATA());
                d.setRefPono(m.getREF_PONO());
                d.setConsigneeRly(m.getCONSIGNEE_RLY());

                detailList.add(d);
            }

            poMaDetailRepository.saveAll(detailList);

            status.setStatus("SAVED");
            status.setProcessedAt(LocalDateTime.now());
            statusRepo.save(status);

        } catch (Exception e) {

            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            statusRepo.save(status);

            throw e;
        }
    }

    @Transactional
    public void savePoCancellationFromFrontend(PoCancellationRequestDto request) {

        PoCancellationHeaderDto hdr = request.getHeader();
        List<PoCancellationDetailDto> dtls = request.getDetails();


        if (hdr == null) throw new RuntimeException("Header missing");
        if (dtls == null || dtls.isEmpty()) throw new RuntimeException("Details missing");

        String caKey = hdr.getCakey();
        if (caKey == null || caKey.isBlank())
            throw new RuntimeException("CAKEY is mandatory");


        if (statusRepo.existsByRefTypeAndRefKey("PO_CA", caKey)) {
            throw new RuntimeException("Already processed");
        }

        CrisSyncStatus status = new CrisSyncStatus();
        status.setRefType("PO_CA");
        status.setRefKey(caKey);
        status.setRly(hdr.getRly());
        status.setStatus("FETCHED");
        status.setFetchedAt(LocalDateTime.now());

        statusRepo.save(status);

        try {


            if (poCancellationHeaderRepo.existsByCakey(caKey)) {
                throw new RuntimeException("Already exists in DB");
            }

            PoCancellationHeader h = new PoCancellationHeader();


            h.setCakey(caKey);
            h.setRly(hdr.getRly());
            h.setPokey(hdr.getPokey());
            h.setPoNo(hdr.getPoNo());
            h.setCaNo(hdr.getCaNo());
            h.setCaType(hdr.getCaType());
            h.setVcode(hdr.getVcode());

            h.setRefNo(hdr.getRefNo());
            h.setRemarks(hdr.getRemarks());
            h.setCaSignOff(hdr.getCaSignOff());
            h.setStatus(hdr.getStatus());

            h.setPurDiv(hdr.getPurDiv());
            h.setPurSec(hdr.getPurSec());

            h.setRequestId(hdr.getRequestId());
            h.setAuthSeq(hdr.getAuthSeq());
            h.setAuthSeqFin(hdr.getAuthSeqFin());

            h.setCuruser(hdr.getCuruser());
            h.setCuruserInd(hdr.getCuruserInd());

            h.setSignId(hdr.getSignId());
            h.setReqId(hdr.getReqId());

            h.setFinStatus(hdr.getFinStatus());
            h.setRecInd(hdr.getRecInd());
            h.setFlag(hdr.getFlag());

            h.setRecadvNo(hdr.getRecadvNo());
            h.setPoMaSrno(hdr.getPoMaSrno());
            h.setCaReason(hdr.getCaReason());

            h.setPublishFlag(hdr.getPublishFlag());
            h.setVetBy(hdr.getVetBy());


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (hdr.getCakeyDate() != null && !hdr.getCakeyDate().isBlank())
                h.setCakeyDate(LocalDate.parse(hdr.getCakeyDate(), formatter));

            if (hdr.getCaDate() != null && !hdr.getCaDate().isBlank())
                h.setCaDate(LocalDate.parse(hdr.getCaDate(), formatter));

            if (hdr.getRefDate() != null && !hdr.getRefDate().isBlank())
                h.setRefDate(LocalDate.parse(hdr.getRefDate(), formatter));

            if (hdr.getReinstDate() != null && !hdr.getReinstDate().isBlank())
                h.setReinstDate(LocalDate.parse(hdr.getReinstDate(), formatter));

            if (hdr.getVetDate() != null && !hdr.getVetDate().isBlank())
                h.setVetDate(LocalDate.parse(hdr.getVetDate(), formatter));


            if (hdr.getOldPoValue() != null && !hdr.getOldPoValue().isBlank())
                h.setOldPoValue(new BigDecimal(hdr.getOldPoValue()));

            if (hdr.getNewPoValue() != null && !hdr.getNewPoValue().isBlank())
                h.setNewPoValue(new BigDecimal(hdr.getNewPoValue()));

            if (hdr.getRecoveryAmt() != null && !hdr.getRecoveryAmt().isBlank())
                h.setRecoveryAmt(new BigDecimal(hdr.getRecoveryAmt()));

            poCancellationHeaderRepo.save(h);


            List<PoCancellationDetail> detailList = new ArrayList<>();

            for (PoCancellationDetailDto d : dtls) {

                PoCancellationDetail entity = new PoCancellationDetail();

                entity.setHeader(h);
                entity.setRly(d.getRly());
                entity.setCakey(d.getCakey());
                entity.setSlno(d.getSlno());
                entity.setPlNo(d.getPlNo());
                entity.setPoSr(d.getPoSr());
                entity.setStatus(d.getStatus());
                entity.setDemStatus(d.getDemStatus());

                if (d.getPoBalQty() != null && !d.getPoBalQty().isBlank())
                    entity.setPoBalQty(new BigDecimal(d.getPoBalQty()));

                if (d.getCancQty() != null && !d.getCancQty().isBlank())
                    entity.setCancQty(new BigDecimal(d.getCancQty()));

                detailList.add(entity);
            }

            poCancellationDetailRepo.saveAll(detailList);


            status.setStatus("SAVED");
            status.setProcessedAt(LocalDateTime.now());
            statusRepo.save(status);

        } catch (Exception e) {


            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            statusRepo.save(status);

            throw e;
        }
    }

    @Override
    public String getImmsToken() {
        return authService.getToken();
    }

    @Override
    public Object fetchPoData(java.util.Map<String, String> requestValues) {
        String token = getImmsToken();
        
        String urlEnding = "/purchase/getPOData";
        if (requestValues.containsKey("maNo")) {
            urlEnding = "/purchase/getPoMaData";
        } else if (requestValues.containsKey("caNo")) {
            urlEnding = "/purchase/getPoCaData";
        }
        
        String url = crisBaseUrl + urlEnding;

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        org.springframework.http.HttpEntity<java.util.Map<String, String>> entity = 
            new org.springframework.http.HttpEntity<>(requestValues, headers);

        try {
            org.springframework.http.ResponseEntity<Object> response = 
                crisRestTemplate.postForEntity(url, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching data from CRIS: " + e.getMessage());
        }
    }

}
