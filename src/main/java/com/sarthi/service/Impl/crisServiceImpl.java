package com.sarthi.service.Impl;

import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.dto.crisDtos.PoHeaderDto;
import com.sarthi.dto.crisDtos.PoItemDto;
import com.sarthi.dto.crisDtos.PoRequestDto;
import com.sarthi.entity.*;
import com.sarthi.entity.CricsPos.CrisSyncStatus;
import com.sarthi.repository.*;
import com.sarthi.service.UserService;
import com.sarthi.service.crisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import java.util.Optional;

@Service
public class crisServiceImpl implements crisService {

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

        h.setVendorCode(m.getIMMS_VENDOR_CODE());
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
            i.setQty(Integer.parseInt(m.getQTY()));

        if (m.getQTY_CANCELLED() != null)
            i.setQtyCancelled(Integer.parseInt(m.getQTY_CANCELLED()));

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




}
