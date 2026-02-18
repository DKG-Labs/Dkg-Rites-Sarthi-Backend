package com.sarthi.service.Impl;

import com.sarthi.dto.po.PoDataForSectionsDto;
import com.sarthi.entity.CricsPos.PoMaHeader;
import com.sarthi.entity.InspectionCallDetails;
import com.sarthi.entity.InventoryEntry;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.rawmaterial.RmInspectionDetails;
import com.sarthi.entity.rawmaterial.RmHeatQuantity;
import com.sarthi.repository.InspectionCallDetailsRepository;
import com.sarthi.repository.InventoryEntryRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoMaHeaderRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.rawmaterial.RmInspectionDetailsRepository;
import com.sarthi.repository.rawmaterial.RmHeatQuantityRepository;
import com.sarthi.service.PoDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for fetching PO data for Inspection Initiation Sections
 * Uses existing repositories without modification
 */
@Service
public class PoDataServiceImpl implements PoDataService {

    private static final Logger logger = LoggerFactory.getLogger(PoDataServiceImpl.class);

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private PoMaHeaderRepository poMaHeaderRepository;

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private InspectionCallDetailsRepository inspectionCallDetailsRepository;

    @Autowired
    private RmInspectionDetailsRepository rmInspectionDetailsRepository;

    @Autowired
    private RmHeatQuantityRepository rmHeatQuantityRepository;

    @Autowired
    private InventoryEntryRepository inventoryEntryRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public PoDataForSectionsDto getPoDataByPoNumber(String poNo) {
        // Find PO Header by PO Number using existing repository method
        List<PoHeader> poHeaders = poHeaderRepository.findAll();
        PoHeader poHeader = poHeaders.stream()
                .filter(h -> poNo.equals(h.getPoNo()))
                .findFirst()
                .orElse(null);

        if (poHeader == null) {
            return null;
        }

        // Find MA records for this PO using existing repository
        List<PoMaHeader> maHeaders = poMaHeaderRepository.findAll().stream()
                .filter(ma -> poNo.equals(ma.getPoNo()))
                .collect(Collectors.toList());

        // Transform to DTO
        return mapToDto(poHeader, maHeaders);
    }

    private PoDataForSectionsDto mapToDto(PoHeader poHeader, List<PoMaHeader> maHeaders) {
        return mapToDto(poHeader, maHeaders, null);
    }

