package com.sarthi.service.Impl;

import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.dto.crisDtos.*;
import com.sarthi.entity.*;
import com.sarthi.entity.CricsPos.*;
import com.sarthi.repository.*;
import com.sarthi.service.UserService;
import com.sarthi.service.crisService;
import org.springframework.beans.BeanUtils;
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

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private PoMaHeaderRepository poMaHeaderRepository;
    @Autowired
    private PoMaDetailRepository poMaDetailRepository;
    @Autowired
    private PoCancellationHeaderRepository poCancellationHeaderRepo;

    @Autowired
    private PoCancellationDetailRepository poCancellationDetailRepo;
    @Autowired
    private AmendmentPoHeaderRepository amendmentPoHeaderRepository;
    @Autowired
    private AmendmentPoItemRepository amendmentPoItemRepository;

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

            // Duplicate check in main table
            if (headerRepo.existsByPoKey(poKey)) {
                throw new RuntimeException("PO already exists in DB: " + poKey);
            }

            // Vendor Code cleanup
            String vendorCode = hdr.getIMMS_VENDOR_CODE();
            if (vendorCode != null) {
                // vendorCode = vendorCode.replace(":", "");
                hdr.setIMMS_VENDOR_CODE(vendorCode);
            }

            // Vendor + User creation
            createVendorIfNotExists(hdr);

            // Save Header
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
            h.setPoDate(parseFlexibleDateTime(m.getPO_DT()));

        if (m.getRECV_DT() != null)
            h.setReceivedDate(parseFlexibleDateTime(m.getRECV_DT()));

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
            i.setQtyCancelled(
                    new BigDecimal(m.getQTY_CANCELLED()).setScale(0, java.math.RoundingMode.HALF_UP).intValue());

        i.setRate(bd(m.getRATE()));
        i.setBasicValue(bd(m.getBASIC_VALUE()));
        i.setSalesTaxPercent(bd(m.getSALES_TAX_PER()));
        i.setSalesTax(bd(m.getSALES_TAX()));
        i.setDiscountType(m.getDISCOUNT_TYPE());
        i.setDiscountPercent(bd(m.getDISCOUNT_PER()));
        i.setDiscount(bd(m.getDISCOUNT()));
        i.setValue(bd(m.getVALUE()));

        if (m.getDELV_DT() != null)
            i.setDeliveryDate(parseFlexibleDateTime(m.getDELV_DT()));

        if (m.getEXT_DELV_DT() != null)
            i.setExtendedDeliveryDate(parseFlexibleDateTime(m.getEXT_DELV_DT()));

        if (m.getDATETIME() != null)
            i.setCrisTimestamp(parseFlexibleDateTime(m.getDATETIME()));

        i.setAllocation(m.getALLOCATION());
        i.setUserId(m.getUSER_ID());

        return i;
    }

    private LocalDateTime parseFlexibleDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        str = str.trim();
        String[] patterns = {
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss.SS",
            "yyyy-MM-dd HH:mm:ss.S",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy"
        };
        for (String pat : patterns) {
            try {
                if (pat.contains("HH") || pat.contains("T")) {
                    return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pat));
                } else {
                    return LocalDate.parse(str, DateTimeFormatter.ofPattern(pat)).atStartOfDay();
                }
            } catch (Exception ignored) {}
        }
        try {
            return LocalDateTime.parse(str);
        } catch (Exception ignored) {}
        try {
            return LocalDate.parse(str).atStartOfDay();
        } catch (Exception ignored) {}
        return null;
    }

    private LocalDate parseFlexibleLocalDate(String str) {
        LocalDateTime ldt = parseFlexibleDateTime(str);
        return ldt != null ? ldt.toLocalDate() : null;
    }

    private static final DateTimeFormatter PO_DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TS_FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendPattern(".S")
            .optionalEnd()
            .toFormatter();

    private BigDecimal bd(Object o) {
        return o == null ? null : new BigDecimal(o.toString());
    }

    private String getVendorRole(String itemCatDescr) {
        if (itemCatDescr == null)
            return "Vendor";

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

        String rawVendorCode = hdr.getIMMS_VENDOR_CODE();
        if (rawVendorCode == null || rawVendorCode.isBlank()) return;

        String cleanCode = rawVendorCode.replace(":", "");
        String colonCode = ":" + cleanCode;
        String vendorCode = cleanCode;

        // Check if Vendor or User already exists under any colon format
        boolean vendorExists = vendorMasterRepository.findByVendorCode(cleanCode).isPresent()
                || vendorMasterRepository.findByVendorCode(colonCode).isPresent()
                || vendorMasterRepository.findByVendorCode(rawVendorCode).isPresent();

        boolean userExists = userMasterRepository.existsByUserName(cleanCode)
                || userMasterRepository.existsByUserName(colonCode)
                || userMasterRepository.existsByUserName(rawVendorCode);

        if (vendorExists && userExists) {
            return;
        }

        // Determine Role
        String role = getVendorRole(hdr.getITEM_CAT_DESCR());

        // Create User
        userRequestDto dto = new userRequestDto();
        dto.setUserName(vendorCode);
        dto.setPassword("Vendor@123"); // better default
        dto.setEmail(vendorCode + "@vendor.local");
        dto.setMobileNumber(null);
        dto.setRoleNames(List.of(role));
        dto.setStatus("Active");

        dto.setCreatedBy("CRIS");
        dto.setEmployeeId(null);
        dto.setClusterName(null);
        dto.setRegionName(null);
        dto.setPriority(null);
        dto.setIeUserIds(null);

        userService.createUser(dto);

        // Fetch user
        Optional<UserMaster> userOpt = userMasterRepository.findFirstByUserName(vendorCode);

        if (userOpt.isEmpty())
            return;

        Integer userId = userOpt.get().getUserId();

        // Fetch role
        Optional<RoleMaster> roleOpt = roleMasterRepository.findByRoleName(role);

        if (roleOpt.isEmpty())
            return; // avoid NPE

        RoleMaster roleEntity = roleOpt.get();

        // Check mapping exists
        boolean existsMapping = userRoleMasterRepository.existsByUserIdAndRoleId(
                userId, roleEntity.getRoleId());

        // Save mapping
        if (!existsMapping) {
            UserRoleMaster urm = new UserRoleMaster();
            urm.setUserId(userId);
            urm.setRoleId(roleEntity.getRoleId());
            urm.setCreatedBy("CRIS");

            userRoleMasterRepository.save(urm);
        }
    }

    /*
     * @Transactional
     * public void saveMaFromFrontend(MaRequestDto request) {
     * 
     * MaHeaderDto hdr = request.getMMP_POMA_HDR();
     * List<MaDetailDto> dtls = request.getMMP_POMA_DTL();
     * 
     * if (hdr == null) throw new RuntimeException("Header missing");
     * if (dtls == null || dtls.isEmpty()) throw new
     * RuntimeException("Details missing");
     * 
     * String maKey = hdr.getMAKEY();
     * if (maKey == null || maKey.isBlank())
     * throw new RuntimeException("MAKEY is mandatory");
     * 
     * if (statusRepo.existsByRefTypeAndRefKey("MA", maKey)) {
     * throw new RuntimeException("Already processed");
     * }
     * 
     * CrisSyncStatus status = new CrisSyncStatus();
     * status.setRefType("MA");
     * status.setRefKey(maKey);
     * status.setRly(hdr.getRLY());
     * status.setStatus("FETCHED");
     * status.setFetchedAt(LocalDateTime.now());
     * 
     * statusRepo.save(status);
     * 
     * try {
     * 
     * if (poMaHeaderRepository.existsByMaKey(maKey)) {
     * throw new RuntimeException("Already exists in DB");
     * }
     * 
     * PoMaHeader h = new PoMaHeader();
     * 
     * 
     * h.setMaKey(maKey);
     * h.setRly(hdr.getRLY());
     * h.setPoKey(hdr.getPOKEY());
     * h.setPoNo(hdr.getPO_NO());
     * h.setMaNo(hdr.getMA_NO());
     * h.setSubject(hdr.getSUBJECT());
     * 
     * 
     * h.setMaType(hdr.getMA_TYPE());
     * h.setVcode(hdr.getVCODE());
     * h.setRemarks(hdr.getREMARKS());
     * h.setMaSignOff(hdr.getMA_SIGN_OFF());
     * h.setStatus(hdr.getSTATUS());
     * h.setPurDiv(hdr.getPUR_DIV());
     * h.setPurSec(hdr.getPUR_SEC());
     * h.setPoMaSrno(hdr.getPO_MA_SRNO());
     * h.setPublishFlag(hdr.getPUBLISH_FLAG());
     * 
     * 
     * h.setRefNo(hdr.getREF_NO());
     * h.setRequestId(hdr.getREQUEST_ID());
     * 
     * h.setAuthSeq(hdr.getAUTH_SEQ());
     * h.setAuthSeqFin(hdr.getAUTH_SEQ_FIN());
     * 
     * h.setCurUser(hdr.getCURUSER());
     * h.setCurUserInd(hdr.getCURUSER_IND());
     * 
     * h.setSignId(hdr.getSIGN_ID());
     * h.setReqId(hdr.getREQ_ID());
     * 
     * h.setRecInd(hdr.getREC_IND());
     * h.setFlag(hdr.getFLAG());
     * h.setReqFlag(hdr.getREQ_FLAG());
     * 
     * 
     * if (hdr.getMA_DATE() != null && !hdr.getMA_DATE().isBlank())
     * h.setMaDate(LocalDate.parse(hdr.getMA_DATE()));
     * 
     * if (hdr.getMAKEY_DATE() != null && !hdr.getMAKEY_DATE().isBlank())
     * h.setMaKeyDate(LocalDate.parse(hdr.getMAKEY_DATE()));
     * 
     * if (hdr.getREF_DATE() != null && !hdr.getREF_DATE().isBlank())
     * h.setRefDate(LocalDate.parse(hdr.getREF_DATE()));
     * 
     * 
     * if (hdr.getOLD_PO_VALUE() != null && !hdr.getOLD_PO_VALUE().isBlank())
     * h.setOldPoValue(new BigDecimal(hdr.getOLD_PO_VALUE()));
     * 
     * if (hdr.getNEW_PO_VALUE() != null && !hdr.getNEW_PO_VALUE().isBlank())
     * h.setNewPoValue(new BigDecimal(hdr.getNEW_PO_VALUE()));
     * 
     * // SAVE HEADER
     * poMaHeaderRepository.save(h);
     * 
     * 
     * List<PoMaDetail> detailList = new ArrayList<>();
     * 
     * for (MaDetailDto m : dtls) {
     * 
     * PoMaDetail d = new PoMaDetail();
     * 
     * d.setMaHeader(h);
     * d.setMaKey(m.getMAKEY());
     * d.setRly(m.getRLY());
     * d.setSlno(m.getSLNO());
     * d.setMaFld(m.getMA_FLD());
     * d.setMaFldDescr(m.getMA_FLD_DESCR());
     * d.setOldValue(m.getOLD_VALUE());
     * d.setNewValue(m.getNEW_VALUE());
     * 
     * d.setNewValueInd(m.getNEW_VALUE_IND());
     * d.setNewValueFlag(m.getNEW_VALUE_FLAG());
     * d.setPlNo(m.getPL_NO());
     * d.setPoSr(m.getPO_SR());
     * 
     * d.setCondSlno(m.getCOND_SLNO());
     * d.setCondCode(m.getCOND_CODE());
     * d.setMaSrNo(m.getMA_SR_NO());
     * d.setStatus(m.getSTATUS());
     * 
     * d.setExpSr(m.getEXP_SR());
     * d.setExpCode(m.getEXP_CODE());
     * d.setCondNo(m.getCOND_NO());
     * 
     * if (m.getORIG_DP() != null && !m.getORIG_DP().isBlank())
     * d.setOrigDp(LocalDate.parse(m.getORIG_DP()));
     * 
     * d.setPaymentYear(m.getPAYMENT_YEAR());
     * d.setNewPosrData(m.getNEW_POSR_DATA());
     * d.setRefPono(m.getREF_PONO());
     * d.setConsigneeRly(m.getCONSIGNEE_RLY());
     * 
     * detailList.add(d);
     * }
     * 
     * poMaDetailRepository.saveAll(detailList);
     * 
     * status.setStatus("SAVED");
     * status.setProcessedAt(LocalDateTime.now());
     * statusRepo.save(status);
     * 
     * } catch (Exception e) {
     * 
     * status.setStatus("FAILED");
     * status.setErrorMessage(e.getMessage());
     * statusRepo.save(status);
     * 
     * throw e;
     * }
     * }
     */

    /*
     * @Transactional
     * public void saveMaPo(MaPoRequestDTO request) {
     * 
     * MaPoHeaderDTO hdr =
     * request.getData().getMmpPomaHdr();
     * 
     * PoMaHeader header = new PoMaHeader();
     * 
     * BeanUtils.copyProperties(hdr, header);
     * 
     * if (hdr.getMaDate() != null) {
     * header.setMaDate(LocalDate.parse(hdr.getMaDate()));
     * }
     * 
     * if (hdr.getRefDate() != null) {
     * header.setRefDate(LocalDate.parse(hdr.getRefDate()));
     * }
     * 
     * if (hdr.getMaKeyDate() != null) {
     * header.setMaKeyDate(LocalDate.parse(hdr.getMaKeyDate()));
     * }
     * 
     * if (hdr.getVetDate() != null) {
     * header.setVetDate(LocalDate.parse(hdr.getVetDate()));
     * }
     * 
     * PoMaHeader savedHeader =
     * poMaHeaderRepository.save(header);
     * 
     * List<PoMaDetail> items =
     * request.getData()
     * .getMmpPomaDtl()
     * .stream()
     * .map(dto -> {
     * 
     * PoMaDetail item = new PoMaDetail();
     * 
     * BeanUtils.copyProperties(
     * dto,
     * item);
     * 
     * item.setMaPoHeader(
     * savedHeader);
     * 
     * return item;
     * })
     * .toList();
     * 
     * poMaDetailRepository.saveAll(items);
     * }
     * 
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMaPo(MaPoRequestDTO request) {

        try {
            if (request == null || request.getData() == null) {
                throw new RuntimeException("Request data cannot be empty");
            }

            // 1. Resolve poKey from any available field (MmpPomaHdr or PoDtl)
            String poKey = null;
            if (request.getData().getMmpPomaHdr() != null && request.getData().getMmpPomaHdr().getPoKey() != null && !request.getData().getMmpPomaHdr().getPoKey().isBlank()) {
                poKey = request.getData().getMmpPomaHdr().getPoKey();
            } else if (request.getData().getPoDtl() != null && !request.getData().getPoDtl().isEmpty() && request.getData().getPoDtl().get(0).getPoKey() != null) {
                poKey = request.getData().getPoDtl().get(0).getPoKey();
            }

            // 2. Fetch or auto-create base PoHeader if poKey is present
            PoHeader poHeader = null;
            if (poKey != null) {
                poHeader = headerRepo.findByPoKey(poKey).orElse(null);
            }

            if (poHeader == null && poKey != null) {
                poHeader = new PoHeader();
                poHeader.setPoKey(poKey);
                if (request.getData().getPoHdr() != null) {
                    AmendedPoHeaderDTO ph = request.getData().getPoHdr();
                    if (ph.getPoNo() != null) poHeader.setPoNo(ph.getPoNo());
                    if (ph.getRlyCd() != null) poHeader.setRlyCd(ph.getRlyCd());
                    if (ph.getVendorCode() != null) poHeader.setVendorCode(ph.getVendorCode());
                    if (ph.getBillPayOff() != null) poHeader.setBillPayOff(ph.getBillPayOff());
                    if (ph.getInspectingAgency() != null) poHeader.setInspectingAgency(ph.getInspectingAgency());
                    if (ph.getPoStatus() != null) poHeader.setPoStatus(ph.getPoStatus());
                    if (ph.getPoDate() != null) poHeader.setPoDate(parseFlexibleDateTime(ph.getPoDate()));
                }
                if (request.getData().getPoDtl() != null && !request.getData().getPoDtl().isEmpty()) {
                    AmendedPoItemDTO firstItem = request.getData().getPoDtl().get(0);
                    if (poHeader.getRlyCd() == null) poHeader.setRlyCd(firstItem.getRly());
                    if (poHeader.getBillPayOff() == null) poHeader.setBillPayOff(firstItem.getBillPayOff());
                } else if (request.getData().getMmpPomaDtl() != null && !request.getData().getMmpPomaDtl().isEmpty()) {
                    MaPoItemDTO firstItem = request.getData().getMmpPomaDtl().get(0);
                    if (poHeader.getRlyCd() == null) poHeader.setRlyCd(firstItem.getRly());
                }
                if (poHeader.getPoNo() == null && request.getData().getMmpPomaHdr() != null && request.getData().getMmpPomaHdr().getPoNo() != null) {
                    poHeader.setPoNo(request.getData().getMmpPomaHdr().getPoNo());
                }
                poHeader.setSourceSystem("CRIS_MA");
                poHeader = headerRepo.save(poHeader);
            } else if (poHeader != null) {
                if (poHeader.getPoDate() == null && request.getData().getPoHdr() != null && request.getData().getPoHdr().getPoDate() != null) {
                    poHeader.setPoDate(parseFlexibleDateTime(request.getData().getPoHdr().getPoDate()));
                    headerRepo.save(poHeader);
                }
            }

            // ======================================
            // SAVE MA HEADER
            // ======================================
            PoMaHeader savedMaHeader = saveMaHeader(
                    request.getData() != null ? request.getData().getMmpPomaHdr() : null,
                    request);

            // ======================================
            // SAVE MA DETAILS
            // ======================================

            saveMaDetails(
                    savedMaHeader,
                    request.getData() != null ? request.getData().getMmpPomaDtl() : null,
                    request.getData() != null ? request.getData().getPoDtl() : null);

            // ======================================
            // SAVE AMENDED HEADER & ITEMS IF PRESENT
            // ======================================
            AmendedPoHeader amendedHeader = null;
            if (request.getData().getPoDtl() != null && !request.getData().getPoDtl().isEmpty()) {
                amendedHeader = saveAmendedPoHeader(
                        request.getData().getPoDtl(),
                        poHeader,
                        poKey
                );

            if (request.getData() != null && request.getData().getPoDtl() != null && !request.getData().getPoDtl().isEmpty()) {
                AmendedPoHeader amendedHeader = saveAmendedPoHeader(request.getData().getPoDtl());
                if (amendedHeader != null) {
                    saveAmendedPoItems(amendedHeader, request.getData().getPoDtl());
                    syncPoHeader(amendedHeader);
                    syncPoItems(amendedHeader);
                }
            }

            // ======================================
            // UPDATE LIVE PO HEADER & DELIVERY DATES
            // ======================================

            PoHeader poHeader = findPoHeaderByPoKeyOrPoNo(
                    savedMaHeader.getPoKey(), savedMaHeader.getPoNo());

            if (poHeader != null) {
                updateAmendmentStatus(poHeader, savedMaHeader);
            }

            syncMaDeliveryDates(savedMaHeader);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Failed to save MA PO: " + e.getMessage(),
                    e);
        }
    }

    private void updateAmendmentStatus(
            PoHeader poHeader,
            PoMaHeader maHeader) {

        if (poHeader == null) {
            return;
        }

        poHeader.setIsAmended(true);

        Integer count = poHeader.getAmendmentCount() == null
                ? 0
                : poHeader.getAmendmentCount();

        poHeader.setAmendmentCount(
                count + 1);

        if (maHeader != null) {
            if (maHeader.getMaNo() != null) {
                poHeader.setLastAmendmentNo(
                        maHeader.getMaNo());
            }

            if (maHeader.getMaDate() != null) {
                poHeader.setLastAmendmentDate(
                        maHeader.getMaDate()
                                .atStartOfDay());
            }
        }

        headerRepo.save(poHeader);
    }

    private PoHeader findPoHeaderByPoKeyOrPoNo(String poKey, String poNo) {
        if (poKey != null && !poKey.isBlank()) {
            Optional<PoHeader> opt = headerRepo.findFirstByPoKey(poKey.trim());
            if (opt.isPresent()) return opt.get();
            opt = headerRepo.findFirstByPoNo(poKey.trim());
            if (opt.isPresent()) return opt.get();
        }
        if (poNo != null && !poNo.isBlank()) {
            Optional<PoHeader> opt = headerRepo.findFirstByPoNo(poNo.trim());
            if (opt.isPresent()) return opt.get();
            opt = headerRepo.findFirstByPoKey(poNo.trim());
            if (opt.isPresent()) return opt.get();
        }
        return null;
    }

    private void syncMaDeliveryDates(PoMaHeader maHeader) {
        if (maHeader == null || maHeader.getItems() == null || maHeader.getItems().isEmpty()) {
            return;
        }

        String poKey = maHeader.getPoKey();
        String poNo = maHeader.getPoNo();

        PoHeader poHeader = null;
        try {
            poHeader = findPoHeaderByPoKeyOrPoNo(poKey, poNo);
        } catch (Exception ignored) {}

        if (poHeader == null) return;

        List<PoItem> items = itemRepo.findByPoHeader(poHeader);
        if (items == null || items.isEmpty()) return;

        for (PoMaDetail d : maHeader.getItems()) {
            String maFld = d.getMaFld();
            String maFldDescr = d.getMaFldDescr();

            boolean isDp = (maFld != null && (maFld.equalsIgnoreCase("DP") || maFld.equalsIgnoreCase("DELV_DT") || maFld.equalsIgnoreCase("EXT_DELV_DT")))
                    || (maFldDescr != null && (maFldDescr.toLowerCase().contains("delivery period") || maFldDescr.toLowerCase().contains("delivery date") || maFldDescr.toLowerCase().contains("dp")));

            if (isDp) {
                LocalDate parsedDate = VendorPoServiceImpl.parseDateFromMaValue(d.getNewValue());
                if (parsedDate != null) {
                    for (PoItem item : items) {
                        String srNo = item.getItemSrNo();
                        String poSr = d.getPoSr();

                        boolean match = (poSr != null && srNo != null && (poSr.trim().equalsIgnoreCase(srNo.trim())
                                || tryParseInt(poSr) == tryParseInt(srNo)));

                        if (match) {
                            item.setExtendedDeliveryDate(parsedDate.atStartOfDay());
                            itemRepo.save(item);
                            System.out.println("[MA Sync] Updated PoItem EDP to " + parsedDate + " for item_sr_no: " + srNo);
                        }
                    }
                }
            }
        }
    }

    private int tryParseInt(String val) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private PoMaHeader saveMaHeader(MaPoHeaderDTO hdr, MaPoRequestDTO request) {
        PoMaHeader header = new PoMaHeader();

        if (hdr != null) {
            header.setRly(hdr.getRly());
            header.setMaKey(hdr.getMaKey());
            header.setPoKey(hdr.getPoKey());
            header.setPoNo(hdr.getPoNo());
            header.setMaNo(hdr.getMaNo());
            header.setMaType(hdr.getMaType());
            header.setVendorCode(hdr.getVendorCode());
            header.setSubject(hdr.getSubject());
            header.setRefNo(hdr.getRefNo());
            header.setRemarks(hdr.getRemarks());
            header.setMaSignOff(hdr.getMaSignOff());
            header.setRequestId(hdr.getRequestId());
            header.setAuthSeq(hdr.getAuthSeq());
            header.setAuthSeqFin(hdr.getAuthSeqFin());
            header.setCurUser(hdr.getCurUser());
            header.setCurUserInd(hdr.getCurUserInd());
            header.setSignId(hdr.getSignId());
            header.setReqId(hdr.getReqId());
            header.setFinStatus(hdr.getFinStatus());
            header.setRecInd(hdr.getRecInd());
            header.setFlag(hdr.getFlag());
            header.setStatus(hdr.getStatus());
            header.setPurDiv(hdr.getPurDiv());
            header.setPurSec(hdr.getPurSec());
            header.setOldPoValue(hdr.getOldPoValue());
            header.setNewPoValue(hdr.getNewPoValue());
            header.setPoMaSrNo(hdr.getPoMaSrNo());
            header.setPublishFlag(hdr.getPublishFlag());
            header.setSent4Vet(hdr.getSent4Vet());
            header.setVetBy(hdr.getVetBy());
            header.setReqFlag(hdr.getReqFlag());

            if (hdr.getMaDate() != null && !hdr.getMaDate().isBlank()) {
                try { header.setMaDate(VendorPoServiceImpl.parseDateFromMaValue(hdr.getMaDate())); } catch (Exception ignored) {}
            }

            if (hdr.getRefDate() != null && !hdr.getRefDate().isBlank()) {
                try { header.setRefDate(VendorPoServiceImpl.parseDateFromMaValue(hdr.getRefDate())); } catch (Exception ignored) {}
            }

            if (hdr.getMaKeyDate() != null && !hdr.getMaKeyDate().isBlank()) {
                try { header.setMaKeyDate(VendorPoServiceImpl.parseDateFromMaValue(hdr.getMaKeyDate())); } catch (Exception ignored) {}
            }

            if (hdr.getVetDate() != null && !hdr.getVetDate().isBlank()) {
                try { header.setVetDate(VendorPoServiceImpl.parseDateFromMaValue(hdr.getVetDate())); } catch (Exception ignored) {}
            }
        }

        // Fallback for poKey, poNo, rly, maNo, maDate if missing in header DTO
        if (request != null) {
            if (header.getMaNo() == null || header.getMaNo().isBlank()) {
                if (request.getMaNo() != null && !request.getMaNo().isBlank()) header.setMaNo(request.getMaNo());
                else if (request.getData() != null && request.getData().getMaNo() != null && !request.getData().getMaNo().isBlank()) header.setMaNo(request.getData().getMaNo());
            }

            if (header.getMaDate() == null) {
                String rawMaDate = request.getMaDate() != null && !request.getMaDate().isBlank() ? request.getMaDate() : (request.getData() != null ? request.getData().getMaDate() : null);
                if (rawMaDate != null && !rawMaDate.isBlank()) {
                    try { header.setMaDate(VendorPoServiceImpl.parseDateFromMaValue(rawMaDate)); } catch (Exception ignored) {}
                }
            }

            if (header.getPoNo() == null || header.getPoNo().isBlank()) {
                if (request.getPoNo() != null && !request.getPoNo().isBlank()) header.setPoNo(request.getPoNo());
            }

            if (header.getPoKey() == null || header.getPoKey().isBlank()) {
                if (request.getPoKey() != null && !request.getPoKey().isBlank()) header.setPoKey(request.getPoKey());
            }

            if (header.getRly() == null || header.getRly().isBlank()) {
                if (request.getRly() != null && !request.getRly().isBlank()) header.setRly(request.getRly());
            }

            if (header.getVendorCode() == null || header.getVendorCode().isBlank()) {
                if (request.getVendorCode() != null && !request.getVendorCode().isBlank()) header.setVendorCode(request.getVendorCode());
            }

            if (request.getData() != null) {
                if ((header.getPoKey() == null || header.getPoKey().isBlank()) && request.getData().getPoHdr() != null) {
                    if (request.getData().getPoHdr().getPoKey() != null) header.setPoKey(request.getData().getPoHdr().getPoKey());
                    if (request.getData().getPoHdr().getPoNo() != null) header.setPoNo(request.getData().getPoHdr().getPoNo());
                }
                if ((header.getPoKey() == null || header.getPoKey().isBlank()) && request.getData().getPoDtl() != null && !request.getData().getPoDtl().isEmpty()) {
                    AmendedPoItemDTO firstItem = request.getData().getPoDtl().get(0);
                    if (firstItem.getPoKey() != null && !firstItem.getPoKey().isBlank()) header.setPoKey(firstItem.getPoKey());
                    if (firstItem.getPoNo() != null && !firstItem.getPoNo().isBlank()) header.setPoNo(firstItem.getPoNo());
                    if (header.getRly() == null) header.setRly(firstItem.getRly());
                }
                if ((header.getPoNo() == null || header.getPoNo().isBlank()) && request.getData().getMmpPomaDtl() != null && !request.getData().getMmpPomaDtl().isEmpty()) {
                    MaPoItemDTO firstMaItem = request.getData().getMmpPomaDtl().get(0);
                    if (firstMaItem.getRefPoNo() != null && !firstMaItem.getRefPoNo().isBlank()) header.setPoNo(firstMaItem.getRefPoNo());
                    if (header.getRly() == null) header.setRly(firstMaItem.getRly());
                }
            }
        }

        // Fill remaining missing header fields from original PoHeader in DB if available
        try {
            PoHeader origPo = findPoHeaderByPoKeyOrPoNo(header.getPoKey(), header.getPoNo());
            if (origPo != null) {
                if (header.getPoKey() == null) header.setPoKey(origPo.getPoKey());
                if (header.getPoNo() == null) header.setPoNo(origPo.getPoNo());
                if (header.getRly() == null) header.setRly(origPo.getRlyCd());
                if (header.getVendorCode() == null) header.setVendorCode(origPo.getVendorCode());

                if (header.getMaKey() == null) header.setMaKey(origPo.getPoKey() != null ? origPo.getPoKey() : header.getPoKey());
                if (header.getMaKeyDate() == null) header.setMaKeyDate(header.getMaDate() != null ? header.getMaDate() : LocalDate.now());
                if (header.getMaNo() == null) {
                    int count = origPo.getAmendmentCount() != null ? origPo.getAmendmentCount() + 1 : 1;
                    header.setMaNo(String.format("%03d", count));
                }
                if (header.getMaDate() == null) header.setMaDate(LocalDate.now());
                if (header.getSubject() == null) header.setSubject("Amendment to P.O.No. " + origPo.getPoNo());
                if (header.getRefNo() == null) header.setRefNo("AN/" + LocalDate.now().getYear() + "/" + (origPo.getRlyShortName() != null ? origPo.getRlyShortName() : "RLY"));
                if (header.getRefDate() == null) header.setRefDate(header.getMaDate() != null ? header.getMaDate() : LocalDate.now());
                if (header.getStatus() == null) header.setStatus("A");
                if (header.getPoMaSrNo() == null) header.setPoMaSrNo("001");
            }
        } catch (Exception ignored) {}

        return poMaHeaderRepository.save(header);
    }

    private void saveMaDetails(
            PoMaHeader header,
            List<MaPoItemDTO> dtos,
            List<AmendedPoItemDTO> fallbackDtos) {

        List<PoMaDetail> items = new ArrayList<>();

        if (dtos != null && !dtos.isEmpty()) {
            for (MaPoItemDTO dto : dtos) {
                PoMaDetail item = new PoMaDetail();
                item.setMaPoHeader(header);
                item.setRly(dto.getRly() != null ? dto.getRly() : header.getRly());
                item.setMaKey(dto.getMaKey() != null ? dto.getMaKey() : header.getMaKey());
                item.setSlNo(dto.getSlNo());
                item.setMaFld(dto.getMaFld());
                item.setMaFldDescr(dto.getMaFldDescr());
                item.setOldValue(dto.getOldValue());
                item.setNewValue(dto.getNewValue());
                item.setNewValueInd(dto.getNewValueInd());
                item.setNewValueFlag(dto.getNewValueFlag());
                item.setPlNo(dto.getPlNo());
                item.setPoSr(dto.getPoSr());
                item.setExpSr(dto.getExpSr());
                item.setExpCode(dto.getExpCode());
                item.setCondSlNo(dto.getCondSlNo());
                item.setCondNo(dto.getCondNo());
                item.setCondCode(dto.getCondCode());
                item.setStatus(dto.getStatus());
                item.setMaSrNo(dto.getMaSrNo());
                item.setOrigDp(dto.getOrigDp());
                item.setPaymentYear(dto.getPaymentYear());
                item.setNewPoSrData(dto.getNewPoSrData());
                item.setRefPoNo(dto.getRefPoNo() != null ? dto.getRefPoNo() : header.getPoNo());
                item.setConsigneeRly(dto.getConsigneeRly());
                items.add(item);
            }
        } else if (fallbackDtos != null && !fallbackDtos.isEmpty()) {
            int seq = 1;
            for (AmendedPoItemDTO dto : fallbackDtos) {
                PoMaDetail item = new PoMaDetail();
                item.setMaPoHeader(header);
                item.setRly(dto.getRly() != null ? dto.getRly() : header.getRly());
                item.setMaKey(header.getMaKey());
                item.setSlNo(String.valueOf(seq++));
                item.setPlNo(dto.getPlNo());
                item.setPoSr(dto.getItemSrNo());
                item.setRefPoNo(header.getPoNo() != null ? header.getPoNo() : dto.getPoNo());
                item.setConsigneeRly(dto.getConsigneeRly());

                if (dto.getDeliveryDate() != null) {
                    item.setOrigDp(dto.getDeliveryDate());
                    item.setOldValue(dto.getDeliveryDate());
                }
                if (dto.getExtendedDeliveryDate() != null) {
                    item.setNewValue(dto.getExtendedDeliveryDate());
                    item.setMaFld("DP");
                    item.setMaFldDescr("Delivery Period End");
                } else if (dto.getDeliveryDate() != null) {
                    item.setNewValue(dto.getDeliveryDate());
                    item.setMaFld("DP");
                    item.setMaFldDescr("Delivery Period End");
                }
                items.add(item);
            }
        }

        if (!items.isEmpty()) {
            poMaDetailRepository.saveAll(items);
        }
    }

    private AmendedPoHeader saveAmendedPoHeader(
            List<AmendedPoItemDTO> items,
            PoHeader poHeader,
            String poKey) {

        if (items == null || items.isEmpty()) {
            return null;
        }

        String poKey = items.get(0).getPoKey();
        String poNo = items.get(0).getPoNo();

        // FIND ORIGINAL PO HEADER
        PoHeader poHeader = findPoHeaderByPoKeyOrPoNo(poKey, poNo);

        AmendedPoHeader entity = new AmendedPoHeader();
        entity.setPoKey(effectivePoKey);

        // ===================================
        // COPY FROM ORIGINAL PO HEADER IF PRESENT
        // ===================================

        if (poHeader != null) {
            entity.setPoKey(poHeader.getPoKey());
            entity.setPoNo(poHeader.getPoNo());
            entity.setL5PoNo(poHeader.getL5PoNo());
            entity.setRlyCd(poHeader.getRlyCd());
            entity.setVendorCode(poHeader.getVendorCode());
            entity.setInspectingAgency(poHeader.getInspectingAgency());
            entity.setPoStatus(poHeader.getPoStatus());
            entity.setBillPayOff(poHeader.getBillPayOff());
            entity.setPoDate(poHeader.getPoDate());
            entity.setRegionCode(poHeader.getRegionCode());
        } else {
            entity.setPoKey(poKey);
            entity.setPoNo(poNo);
        }

        return amendmentPoHeaderRepository.save(
                entity);
    }

    /*
     * private void saveAmendedPoItems(
     * AmendedPoHeader header,
     * List<AmendedPoItemDTO> dtos) {
     * 
     * List<AmendedPoItem> items =
     * new ArrayList<>();
     * 
     * for (AmendedPoItemDTO dto : dtos) {
     * 
     * AmendedPoItem item =
     * new AmendedPoItem();
     * 
     * item.setAmendedPoHeader(
     * header);
     * 
     * item.setRly(dto.getRly());
     * 
     * item.setItemSrNo(
     * dto.getPoSr());
     * 
     * item.setPlNo(
     * dto.getPlNo());
     * 
     * item.setConsigneeCd(
     * dto.getConsigneeCd());
     * 
     * item.setAllocation(
     * dto.getAllocation());
     * 
     * item.setBillPayOff(
     * dto.getBillPayOff());
     * 
     * item.setBillPassOff(
     * dto.getBillPassOff());
     * 
     * item.setConsigneeRly(
     * dto.getConsigneeRly());
     * 
     * item.setPRly(
     * dto.getPRly());
     * 
     * if (dto.getPoQty() != null) {
     * 
     * item.setQty(
     * Integer.parseInt(
     * dto.getPoQty()));
     * }
     * 
     * if (dto.getQtyCancelled() != null) {
     * 
     * item.setQtyCancelled(
     * Integer.parseInt(
     * dto.getQtyCancelled()));
     * }
     * 
     * if (dto.getRate() != null) {
     * 
     * item.setRate(
     * new BigDecimal(
     * dto.getRate()));
     * }
     * 
     * items.add(item);
     * }
     * 
     * amendmentPoItemRepository.saveAll(
     * items);
     * }
     */
    private void saveAmendedPoItems(
            AmendedPoHeader header,
            List<AmendedPoItemDTO> dtos) {

        List<AmendedPoItem> items = new ArrayList<>();

        DateTimeFormatter deliveryFormatter = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm");

        DateTimeFormatter crisFormatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss");

        for (AmendedPoItemDTO dto : dtos) {

            AmendedPoItem item = new AmendedPoItem();

            item.setAmendedPoHeader(
                    header);

            // BASIC

            item.setRly(
                    dto.getRly());

            item.setItemSrNo(
                    dto.getItemSrNo());
            item.setPoKey(dto.getPoKey());

            item.setPlNo(
                    dto.getPlNo());

            item.setItemDesc(
                    dto.getItemDesc());

            // CONSIGNEE

            item.setConsigneeCd(
                    dto.getConsigneeCd());

            item.setImmsConsigneeCd(
                    dto.getImmsConsigneeCd());

            item.setImmsConsigneeName(
                    dto.getImmsConsigneeName());

            item.setConsigneeDetail(
                    dto.getConsigneeDetail());

            // UOM

            item.setUomCd(
                    dto.getUomCd());

            item.setUom(
                    dto.getUom());

            // OTHER

            item.setAllocation(
                    dto.getAllocation());

            item.setUserId(
                    dto.getUserId());

            item.setConsigneeRly(
                    dto.getConsigneeRly());

            item.setConsigneeRlyShortName(
                    dto.getConsigneeRlyShortName());

            item.setPRly(
                    dto.getPRly());

            item.setBillPayOff(
                    dto.getBillPayOff());

            item.setBillPayOffDesc(
                    dto.getBillPayOffDesc());

            item.setBillPassOff(
                    dto.getBillPassOff());

            // QUANTITY

            if (dto.getQty() != null
                    && !dto.getQty().isBlank()) {

                item.setQty(
                        Integer.parseInt(
                                dto.getQty()));
            }

            if (dto.getQtyCancelled() != null
                    && !dto.getQtyCancelled().isBlank()) {

                item.setQtyCancelled(
                        Integer.parseInt(
                                dto.getQtyCancelled()));
            }

            // FINANCIAL

            if (dto.getRate() != null
                    && !dto.getRate().isBlank()) {

                item.setRate(
                        new BigDecimal(
                                dto.getRate()));
            }

            if (dto.getBasicValue() != null
                    && !dto.getBasicValue().isBlank()) {

                item.setBasicValue(
                        new BigDecimal(
                                dto.getBasicValue()));
            }

            if (dto.getSalesTaxPercent() != null
                    && !dto.getSalesTaxPercent().isBlank()) {

                item.setSalesTaxPercent(
                        new BigDecimal(
                                dto.getSalesTaxPercent()));
            }

            if (dto.getSalesTax() != null
                    && !dto.getSalesTax().isBlank()) {

                item.setSalesTax(
                        new BigDecimal(
                                dto.getSalesTax()));
            }

            item.setDiscountType(
                    dto.getDiscountType());

            if (dto.getDiscountPercent() != null
                    && !dto.getDiscountPercent().isBlank()) {

                item.setDiscountPercent(
                        new BigDecimal(
                                dto.getDiscountPercent()));
            }

            if (dto.getDiscount() != null
                    && !dto.getDiscount().isBlank()) {

                item.setDiscount(
                        new BigDecimal(
                                dto.getDiscount()));
            }

            if (dto.getValue() != null
                    && !dto.getValue().isBlank()) {

                item.setValue(
                        new BigDecimal(
                                dto.getValue()));
            }

            item.setOtChargeType(
                    dto.getOtChargeType());

            if (dto.getOtChargePercent() != null
                    && !dto.getOtChargePercent().isBlank()) {

                item.setOtChargePercent(
                        new BigDecimal(
                                dto.getOtChargePercent()));
            }

            if (dto.getOtherCharges() != null
                    && !dto.getOtherCharges().isBlank()) {

                item.setOtherCharges(
                        new BigDecimal(
                                dto.getOtherCharges()));
            }

            // DATES

            if (dto.getDeliveryDate() != null
                    && !dto.getDeliveryDate().isBlank()) {

                item.setDeliveryDate(
                        LocalDateTime.parse(
                                dto.getDeliveryDate(),
                                deliveryFormatter));
            }

            if (dto.getExtendedDeliveryDate() != null
                    && !dto.getExtendedDeliveryDate().isBlank()) {

                item.setExtendedDeliveryDate(
                        LocalDateTime.parse(
                                dto.getExtendedDeliveryDate(),
                                deliveryFormatter));
            }

            if (dto.getCrisTimestamp() != null
                    && !dto.getCrisTimestamp().isBlank()) {

                item.setCrisTimestamp(
                        LocalDateTime.parse(
                                dto.getCrisTimestamp(),
                                crisFormatter));
            }

            items.add(item);
        }

        amendmentPoItemRepository.saveAll(
                items);
    }

    private void syncPoHeader(
            AmendedPoHeader amendedHeader) {

        if (amendedHeader == null) return;

        PoHeader poHeader = findPoHeaderByPoKeyOrPoNo(
                amendedHeader.getPoKey(), amendedHeader.getPoNo());

        if (poHeader == null) return;

        if (poHeader == null) {
            return;
        }

        if (amendedHeader.getPoNo() != null) poHeader.setPoNo(amendedHeader.getPoNo());
        if (amendedHeader.getRlyCd() != null) poHeader.setRlyCd(amendedHeader.getRlyCd());
        if (amendedHeader.getVendorCode() != null) poHeader.setVendorCode(amendedHeader.getVendorCode());
        if (amendedHeader.getInspectingAgency() != null) poHeader.setInspectingAgency(amendedHeader.getInspectingAgency());
        if (amendedHeader.getPoStatus() != null) poHeader.setPoStatus(amendedHeader.getPoStatus());
        if (amendedHeader.getBillPayOff() != null) poHeader.setBillPayOff(amendedHeader.getBillPayOff());
        if (amendedHeader.getRegionCode() != null) poHeader.setRegionCode(amendedHeader.getRegionCode());
        if (amendedHeader.getPoDate() != null) poHeader.setPoDate(amendedHeader.getPoDate());

        headerRepo.save(
                poHeader);
    }

    private void syncPoItems(
            AmendedPoHeader amendedHeader) {

        if (amendedHeader == null) return;

        PoHeader poHeader = findPoHeaderByPoKeyOrPoNo(
                amendedHeader.getPoKey(), amendedHeader.getPoNo());

        if (poHeader == null) return;

        List<AmendedPoItem> amendedItems = amendmentPoItemRepository
                .findByAmendedPoHeader(
                        amendedHeader);

        Map<String, PoItem> existingItems = itemRepo
                .findByPoHeader(poHeader)
                .stream()
                .filter(item -> item.getItemSrNo() != null && !item.getItemSrNo().isBlank())
                .collect(Collectors.toMap(
                        PoItem::getItemSrNo,
                        Function.identity(),
                        (existing, replacement) -> existing));

        for (AmendedPoItem amendedItem : amendedItems) {
            if (amendedItem.getItemSrNo() == null || amendedItem.getItemSrNo().isBlank()) {
                continue; // Skip items without a valid serial number
            }

            PoItem poItem = existingItems.get(amendedItem.getItemSrNo());

            if (poItem == null) {
                poItem = new PoItem();
                poItem.setPoHeader(poHeader);
                poItem.setItemSrNo(amendedItem.getItemSrNo());
            }

            copyAmendedItemToPo(amendedItem, poItem);
            itemRepo.save(poItem);
        }
    }

    /*
     * private void copyAmendedItemToPo(
     * AmendedPoItem source,
     * PoItem target) {
     * 
     * 
     * 
     * if (source.getRly() != null)
     * target.setRly(source.getRly());
     * 
     * if (source.getPlNo() != null)
     * target.setPlNo(source.getPlNo());
     * 
     * if (source.getQty() != null)
     * target.setQty(source.getQty());
     * 
     * if (source.getRate() != null)
     * target.setRate(source.getRate());
     * 
     * if (source.getAllocation() != null)
     * target.setAllocation(source.getAllocation());
     * 
     * if (source.getBillPayOff() != null)
     * target.setBillPayOff(source.getBillPayOff());
     * 
     * if (source.getBillPassOff() != null)
     * target.setBillPassOff(source.getBillPassOff());
     * 
     * if (source.getConsigneeRly() != null)
     * target.setConsigneeRly(source.getConsigneeRly());
     * 
     * if (source.getPRly() != null)
     * target.setPRly(source.getPRly());
     * 
     * if (source.getExtendedDeliveryDate() != null)
     * target.setExtendedDeliveryDate(
     * source.getExtendedDeliveryDate());
     * 
     * if (source.getCrisTimestamp() != null)
     * target.setCrisTimestamp(
     * source.getCrisTimestamp());
     * }
     */
    private void copyAmendedItemToPo(
            AmendedPoItem source,
            PoItem target) {

        // =========================
        // BASIC
        // =========================

        if (source.getRly() != null)
            target.setRly(source.getRly());

        if (source.getCaseNo() != null)
            target.setCaseNo(source.getCaseNo());

        if (source.getItemSrNo() != null)
            target.setItemSrNo(source.getItemSrNo());

        if (source.getPlNo() != null)
            target.setPlNo(source.getPlNo());

        if (source.getItemDesc() != null)
            target.setItemDesc(source.getItemDesc());

        // =========================
        // CONSIGNEE
        // =========================

        if (source.getConsigneeCd() != null)
            target.setConsigneeCd(source.getConsigneeCd());

        if (source.getImmsConsigneeCd() != null)
            target.setImmsConsigneeCd(source.getImmsConsigneeCd());

        if (source.getImmsConsigneeName() != null)
            target.setImmsConsigneeName(source.getImmsConsigneeName());

        if (source.getConsigneeDetail() != null)
            target.setConsigneeDetail(source.getConsigneeDetail());

        if (source.getConsigneeRly() != null)
            target.setConsigneeRly(source.getConsigneeRly());

        if (source.getConsigneeRlyShortName() != null)
            target.setConsigneeRlyShortName(
                    source.getConsigneeRlyShortName());

        // =========================
        // QTY & UOM
        // =========================

        if (source.getQty() != null)
            target.setQty(source.getQty());

        if (source.getQtyCancelled() != null)
            target.setQtyCancelled(source.getQtyCancelled());

        if (source.getUomCd() != null)
            target.setUomCd(source.getUomCd());

        if (source.getUom() != null)
            target.setUom(source.getUom());

        // =========================
        // FINANCIALS
        // =========================

        if (source.getRate() != null)
            target.setRate(source.getRate());

        if (source.getBasicValue() != null)
            target.setBasicValue(source.getBasicValue());

        if (source.getSalesTaxPercent() != null)
            target.setSalesTaxPercent(
                    source.getSalesTaxPercent());

        if (source.getSalesTax() != null)
            target.setSalesTax(source.getSalesTax());

        if (source.getDiscountType() != null)
            target.setDiscountType(
                    source.getDiscountType());

        if (source.getDiscountPercent() != null)
            target.setDiscountPercent(
                    source.getDiscountPercent());

        if (source.getDiscount() != null)
            target.setDiscount(source.getDiscount());

        if (source.getValue() != null)
            target.setValue(source.getValue());

        if (source.getOtChargeType() != null)
            target.setOtChargeType(
                    source.getOtChargeType());

        if (source.getOtChargePercent() != null)
            target.setOtChargePercent(
                    source.getOtChargePercent());

        if (source.getOtherCharges() != null)
            target.setOtherCharges(
                    source.getOtherCharges());

        // =========================
        // DATES
        // =========================

        if (source.getDeliveryDate() != null)
            target.setDeliveryDate(
                    source.getDeliveryDate());

        if (source.getExtendedDeliveryDate() != null)
            target.setExtendedDeliveryDate(
                    source.getExtendedDeliveryDate());

        if (source.getCrisTimestamp() != null)
            target.setCrisTimestamp(
                    source.getCrisTimestamp());

        // =========================
        // MISC
        // =========================

        if (source.getAllocation() != null)
            target.setAllocation(source.getAllocation());

        if (source.getUserId() != null)
            target.setUserId(source.getUserId());

        if (source.getSourceSystem() != null)
            target.setSourceSystem(
                    source.getSourceSystem());

        if (source.getPRly() != null)
            target.setPRly(source.getPRly());

        if (source.getBillPayOff() != null)
            target.setBillPayOff(
                    source.getBillPayOff());

        if (source.getBillPayOffDesc() != null)
            target.setBillPayOffDesc(
                    source.getBillPayOffDesc());

        if (source.getBillPassOff() != null)
            target.setBillPassOff(
                    source.getBillPassOff());
    }

    /*
     * 
     * private void copyAmendedHeaderToPo(
     * AmendedPoHeader source,
     * PoHeader target) {
     * 
     * if (source.getPoNo() != null) {
     * target.setPoNo(source.getPoNo());
     * }
     * 
     * if (source.getRlyCd() != null) {
     * target.setRlyCd(source.getRlyCd());
     * }
     * 
     * if (source.getVendorCode() != null) {
     * target.setVendorCode(source.getVendorCode());
     * }
     * 
     * if (source.getInspectingAgency() != null) {
     * target.setInspectingAgency(
     * source.getInspectingAgency());
     * }
     * 
     * if (source.getPoStatus() != null) {
     * target.setPoStatus(
     * source.getPoStatus());
     * }
     * 
     * if (source.getBillPayOff() != null) {
     * target.setBillPayOff(
     * source.getBillPayOff());
     * }
     * 
     * if (source.getPoDate() != null) {
     * target.setPoDate(
     * source.getPoDate());
     * }
     * 
     * if (source.getCrisTimestamp() != null) {
     * target.setCrisTimestamp(
     * source.getCrisTimestamp());
     * }
     * }
     */
    private void copyAmendedHeaderToPo(
            AmendedPoHeader source,
            PoHeader target) {

        if (source.getPoNo() != null)
            target.setPoNo(source.getPoNo());

        if (source.getRlyCd() != null)
            target.setRlyCd(source.getRlyCd());

        if (source.getVendorCode() != null)
            target.setVendorCode(source.getVendorCode());

        if (source.getInspectingAgency() != null)
            target.setInspectingAgency(
                    source.getInspectingAgency());

        if (source.getPoStatus() != null)
            target.setPoStatus(
                    source.getPoStatus());

        if (source.getBillPayOff() != null)
            target.setBillPayOff(
                    source.getBillPayOff());

        if (source.getRegionCode() != null)
            target.setRegionCode(
                    source.getRegionCode());

        if (source.getPoDate() != null)
            target.setPoDate(
                    source.getPoDate());

        if (source.getCrisTimestamp() != null)
            target.setCrisTimestamp(
                    source.getCrisTimestamp());
    }

    @Transactional
    public void savePoCancellationFromFrontend(PoCancellationRequestDto request) {

        PoCancellationHeaderDto hdr = request.getHeader();
        List<PoCancellationDetailDto> dtls = request.getDetails();

        if (hdr == null)
            throw new RuntimeException("Header missing");
        if (dtls == null || dtls.isEmpty())
            throw new RuntimeException("Details missing");

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
    public String getPoDateByPoNo(String poNo) {
        PoHeader header = headerRepo.findByPoNo(poNo)
                .orElseThrow(() -> new RuntimeException("PO not found in database for PO No: " + poNo));
        if (header.getPoDate() != null) {
            // MySQL JDBC with serverTimezone=UTC converts the stored UTC datetime to
            // the JVM's local timezone (IST +05:30) before mapping to LocalDateTime.
            // e.g. DB: 2025-10-15 19:19:00 UTC → JVM reads as 2025-10-16 00:49:00 IST
            // So we must re-attach the system timezone and convert back to UTC
            // to recover the original calendar date (15/10/2025, not 16/10/2025).
            java.time.LocalDate originalDate = header.getPoDate()
                    .atZone(java.time.ZoneId.systemDefault())           // treat as IST
                    .withZoneSameInstant(java.time.ZoneOffset.UTC)      // convert to UTC
                    .toLocalDate();                                      // 2025-10-15
            return originalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return null;
    }

    @Override
    public PoHeader getPoHeaderByPoNo(String poNo) {
        return headerRepo.findByPoNo(poNo).orElse(null);
    }

    @Override
    public Object fetchPoData(java.util.Map<String, String> requestValues) {
        String token = getImmsToken();

        String urlEnding = "/purchase/getPOData";
        java.util.Map<String, String> payload = new java.util.HashMap<>(requestValues);

        if (payload.containsKey("maNo") && payload.get("maNo") != null && !payload.get("maNo").isBlank()) {
            urlEnding = "/purchase/getPoMaData";
            payload.remove("amended");
        } else if (payload.containsKey("caNo") && payload.get("caNo") != null && !payload.get("caNo").isBlank()) {
            urlEnding = "/purchase/getPoCaData";
            payload.remove("amended");
        } else if (payload.containsKey("amended")) {
            urlEnding = "/purchase/getAmendedPoData";
            payload.remove("amended");
        }

        String url = crisBaseUrl + urlEnding;
        System.out.println("Calling CRIS API: " + url);
        System.out.println("Payload: " + payload);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        org.springframework.http.HttpEntity<java.util.Map<String, String>> entity = new org.springframework.http.HttpEntity<>(
                payload, headers);

        try {
            org.springframework.http.ResponseEntity<Object> response = crisRestTemplate.postForEntity(url, entity,
                    Object.class);
            return response.getBody();
        } catch (Exception e) {
            if ("/purchase/getPOData".equals(urlEnding)) {
                String amendedUrl = crisBaseUrl + "/purchase/getAmendedPoData";
                System.out.println("CRIS getPOData returned error/417, retrying with getAmendedPoData: " + amendedUrl);
                try {
                    org.springframework.http.ResponseEntity<Object> amendedResponse = crisRestTemplate.postForEntity(amendedUrl, entity, Object.class);
                    return amendedResponse.getBody();
                } catch (Exception ex) {
                    System.err.println("Fallback getAmendedPoData also failed: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error fetching data from CRIS: " + e.getMessage());
        }
    }

}
