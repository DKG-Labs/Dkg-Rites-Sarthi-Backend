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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
        PoHeader poHeader = poHeaderRepository.findByPoNo(ic.getPoNo())
                .orElse(null);

        // 4. Fetch MA (Amendment) data
        List<PoMaHeader> maHeaders = poMaHeaderRepository.findAll().stream()
                .filter(ma -> ic.getPoNo().equals(ma.getPoNo()))
                .collect(Collectors.toList());

        // 5. Fetch RM IC Mappings (heat numbers)
        List<ProcessRmIcMapping> rmMappings = processRmIcMappingRepository.findByProcessIcId(ic.getId());

        // 6. Build DTO
        ProcessInitiationDataDto dto = new ProcessInitiationDataDto();

        // Section A: PO Information (from PoHeader and InspectionCall)
        dto.setPoNo(ic.getPoNo());

        // Fetch PO Item data for quantity and other details
        Integer poQty = null;
        String poUnit = "Nos";
        String consignee = "N/A";
        String deliveryDate = "N/A";

        if (poHeader != null) {
            dto.setPoDate(poHeader.getPoDate() != null ? poHeader.getPoDate().format(DATE_TIME_FORMATTER) : null);
            dto.setVendorName(poHeader.getVendorDetails());
            dto.setVendorCode(poHeader.getVendorCode());
            dto.setPurchasingAuthority(poHeader.getPurchaserDetail());

            // Get PO quantity from first item (if available)
            if (poHeader.getItems() != null && !poHeader.getItems().isEmpty()) {
                var firstItem = poHeader.getItems().get(0);
                poQty = firstItem.getQty();
                poUnit = firstItem.getUom() != null ? firstItem.getUom() : "Nos";
                consignee = firstItem.getConsigneeDetail() != null ? firstItem.getConsigneeDetail() : "N/A";
                if (firstItem.getDeliveryDate() != null) {
                    deliveryDate = firstItem.getDeliveryDate().format(DATE_TIME_FORMATTER);
                }
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
        dto.setPoUnit(poUnit);
        dto.setConsignee(consignee);
        dto.setDeliveryDate(deliveryDate);
        dto.setBillPayingOfficer("N/A");

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
        dto.setRmIcNumber(processDetails.getRmIcNumber() != null ? processDetails.getRmIcNumber() : "N/A");
        dto.setLotNumber(processDetails.getLotNumber() != null ? processDetails.getLotNumber() : "N/A");
        dto.setHeatNumber(processDetails.getHeatNumber() != null ? processDetails.getHeatNumber() : "N/A");
        dto.setOfferedQty(processDetails.getOfferedQty() != null ? processDetails.getOfferedQty() : 0); // CALL QTY for Section B

        // Build list of all lots for this inspection call
        List<ProcessInitiationDataDto.LotDetailsInfo> lotDetailsList = new ArrayList<>();
        for (ProcessInspectionDetails lot : processDetailsList) {
            ProcessInitiationDataDto.LotDetailsInfo lotInfo = new ProcessInitiationDataDto.LotDetailsInfo();
            lotInfo.setRmIcNumber(lot.getRmIcNumber() != null ? lot.getRmIcNumber() : "N/A");
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
}