    private PoDataForSectionsDto mapToDto(PoHeader poHeader, List<PoMaHeader> maHeaders, InspectionCall inspectionCall) {
        PoDataForSectionsDto dto = new PoDataForSectionsDto();

        // Section A: PO Header fields (from po_header table)
        dto.setRlyCd(poHeader.getRlyCd());
        dto.setPoNo(poHeader.getPoNo());

        // Set PO Serial Number and formatted fields from inspection call if available
        if (inspectionCall != null && inspectionCall.getPoSerialNo() != null) {
            dto.setPoSerialNo(inspectionCall.getPoSerialNo());
            dto.setRlyPoNo(poHeader.getRlyCd() + "/" + poHeader.getPoNo()); // RLY/PO_NO with / separator
            dto.setRlyPoNoSerial(poHeader.getRlyCd() + "/" + poHeader.getPoNo() + "/" + inspectionCall.getPoSerialNo()); // RLY/PO_NO/PO_SR

            // Use place_of_inspection from inspection_calls table, fallback to vendor details if null
            String placeOfInspection = inspectionCall.getPlaceOfInspection();
            dto.setInspPlace(placeOfInspection != null ? placeOfInspection : extractPlaceOfInspection(poHeader.getFirmDetails()));
        } else {
            dto.setPoSerialNo("N/A");
            dto.setRlyPoNo("N/A");
            dto.setRlyPoNoSerial("N/A");
            dto.setInspPlace(extractPlaceOfInspection(poHeader.getFirmDetails())); // Fallback to vendor details
        }

        dto.setPoDate(formatDateTime(poHeader.getPoDate()));

        dto.setVendorCode(poHeader.getVendorCode());
        dto.setVendorDetails(poHeader.getVendorDetails());
        dto.setVendorName(extractVendorName(poHeader.getVendorDetails()));

        dto.setPurchasingAuthority(poHeader.getPurchaserDetail() != null ?
                poHeader.getPurchaserDetail() : "Manager, Procurement");
        dto.setBillPayingOfficer("BPO-001"); // Default value - can be configured

        // Section B: PO Item fields (from po_item table)
        if (poHeader.getItems() != null && !poHeader.getItems().isEmpty()) {
            PoItem firstItem = poHeader.getItems().get(0);

            dto.setItemDesc(firstItem.getItemDesc());
            dto.setConsignee(firstItem.getImmsConsigneeName());
            dto.setConsigneeDetail(firstItem.getConsigneeDetail());
            dto.setUnit(firstItem.getUom() != null ? firstItem.getUom() : "Nos");
            dto.setDeliveryDate(formatDateTime(firstItem.getDeliveryDate()));
            dto.setExtendedDeliveryDate(formatDateTime(firstItem.getExtendedDeliveryDate()));
            dto.setPlNo(firstItem.getPlNo());

            // Calculate total PO Qty from all items
            int totalQty = poHeader.getItems().stream()
                    .mapToInt(item -> item.getQty() != null ? item.getQty() : 0)
                    .sum();
            dto.setPoQty(totalQty);
        }

        // Section B: Additional fields from inspection_calls and rm_inspection_details
        if (inspectionCall != null) {
            // Try to fetch Type of ERC from inspection_call_details table (Section B)
            // This is the approved value from Section B, which takes priority
            Optional<InspectionCallDetails> callDetailsOpt =
                inspectionCallDetailsRepository.findByInspectionCallNo(inspectionCall.getIcNumber());

            if (callDetailsOpt.isPresent() && callDetailsOpt.get().getTypeOfErc() != null) {
                // Use Type of ERC from Section B (inspection_call_details)
                dto.setErcType(callDetailsOpt.get().getTypeOfErc());
            } else {
                // Fallback to inspection_calls.erc_type if Section B not yet approved
                dto.setErcType(inspectionCall.getErcType());
            }

            // Fetch RM inspection details to get total_offered_qty_mt
            Optional<RmInspectionDetails> rmDetailsOpt = rmInspectionDetailsRepository.findByIcId(inspectionCall.getId());
            if (rmDetailsOpt.isPresent()) {
                dto.setTotalOfferedQtyMt(rmDetailsOpt.get().getTotalOfferedQtyMt());
            }
        }

        // Section C: Additional fields
        dto.setProductType("Raw Material"); // Default - can be determined from item type
        dto.setOrigDp(poHeader.getFirmDetails());
        dto.setExtDp("N/A");
        dto.setOrigDpStart(formatDateTime(poHeader.getPoDate()));

        // MA (Amendment) fields from po_ma_header table
        if (maHeaders != null && !maHeaders.isEmpty()) {
            // Set first MA details in main fields
            PoMaHeader firstMa = maHeaders.get(0);
            dto.setMaNo(firstMa.getMaNo());
            dto.setMaDate(firstMa.getMaDate() != null ?
                    firstMa.getMaDate().format(DATE_FORMATTER) : null);

            // Build MA list for all amendments
            List<PoDataForSectionsDto.MaInfoDto> maList = new ArrayList<>();
            for (PoMaHeader maHeader : maHeaders) {
                PoDataForSectionsDto.MaInfoDto maInfo = new PoDataForSectionsDto.MaInfoDto();
                maInfo.setMaNo(maHeader.getMaNo());
                maInfo.setMaDate(maHeader.getMaDate() != null ?
                        maHeader.getMaDate().format(DATE_FORMATTER) : null);
                maInfo.setSubject(maHeader.getSubject());
                maInfo.setOldPoValue(maHeader.getOldPoValue());
                maInfo.setNewPoValue(maHeader.getNewPoValue());
                maInfo.setMaType(maHeader.getMaType());
                maInfo.setStatus(maHeader.getStatus());
                maList.add(maInfo);
            }
            dto.setMaList(maList);

            // Get condition details from po_ma_detail if available
            if (firstMa.getDetails() != null && !firstMa.getDetails().isEmpty()) {
                dto.setPoCondSrNo(firstMa.getDetails().get(0).getCondSlno());
                dto.setCondTitle(firstMa.getDetails().get(0).getMaFldDescr());
                dto.setCondText(firstMa.getDetails().get(0).getNewValue());
            }
        } else {
            // No MA records
            dto.setMaNo("N/A");
            dto.setMaDate("N/A");
        }

        // Default condition fields if not set from MA
        if (dto.getCondTitle() == null) {
            dto.setCondTitle("N/A");
        }
        if (dto.getCondText() == null) {
            dto.setCondText("N/A");
        }
        if (dto.getPoCondSrNo() == null) {
            dto.setPoCondSrNo("N/A");
        }

        return dto;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().format(DATE_FORMATTER);
    }

    private String extractVendorName(String vendorDetails) {
        if (vendorDetails == null) {
            return null;
        }
        // Extract vendor name from format: "VENDOR NAME-CITY~address~..."
        String[] parts = vendorDetails.split("~");
        if (parts.length > 0) {
            return parts[0];
        }
        return vendorDetails;
    }

    private String extractPlaceOfInspection(String vendorDetails) {
        if (vendorDetails == null) {
            return "N/A";
        }
        // Extract city from vendor details
        String[] parts = vendorDetails.split("~");
        if (parts.length > 0) {
            String[] nameParts = parts[0].split("-");
            if (nameParts.length > 1) {
                return nameParts[nameParts.length - 1];
            }
        }
        return "Factory";
    }

