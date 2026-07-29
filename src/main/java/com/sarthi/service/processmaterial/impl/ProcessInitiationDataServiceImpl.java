package com.sarthi.service.processmaterial.impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.processmaterial.ProcessInitiationDataDto;
import com.sarthi.dto.processmaterial.ProcessInitiationDataDto.RmIcHeatInfo;
import com.sarthi.entity.InventoryEntry;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.CricsPos.PoMaHeader;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessRmIcMapping;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.InventoryEntryRepository;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoMaHeaderRepository;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.processmaterial.ProcessRmIcMappingRepository;
import com.sarthi.service.processmaterial.ProcessInitiationDataService;
import com.sarthi.repository.rawmaterial.RmIcEditRepository;
import com.sarthi.entity.rawmaterial.RmIcEdit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProcessInitiationDataServiceImpl implements ProcessInitiationDataService {

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProcessInspectionDetailsRepository processDetailsRepository;

    @Autowired
    private ProcessRmIcMappingRepository processRmIcMappingRepository;

    @Autowired
    private PoHeaderRepository poHeaderRepository;

    @Autowired
    private PoMaHeaderRepository poMaHeaderRepository;

    @Autowired
    private InventoryEntryRepository inventoryEntryRepository;

    @Autowired
    private RmIcEditRepository rmIcEditRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public ProcessInitiationDataDto getInitiationDataByCallNo(String callNo) {
        log.info("Fetching Process initiation data for call: {}", callNo);

        // 1. Fetch Inspection Call
        InspectionCall ic = inspectionCallRepository.findByIcNumber(callNo)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.NO_RECORD_FOUND,
                                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                AppConstant.ERROR_TYPE_VALIDATION,
                                "Inspection Call not found: " + callNo
                        )
                ));

        // 2. Fetch Process Inspection Details (get first lot if multiple lots exist)
        List<ProcessInspectionDetails> processDetailsList = processDetailsRepository.findByIcId(ic.getId());
        if (processDetailsList.isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.NO_RECORD_FOUND,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Process inspection details not found for call: " + callNo
                    )
            );
        }
        // Use the first lot's details (primary lot)
        ProcessInspectionDetails processDetails = processDetailsList.get(0);

        // 3. Fetch PO Header data
        PoHeader poHeader = poHeaderRepository.findByPoNoWithItems(ic.getPoNo())
                .orElse(null);

        // 4. Fetch MA (Amendment) data using targeted query
        List<PoMaHeader> maHeaders = poMaHeaderRepository.findByPoNo(ic.getPoNo());

        // 5. Fetch RM IC Mappings (heat numbers)
        List<ProcessRmIcMapping> rmMappings = processRmIcMappingRepository.findByProcessIcId(ic.getId());

        // 6. Build DTO
        ProcessInitiationDataDto dto = new ProcessInitiationDataDto();

        // Section A: PO Information (from PoHeader and InspectionCall)
        String rlyPrefix = null;
        if (poHeader != null) {
            rlyPrefix = poHeader.getRlyShortName();
            if (rlyPrefix == null || rlyPrefix.length() > 6) {
                rlyPrefix = poHeader.getRlyCd();
            }
        }
        
        String formattedPoNo = ic.getPoNo();
        String rlyPoNo = formattedPoNo;
        if (rlyPrefix != null) {
            rlyPoNo = rlyPrefix + " / " + formattedPoNo;
        }
        
        String rlyPoNoSerial = rlyPoNo;
        if (ic.getPoSerialNo() != null) {
            String srNo = ic.getPoSerialNo();
            if (srNo.contains("/")) {
                String[] parts = srNo.split("/");
                srNo = parts[parts.length - 1];
            }
            rlyPoNoSerial = rlyPoNo + " / " + srNo;
        }
        dto.setPoNo(formattedPoNo); // Keep raw poNo mapped
        dto.setRlyPoNo(rlyPoNo); // Map Rly / PO No
        dto.setRlyPoNoSerial(rlyPoNoSerial); // Map Rly / PO No / Sr No

        // Fetch PO Item data for quantity and other details
        Integer poQty = null;
        Integer poSrQty = null;
        String poUnit = "Nos";
        String consignee = "N/A";
        String deliveryDate = "N/A";

        if (poHeader != null) {
            dto.setPoDate(poHeader.getPoDate() != null ? poHeader.getPoDate().format(DATE_TIME_FORMATTER) : null);
            dto.setVendorName(poHeader.getVendorDetails());
            dto.setVendorCode(poHeader.getVendorCode());
            dto.setPurchasingAuthority(poHeader.getPurchaserDetail());

            if (poHeader.getItems() != null && !poHeader.getItems().isEmpty()) {
                // Find item matching PO Serial No
                java.util.Optional<com.sarthi.entity.PoItem> matchedItem = poHeader.getItems().stream()
                        .filter(item -> {
                            String target = ic.getPoSerialNo();
                            String current = item.getItemSrNo();
                            if (target == null || current == null) return false;
                            
                            if (target.contains("/")) {
                                String[] parts = target.split("/");
                                target = parts[parts.length - 1];
                            }
                            
                            if (target.trim().equals(current.trim())) return true;
                            
                            try {
                                return Integer.parseInt(target.trim()) == Integer.parseInt(current.trim());
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        })
                        .findFirst();
                
                com.sarthi.entity.PoItem referenceItem = matchedItem.orElse(poHeader.getItems().get(0));
                
                poSrQty = referenceItem.getQty();
                poUnit = referenceItem.getUom() != null ? referenceItem.getUom() : "Nos";
                consignee = referenceItem.getConsigneeDetail() != null ? referenceItem.getConsigneeDetail() : "N/A";
                if (referenceItem.getDeliveryDate() != null) {
                    deliveryDate = referenceItem.getDeliveryDate().format(DATE_TIME_FORMATTER);
                }

                int totalQty = poHeader.getItems().stream()
                        .mapToInt(item -> item.getQty() != null ? item.getQty() : 0)
                        .sum();
                poQty = totalQty;
            }
        }

        // Amendment data from MA headers
        if (!maHeaders.isEmpty()) {
            PoMaHeader latestMa = maHeaders.get(0);
            dto.setAmendmentNo(latestMa.getMaNo());
            dto.setAmendmentDate(latestMa.getMaDate() != null ? latestMa.getMaDate().format(DATE_TIME_FORMATTER) : null);
        } else {
            dto.setAmendmentNo("N/A");
            dto.setAmendmentDate("N/A");
        }

        // Set PO fields
        dto.setPoDescription("Process Material Inspection");
        dto.setPoQty(poQty != null ? poQty : 0);
        dto.setPoSrQty(poSrQty != null ? poSrQty : 0);
        dto.setPoUnit(poUnit);
        dto.setConsignee(consignee);
        dto.setDeliveryDate(deliveryDate);
        dto.setBillPayingOfficer("N/A");
        dto.setIbsCaseNo(poHeader != null ? poHeader.getCaseNo() : "");

        // Section B: Inspection Call Details
        dto.setCallNo(ic.getIcNumber());
        dto.setCallDate(ic.getDesiredInspectionDate() != null ? ic.getDesiredInspectionDate().format(DATE_TIME_FORMATTER) : null);
        dto.setDesiredInspectionDate(ic.getDesiredInspectionDate() != null ? ic.getDesiredInspectionDate().format(DATE_TIME_FORMATTER) : null);
        dto.setTypeOfCall(ic.getTypeOfCall());
        dto.setTypeOfErc(ic.getErcType()); // Type of ERC from inspection_calls table
        dto.setPlaceOfInspection(ic.getPlaceOfInspection());
        dto.setCompanyName(ic.getCompanyName());
        dto.setUnitName(ic.getUnitName());
        dto.setUnitAddress(ic.getUnitAddress());

        // RM IC Number, Lot Number, Heat Number, and Offered Qty - from process_inspection_details table
        dto.setRmIcNumber(getFormattedRmIcsWithDates(processDetails.getRmIcNumber()));
        dto.setLotNumber(processDetails.getLotNumber() != null ? processDetails.getLotNumber() : "N/A");
        dto.setHeatNumber(processDetails.getHeatNumber() != null ? processDetails.getHeatNumber() : "N/A");
        dto.setOfferedQty(processDetails.getOfferedQty() != null ? processDetails.getOfferedQty() : 0); // CALL QTY for Section B

        // Build list of all lots for this inspection call
        List<ProcessInitiationDataDto.LotDetailsInfo> lotDetailsList = new ArrayList<>();
        for (ProcessInspectionDetails lot : processDetailsList) {
            ProcessInitiationDataDto.LotDetailsInfo lotInfo = new ProcessInitiationDataDto.LotDetailsInfo();
            lotInfo.setRmIcNumber(getFormattedRmIcsWithDates(lot.getRmIcNumber()));
            lotInfo.setLotNumber(lot.getLotNumber() != null ? lot.getLotNumber() : "N/A");
            lotInfo.setHeatNumber(lot.getHeatNumber() != null ? lot.getHeatNumber() : "N/A");
            lotInfo.setManufacturer(lot.getManufacturer() != null ? lot.getManufacturer() : "N/A");
            lotInfo.setManufacturerHeat(lot.getManufacturerHeat() != null ? lot.getManufacturerHeat() : "N/A");
            lotInfo.setOfferedQty(lot.getOfferedQty() != null ? lot.getOfferedQty() : 0);
            lotInfo.setTotalAcceptedQtyRm(lot.getTotalAcceptedQtyRm() != null ? lot.getTotalAcceptedQtyRm() : 0);
            lotDetailsList.add(lotInfo);
        }
        dto.setLotDetailsList(lotDetailsList);
        log.info("✅ Built lot details list with {} lots", lotDetailsList.size());

        // Section C: RM IC Heat Information from inventory_entry table
        List<RmIcHeatInfo> heatInfoList = new ArrayList<>();

        // Get heat number from process_inspection_details
        String heatNumber = processDetails.getHeatNumber();

        if (heatNumber != null && !heatNumber.isEmpty()) {
            // Fetch inventory entry by heat number
            List<InventoryEntry> inventoryEntries = inventoryEntryRepository.findByHeatNumber(heatNumber);

            if (!inventoryEntries.isEmpty()) {
                // Use the first matching inventory entry
                InventoryEntry inventory = inventoryEntries.get(0);

                RmIcHeatInfo heatInfo = new RmIcHeatInfo();
                heatInfo.setRmIcNumber(processDetails.getRmIcNumber() != null ? processDetails.getRmIcNumber() : "N/A");
                heatInfo.setHeatNumber(inventory.getHeatNumber());
                heatInfo.setManufacturer(inventory.getSupplierName());
                heatInfo.setRawMaterialName(inventory.getRawMaterial());
                heatInfo.setGradeSpec(inventory.getGradeSpecification());
                heatInfo.setTcNumber(inventory.getTcNumber());
                heatInfo.setTcDate(inventory.getTcDate() != null ? inventory.getTcDate().format(DATE_TIME_FORMATTER) : null);
                heatInfo.setSubPoNumber(inventory.getSubPoNumber());
                heatInfo.setSubPoDate(inventory.getSubPoDate() != null ? inventory.getSubPoDate().format(DATE_TIME_FORMATTER) : null);
                heatInfo.setInvoiceNumber(inventory.getInvoiceNumber());
                heatInfo.setInvoiceDate(inventory.getInvoiceDate() != null ? inventory.getInvoiceDate().format(DATE_TIME_FORMATTER) : null);
                heatInfo.setSubPoQty(inventory.getSubPoQty() != null ? inventory.getSubPoQty().toString() : null);
                heatInfo.setTcQuantity(inventory.getTcQuantity() != null ? inventory.getTcQuantity().toString() : null);
                heatInfo.setUnit(inventory.getUnitOfMeasurement());
                heatInfo.setQtyAccepted(processDetails.getOfferedQty());

                heatInfoList.add(heatInfo);
                log.info("✅ Fetched inventory data for heat number: {}", heatNumber);
            } else {
                log.warn("⚠️ No inventory entry found for heat number: {}", heatNumber);
            }
        } else {
            log.warn("⚠️ No heat number found in process_inspection_details");
        }

        dto.setRmIcHeatInfoList(heatInfoList);

        log.info("Successfully fetched initiation data for Process call: {}", callNo);
        return dto;
    }

    private String getRmIcDateFromEditTable(String icNumber) {
        if (icNumber == null || icNumber.isEmpty() || "N/A".equals(icNumber)) {
            return null;
        }
        String cleanIc = icNumber.trim();
        Optional<RmIcEdit> rmIcEditOpt = rmIcEditRepository.findByIcNumber(cleanIc);
        if (rmIcEditOpt.isPresent() && rmIcEditOpt.get().getCreatedAt() != null) {
            return rmIcEditOpt.get().getCreatedAt().format(DATE_TIME_FORMATTER);
        }
        return null;
    }

    private String getFormattedRmIcsWithDates(String rmIcNumberField) {
        if (rmIcNumberField == null || rmIcNumberField.trim().isEmpty() || "N/A".equals(rmIcNumberField.trim())) {
            return "N/A";
        }
        String[] parts = rmIcNumberField.split(",");
        List<String> formattedParts = new ArrayList<>();
        for (String part : parts) {
            String trimmedPart = part.trim();
            if (trimmedPart.isEmpty()) continue;
            String dateStr = getRmIcDateFromEditTable(trimmedPart);
            if (dateStr != null) {
                formattedParts.add(trimmedPart + " dated " + dateStr);
            } else {
                formattedParts.add(trimmedPart);
            }
        }
        return String.join(", ", formattedParts);
    }
}

