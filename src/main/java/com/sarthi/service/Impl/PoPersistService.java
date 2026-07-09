package com.sarthi.service.Impl;

import com.sarthi.dto.WorkflowDtos.userRequestDto;
import com.sarthi.entity.*;
import com.sarthi.repository.*;
import com.sarthi.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PoPersistService {

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


    @Transactional
    public void savePo(Map<String, Object> data) {

        Map<String, Object> hdrMap = (Map<String, Object>) data.get("PoHdr");
        List<Map<String, Object>> itemList =
                (List<Map<String, Object>>) data.get("PoDtl");

        if (hdrMap == null) {
            throw new RuntimeException("PoHdr missing in response");
        }

        createVendorIfNotExists(hdrMap);

        String poKey = (String) hdrMap.get("POKEY");

        if (poKey == null) {
            throw new IllegalStateException("POKEY missing in CRIS response");
        }

        if (headerRepo.existsByPoKey(poKey)) {
            return;
        }

        PoHeader header = buildPoHeader(hdrMap);
        headerRepo.save(header);

        if (itemList != null) {
            for (Map<String, Object> m : itemList) {
                PoItem item = buildPoItem(m, header);
                itemRepo.save(item);
            }
        }
    }


    private PoHeader buildPoHeader(Map<String, Object> m) {

        PoHeader h = new PoHeader();

        //  h.setPoKey((String) m.get("CASE_NO"));
        h.setPoKey((String) m.get("POKEY"));
        h.setPoNo((String) m.get("PO_NO"));
        h.setL5PoNo((String) m.get("L5NO_PO"));
        h.setRlyCd((String) m.get("RLY_CD"));
        h.setRlyShortName((String) m.get("RLY_SHORTNAME"));

        h.setPurchaserCode((String) m.get("IMMS_PURCHASER_CODE"));
        h.setPurchaserDetail((String) m.get("IMMS_PURCHASER_DETAIL"));

        h.setVendorCode((String) m.get("IMMS_VENDOR_CODE"));
        h.setVendorDetails((String) m.get("VENDOR_DETAILS"));
        h.setFirmDetails((String) m.get("FIRM_DETAILS"));

        h.setStockNonStock((String) m.get("STOCK_NONSTOCK"));
        h.setRlyNonRly((String) m.get("RLY_NONRLY"));
        h.setPoOrLetter((String) m.get("PO_OR_LETTER"));

        h.setPoStatus((String) m.get("PO_STATUS"));
        h.setInspectingAgency((String) m.get("INSPECTING_AGENCY"));
        h.setPdfPath((String) m.get("PO_PDF_PATH"));

        h.setRegionCode((String) m.get("REGION_CODE"));
        h.setRemarks((String) m.get("REMARKS"));

        h.setBillPayOff((String) m.get("BILL_PAY_OFF"));
        h.setBillPayOffName((String) m.get("BILL_PAY_OFF_NAME"));

        h.setPoiCd((String) m.get("POI_CD"));

        h.setItemCat((String) m.get("ITEM_CAT"));
        h.setItemCatDescr((String) m.get("ITEM_CAT_DESCR"));


        if (m.get("PO_DT") != null)
            h.setPoDate(LocalDateTime.parse(m.get("PO_DT").toString(), PO_DT_FMT));

        if (m.get("RECV_DT") != null)
            h.setReceivedDate(LocalDateTime.parse(m.get("RECV_DT").toString(), TS_FMT));

        h.setSourceSystem("CRIS");
        return h;
    }


    private PoItem buildPoItem(Map<String, Object> m, PoHeader header) {

        PoItem i = new PoItem();
        i.setPoHeader(header);

        i.setCaseNo((String) m.get("CASE_NO"));
        //   i.setPoKey((String) m.get("POKEY"));
        i.setItemSrNo((String) m.get("ITEM_SRNO"));
        i.setPlNo((String) m.get("PL_NO"));
        i.setItemDesc((String) m.get("ITEM_DESC"));

        i.setConsigneeCd((String) m.get("CONSIGNEE_CD"));
        i.setImmsConsigneeCd((String) m.get("IMMS_CONSIGNEE_CD"));
        i.setImmsConsigneeName((String) m.get("IMMS_CONSIGNEE_NAME"));
        i.setConsigneeDetail((String) m.get("CONSIGNEE_DETAIL"));

        i.setConsigneeRly((String) m.get("CONSIGNEE_RLY"));
        i.setConsigneeRlyShortName((String) m.get("CONSIGNEE_RLY_SHORTNAME"));

        i.setPRly((String) m.get("P_RLY"));

        i.setBillPayOff((String) m.get("BILL_PAY_OFF"));
        i.setBillPayOffDesc((String) m.get("BILL_PAY_OFF_DESC"));
        i.setBillPassOff((String) m.get("BILL_PASS_OFF"));


        i.setUomCd((String) m.get("UOM_CD"));
        i.setUom((String) m.get("UOM"));

        if (m.get("QTY") != null)
            i.setQty(Integer.parseInt(m.get("QTY").toString()));

        if (m.get("QTY_CANCELLED") != null)
            i.setQtyCancelled(Integer.parseInt(m.get("QTY_CANCELLED").toString()));

        i.setRate(bd(m.get("RATE")));
        i.setBasicValue(bd(m.get("BASIC_VALUE")));
        i.setSalesTaxPercent(bd(m.get("SALES_TAX_PER")));
        i.setSalesTax(bd(m.get("SALES_TAX")));
        i.setDiscountType((String) m.get("DISCOUNT_TYPE"));
        i.setDiscountPercent(bd(m.get("DISCOUNT_PER")));
        i.setDiscount(bd(m.get("DISCOUNT")));
        i.setValue(bd(m.get("VALUE")));

        if (m.get("DELV_DT") != null)
            i.setDeliveryDate(LocalDateTime.parse(m.get("DELV_DT").toString(), PO_DT_FMT));

        if (m.get("EXT_DELV_DT") != null)
            i.setExtendedDeliveryDate(LocalDateTime.parse(m.get("EXT_DELV_DT").toString(), PO_DT_FMT));

        if (m.get("DATETIME") != null)
            i.setCrisTimestamp(LocalDateTime.parse(m.get("DATETIME").toString(), TS_FMT));

        i.setAllocation((String) m.get("ALLOCATION"));
        i.setUserId((String) m.get("USER_ID"));

        i.setSourceSystem("CRIS");
        return i;
    }

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
    public void createVendorIfNotExists(Map<String, Object> hdrMap) {

        String vendorCode = (String) hdrMap.get("IMMS_VENDOR_CODE");
        String vendorDetails = (String) hdrMap.get("VENDOR_DETAILS");
        String firmDetails = (String) hdrMap.get("FIRM_DETAILS");

        if (vendorCode == null || vendorCode.isBlank()) return;

        Optional<VendorMaster> existing =
                vendorMasterRepository.findByVendorCode(vendorCode);

        if (existing.isPresent()) return;

        //  Save Vendor Master
        VendorMaster vendor = new VendorMaster();
        vendor.setVendorCode(vendorCode);
        vendor.setVendorName(firmDetails);
        vendor.setVendorDetails(vendorDetails);
        vendor.setCreatedDate(LocalDateTime.now());
        vendorMasterRepository.save(vendor);

        if (userMasterRepository.existsByUserName(vendorCode)) {
            return;
        }

        userRequestDto dto = new userRequestDto();
        dto.setUserName(vendorCode);                     // unique vendor login
        dto.setPassword("Vendor@123");                   // or encoded inside service
        dto.setEmail(vendorCode + "@vendor.local");
        dto.setMobileNumber(null);
        //  dto.setRoleNames(List.of("Vendor"));
        String role = getVendorRole((String) hdrMap.get("ITEM_CAT_DESCR"));
        dto.setRoleNames(List.of(role));

        dto.setCreatedBy("Crics");
        dto.setEmployeeId(null);

        dto.setClusterName(null);
        dto.setRegionName(null);
        dto.setPriority(null);
        dto.setIeUserIds(null);

        userService.createUser(dto);
        //  String role = getVendorRole((String) hdrMap.get("ITEM_CAT_DESCR"));

        Optional<UserMaster> userOpt = userMasterRepository.findFirstByUserName(vendorCode);

        if (userOpt.isPresent() && role != null && !role.equals("Vendor")) {

            Integer userId = userOpt.get().getUserId();

            Optional<RoleMaster> roleId = roleMasterRepository.findByRoleName(role);

            RoleMaster roleIds=null;
            if (roleId.isPresent()){
                roleIds = roleId.get();
            }
            boolean exists = userRoleMasterRepository.existsByUserIdAndRoleId(userId, roleIds.getRoleId());

            if (!exists) {
                UserRoleMaster urm = new UserRoleMaster();
                urm.setUserId(userId);
                urm.setRoleId(roleIds.getRoleId());
                urm.setCreatedBy("CRIS");

                userRoleMasterRepository.save(urm);
            }
        }
    }

}