    @Override
    public PoDataForSectionsDto getPoDataWithRmDetailsForSectionC(String poNo, String requestId) {
        // Find PO Header
        PoHeader poHeader = poHeaderRepository.findAll().stream()
                .filter(po -> poNo.equals(po.getPoNo()))
                .findFirst()
                .orElse(null);

        if (poHeader == null) {
            return null;
        }

        // Find MA records for this PO
        List<PoMaHeader> maHeaders = poMaHeaderRepository.findAll().stream()
                .filter(ma -> poNo.equals(ma.getPoNo()))
                .collect(Collectors.toList());

        // Find inspection call based on requestId or get the latest one
        InspectionCall targetCall = null;

        if (requestId != null && !requestId.trim().isEmpty()) {
            // Find specific inspection call by ic_number (requestId) - use direct method
            targetCall = inspectionCallRepository.findByIcNumber(requestId).orElse(null);

            if (targetCall == null) {
                // If specific call not found, return PO data without RM details and inspection call info
                return mapToDto(poHeader, maHeaders, null);
            }
        } else {
            // If no requestId provided, get the latest inspection call (backward compatibility)
            List<InspectionCall> inspectionCalls = inspectionCallRepository.findByPoNoOrderByCreatedAtDesc(poNo);
            if (inspectionCalls.isEmpty()) {
                return mapToDto(poHeader, maHeaders, null); // Return PO data without RM details
            }
            targetCall = inspectionCalls.get(0);
        }

        // Build DTO with inspection call data
        PoDataForSectionsDto dto = mapToDto(poHeader, maHeaders, targetCall);

        // Find RM inspection details for this call
        RmInspectionDetails rmDetails = rmInspectionDetailsRepository.findByIcId(targetCall.getId()).orElse(null);
        if (rmDetails == null) {
            return dto; // Return PO data without RM details
        }

        // Find all heat quantities for this RM detail
        List<RmHeatQuantity> heatQuantities = rmHeatQuantityRepository.findByRmDetailId(Math.toIntExact(rmDetails.getId()));

        // Build RM Heat Details list
        List<PoDataForSectionsDto.RmHeatDetailsDto> rmHeatDetailsList = new ArrayList<>();
        for (RmHeatQuantity heat : heatQuantities) {
            PoDataForSectionsDto.RmHeatDetailsDto heatDto = new PoDataForSectionsDto.RmHeatDetailsDto();

            // Fetch grade, raw material name, total_po, and tc_quantity from inventory_entries table based on heat + TC combination
            String rawMaterialName = rmDetails.getItemDescription(); // Default from rm_inspection_details
            String grade = "N/A"; // Default
            BigDecimal totalValueOfPo = null; // Default
            BigDecimal tcQuantity = null; // Default

            if (heat.getHeatNumber() != null && heat.getTcNumber() != null) {
                Optional<InventoryEntry> inventoryEntryOpt = inventoryEntryRepository
                        .findByHeatNumberAndTcNumber(heat.getHeatNumber(), heat.getTcNumber());

                if (inventoryEntryOpt.isPresent()) {
                    InventoryEntry inventoryEntry = inventoryEntryOpt.get();
                    // Override with inventory data if available
                    rawMaterialName = inventoryEntry.getRawMaterial();
                    grade = inventoryEntry.getGradeSpecification();
                    totalValueOfPo = inventoryEntry.getTotalPo();
                    tcQuantity = inventoryEntry.getTcQuantity();
                }
            }

            // From rm_inspection_details + inventory_entries
            heatDto.setRawMaterialName(rawMaterialName);
            heatDto.setGrade(grade);
            heatDto.setManufacturer(heat.getManufacturer() != null ? heat.getManufacturer() : rmDetails.getManufacturer());
            heatDto.setSubPoNumber(rmDetails.getSubPoNumber());
            heatDto.setSubPoDate(formatDate(rmDetails.getSubPoDate()));
            heatDto.setSubPoQty(rmDetails.getSubPoQty());
            heatDto.setInvoiceNumber(rmDetails.getInvoiceNumber());
            heatDto.setInvoiceDate(formatDate(rmDetails.getInvoiceDate()));

            // From rm_heat_quantities
            heatDto.setHeatNumber(heat.getHeatNumber());
            heatDto.setTcNumber(heat.getTcNumber());
            heatDto.setTcDate(formatDate(heat.getTcDate()));
            heatDto.setOfferedQty(heat.getOfferedQty());
            heatDto.setColorCode(heat.getColorCode()); // Color code manually entered by inspector

            // From inventory_entries (fetched by heat + TC combination)
            heatDto.setTotalValueOfPo(totalValueOfPo);
            heatDto.setTcQuantity(tcQuantity);

            rmHeatDetailsList.add(heatDto);
        }

        dto.setRmHeatDetails(rmHeatDetailsList);
        return dto;
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    @Override
    public boolean updateColorCode(Integer heatId, String colorCode) {
        try {
            Optional<RmHeatQuantity> heatOpt = rmHeatQuantityRepository.findById(heatId);

            if (heatOpt.isPresent()) {
                RmHeatQuantity heat = heatOpt.get();
                heat.setColorCode(colorCode);
                rmHeatQuantityRepository.save(heat);
                return true;
            }

            return false;
        } catch (Exception e) {
            logger.error("Error updating color code for heat ID: " + heatId, e);
            return false;
        }
    }
}

