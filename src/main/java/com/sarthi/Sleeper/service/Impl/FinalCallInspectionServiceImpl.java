package com.sarthi.Sleeper.service.Impl;

import com.sarthi.Sleeper.dto.FinalCalDtos.*;
import com.sarthi.Sleeper.entity.FInalCall.FinalCallInspectionSectionB;
import com.sarthi.Sleeper.entity.FInalCall.FinalCallnspectionSectionA;
import com.sarthi.Sleeper.entity.FInalCall.SleeperSchedule;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import com.sarthi.Sleeper.repository.FinalCallInspectionSectionBRepository;
import com.sarthi.Sleeper.repository.FinalCallnspectionSectionARepository;
import com.sarthi.Sleeper.repository.FinalInspectionRepository.SleeperInspectionCallRepository;
import com.sarthi.Sleeper.repository.SleeperScheduleRepository;
import com.sarthi.Sleeper.service.FinalCallInspectionService;
import com.sarthi.constant.AppConstant;
import com.sarthi.entity.PoHeader;
import com.sarthi.entity.PoItem;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.PoHeaderRepository;
import com.sarthi.repository.PoItemRepository;
import com.sarthi.util.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinalCallInspectionServiceImpl implements FinalCallInspectionService {


        private final SleeperInspectionCallRepository callRepo;
        private final PoHeaderRepository poHeaderRepo;
        private final PoItemRepository poItemRepo;

        private final FinalCallnspectionSectionARepository  repoFinalSectionA;
        private final FinalCallInspectionSectionBRepository  repoSectionb;

        private final SleeperScheduleRepository sleeperScheduleRepository;

        private final NotificationService notificationService;
        public InspectionCallSection1Response getDetails(String callNo) {

            SleeperInspectionCall call = callRepo.findByCallNo(callNo)
                    .orElseThrow(() -> new RuntimeException("Call not found"));

            PoHeader poHeader = poHeaderRepo.findByPoNo(call.getPoNo())
                    .orElseThrow(() -> new RuntimeException("PO not found"));

            PoItem poItem = poItemRepo.findByPoHeader_PoNoAndItemSrNo(call.getPoNo(), call.getSrNo())
                    .orElseThrow(() -> new RuntimeException("PO Item not found"));

            InspectionCallSection1Response res = new InspectionCallSection1Response();

            // Mapping UI fields
            res.setRlyPoNo(poHeader.getRlyShortName() + " / " + poHeader.getPoNo() + " / " + call.getSrNo());
            res.setPoDate(poHeader.getPoDate());
            res.setPoQty(poItem.getQty());
            res.setVendorName(poHeader.getVendorDetails());
            res.setMaNo("N/A"); // not available in entity
            res.setMaDate("N/A");
            res.setPurchasingAuthority(poHeader.getPurchaserDetail());
            res.setBillPayingOfficer(poHeader.getBillPayOff());

            return res;
        }


    public InspectionCallSection2DetailsResponse getSectionB(String callNo) {

        SleeperInspectionCall call = callRepo.findByCallNo(callNo)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        PoHeader poHeader = poHeaderRepo.findByPoNo(call.getPoNo())
                .orElseThrow(() -> new RuntimeException("PO not found"));

        PoItem poItem = poItemRepo
                .findByPoHeader_PoNoAndItemSrNo(call.getPoNo(), call.getSrNo())
                .orElseThrow(() -> new RuntimeException("PO Item not found"));

        InspectionCallSection2DetailsResponse res = new InspectionCallSection2DetailsResponse();

        res.setInspectionCallNo(call.getCallNo());
        res.setInspectionCallDate(call.getCreatedAt());

        // Desired Inspection Date
        res.setInspectionDesiredDate(call.getDesiredInspectionDate());

        res.setRlyPoSr(poHeader.getRlyShortName() + "/" + poHeader.getPoNo() + "/" + call.getSrNo());

        res.setItemDesc(poItem.getItemDesc());

        res.setProductType(null);
        res.setTypeOfErc(call.getSleeperType());

        res.setPoSrQtyUnit(poItem.getQty() + " " + poItem.getUom());

        res.setConsignee(
                poItem.getConsigneeRly() + "/" + poItem.getConsigneeDetail()
        );

        res.setOrigDp(poItem.getDeliveryDate());
        res.setExtDp(poItem.getExtendedDeliveryDate());

        res.setOrigDpStart(null);

        res.setStageOfInspection(call.getStatus());

        res.setCallQtyMt(call.getTotalOffered());

        res.setPlaceOfInspection(poHeader.getFirmDetails());

        res.setProcessIcNumbers(null);
        res.setRemarks(null);

        return res;
    }

    public SectionARequest create(SectionARequest req) {

        FinalCallnspectionSectionA entity = new FinalCallnspectionSectionA();

        entity.setCallNo(req.getCallNo());
        entity.setRlyPoNo(req.getRlyPoNo());
        entity.setPoDate(req.getPoDate());
        entity.setPoQty(req.getPoQty());
        entity.setVendorName(req.getVendorName());

        entity.setMaNo(req.getMaNo());
        entity.setMaDate(req.getMaDate());

        entity.setPurchasingAuthority(req.getPurchasingAuthority());
        entity.setBillPayingOfficer(req.getBillPayingOfficer());

        entity.setPlantId(req.getPlantId());
        entity.setVendorCode(req.getVendorCode());
        entity.setShift(req.getShift());

        entity.setCreatedBy(req.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        FinalCallnspectionSectionA saved =
                repoFinalSectionA.save(entity);

        return mapToResponse(saved);
    }

    private SectionARequest mapToResponse(FinalCallnspectionSectionA entity) {

        SectionARequest res = new SectionARequest();

        res.setCallNo(entity.getCallNo());
        res.setRlyPoNo(entity.getRlyPoNo());
        res.setPoDate(entity.getPoDate());
        res.setPoQty(entity.getPoQty());
        res.setVendorName(entity.getVendorName());

        res.setMaNo(entity.getMaNo());
        res.setMaDate(entity.getMaDate());

        res.setPurchasingAuthority(entity.getPurchasingAuthority());
        res.setBillPayingOfficer(entity.getBillPayingOfficer());

        res.setPlantId(entity.getPlantId());
        res.setVendorCode(entity.getVendorCode());
        res.setShift(entity.getShift());

        res.setCreatedBy(entity.getCreatedBy());

        return res;
    }


    public SectionBRequest create(SectionBRequest req) {

       FinalCallInspectionSectionB entity = new FinalCallInspectionSectionB();

        entity.setCallNo(req.getCallNo());

        entity.setInspectionCallDate(req.getInspectionCallDate());
        entity.setInspectionDesiredDate(req.getInspectionDesiredDate());

        entity.setRlyPoSr(req.getRlyPoSr());
        entity.setItemDesc(req.getItemDesc());

        entity.setProductType(req.getProductType());
        entity.setTypeOfErc(req.getTypeOfErc());

        entity.setPoSrQtyUnit(req.getPoSrQtyUnit());
        entity.setConsignee(req.getConsignee());

        entity.setOrigDp(req.getOrigDp());
        entity.setExtDp(req.getExtDp());
        entity.setOrigDpStart(req.getOrigDpStart());

        entity.setStageOfInspection(req.getStageOfInspection());
        entity.setCallQtyMt(req.getCallQtyMt());

        entity.setPlaceOfInspection(req.getPlaceOfInspection());

        entity.setProcessIcNumbers(req.getProcessIcNumbers());
        entity.setRemarks(req.getRemarks());

        entity.setPlantId(req.getPlantId());
        entity.setVendorCode(req.getVendorCode());
        entity.setShift(req.getShift());

        entity.setCreatedBy(req.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        FinalCallInspectionSectionB res = repoSectionb.save(entity);

        return mapToSectionBResponse(res);
    }

    private SectionBRequest mapToSectionBResponse(FinalCallInspectionSectionB entity) {

        SectionBRequest res = new SectionBRequest();

        res.setCallNo(entity.getCallNo());

        res.setInspectionCallDate(entity.getInspectionCallDate());
        res.setInspectionDesiredDate(entity.getInspectionDesiredDate());

        res.setRlyPoSr(entity.getRlyPoSr());
        res.setItemDesc(entity.getItemDesc());

        res.setProductType(entity.getProductType());
        res.setTypeOfErc(entity.getTypeOfErc());

        res.setPoSrQtyUnit(entity.getPoSrQtyUnit());
        res.setConsignee(entity.getConsignee());

        res.setOrigDp(entity.getOrigDp());
        res.setExtDp(entity.getExtDp());
        res.setOrigDpStart(entity.getOrigDpStart());

        res.setStageOfInspection(entity.getStageOfInspection());
        res.setCallQtyMt(entity.getCallQtyMt());

        res.setPlaceOfInspection(entity.getPlaceOfInspection());

        res.setProcessIcNumbers(entity.getProcessIcNumbers());
        res.setRemarks(entity.getRemarks());

        res.setPlantId(entity.getPlantId());
        res.setVendorCode(entity.getVendorCode());
        res.setShift(entity.getShift());

        res.setCreatedBy(entity.getCreatedBy());

        return res;
    }

    public SleeperScheduleRequest create(SleeperScheduleRequest req) {

        if (sleeperScheduleRepository.existsByCallNo(req.getCallNo())) {
            req.setUpdatedBy(req.getCreatedBy());
            return update(req);
        }

        SleeperSchedule entity = new SleeperSchedule();

        entity.setCallNo(req.getCallNo());
        entity.setScheduleDate(req.getScheduleDate());
        entity.setReason(req.getReason());

        entity.setPlantId(req.getPlantId());
        entity.setVendorCode(req.getVendorCode());
        entity.setShift(req.getShift());

        entity.setCreatedBy(req.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());

        SleeperSchedule saved = sleeperScheduleRepository.save(entity);
        String productType = "SLLEPER";
        notificationService.sendInspectionScheduledNotification(
                productType,
                req.getCallNo(),
                Math.toIntExact(req.getCreatedBy())
        );
        return mapToResponse(saved);
    }

    public SleeperScheduleRequest update(SleeperScheduleRequest req) {

        SleeperSchedule entity = sleeperScheduleRepository.findByCallNo(req.getCallNo())
                .orElse(null);

        if (entity == null) {
            return create(req);
        }

        entity.setCallNo(req.getCallNo());
        entity.setScheduleDate(req.getScheduleDate());
        entity.setReason(req.getReason());

        if (req.getPlantId() != null) entity.setPlantId(req.getPlantId());
        if (req.getVendorCode() != null) entity.setVendorCode(req.getVendorCode());
        if (req.getShift() != null) entity.setShift(req.getShift());

        entity.setUpdatedBy(req.getUpdatedBy());
        entity.setUpdatedDate(LocalDateTime.now());

        SleeperSchedule updated = sleeperScheduleRepository.save(entity);

        return mapToResponse(updated);
    }

    private SleeperScheduleRequest mapToResponse(SleeperSchedule entity) {

        SleeperScheduleRequest res = new SleeperScheduleRequest();

        res.setCallNo(entity.getCallNo());
        res.setScheduleDate(entity.getScheduleDate());
        res.setReason(entity.getReason());

        res.setPlantId(entity.getPlantId());
        res.setVendorCode(entity.getVendorCode());
        res.setShift(entity.getShift());

        res.setCreatedBy(entity.getCreatedBy());

        return res;
    }

    @Override
    public SleeperScheduleRequest getSchedule(String callNo) {
        return sleeperScheduleRepository.findByCallNo(callNo)
                .map(this::mapToResponse)
                .orElse(null);
    }

}
