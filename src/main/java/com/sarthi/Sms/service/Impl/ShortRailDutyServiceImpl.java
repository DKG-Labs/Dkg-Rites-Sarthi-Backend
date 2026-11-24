package com.sarthi.Sms.service.Impl;

import com.sarthi.Sms.constant.RailMillAppConstant;
import com.sarthi.Sms.dto.sms.common.DutyStatusResDto;
import com.sarthi.Sms.dto.sms.common.EndDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyReqDto;
import com.sarthi.Sms.dto.sms.common.StartDutyResDto;
import com.sarthi.Sms.dto.shortrail.IdDto;
import com.sarthi.Sms.dto.shortrail.MasterByIdDto;
import com.sarthi.Sms.dto.shortrail.RejectionAnalysisDtlsDto;
import com.sarthi.Sms.dto.shortrail.RejectionAnalysisDtlsResDto;
import com.sarthi.Sms.dto.shortrail.ShortRailMasterDto;
import com.sarthi.Sms.dto.shortrail.ShortRailMasterResDto;
import com.sarthi.Sms.dto.shortrail.ShortRailSummaryResDto;
import com.sarthi.Sms.dto.shortrail.SriRgRsDeleteDto;
import com.sarthi.Sms.dto.shortrail.WorkstationRemarksDto;
import com.sarthi.Sms.entity.shortrail.ShortRailDutyEntity;
import com.sarthi.Sms.entity.shortrail.ShortRailMasterEntity;
import com.sarthi.Sms.entity.shortrail.ShortRailMasterKey;
import com.sarthi.Sms.entity.shortrail.ShortRailRejectionAnalysisEntity;
import com.sarthi.Sms.exception.SmsErrorDetails;
import com.sarthi.Sms.exception.SmsInvalidArgumentException;
import com.sarthi.Sms.exception.SmsResourceNotFoundException;
import com.sarthi.Sms.repository.shortrail.ShortRailDutyRepository;
import com.sarthi.Sms.repository.shortrail.ShortRailMasterRepository;
import com.sarthi.Sms.repository.shortrail.ShortRailRejectionAnalysisRepository;
import com.sarthi.Sms.service.DutySequenceService;
import com.sarthi.Sms.service.ShortRailService;
import com.sarthi.Sms.util.RailMillCommonUtils;
import com.sarthi.Sms.util.DutyEnum;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShortRailDutyServiceImpl implements ShortRailService {

  @Autowired
  private ShortRailDutyRepository shortRailDutyRepository;

  @Autowired
  private DutySequenceService dss;

  @Autowired
  private ShortRailMasterRepository srmr;

  @Autowired
  private ShortRailRejectionAnalysisRepository srrar;

  @Override
  @Transactional
  public StartDutyResDto startDuty(String authorizationHeader, StartDutyReqDto request) {
    ShortRailDutyEntity entity = new ShortRailDutyEntity();
    String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);

    // Check if a duty is already in progress
    DutyStatusResDto statusResDto = checkDutyStatus(authorizationHeader);
    if (statusResDto.getIsDutyInProgess()) {
      throw new SmsInvalidArgumentException(new SmsErrorDetails(
          RailMillAppConstant.USER_ALREADY_EXISTS,
          RailMillAppConstant.USER_ALREADY_EXISTS,
          RailMillAppConstant.ERROR_TYPE_VALIDATION,
          "Cannot start a new duty. Previous duty already in progress."));
    }

    LocalDate date = RailMillCommonUtils.convertStringToDateObject(request.getStartDate());

    // Generate a unique Duty ID
    String dutyId = dss.generateDutyId(DutyEnum.SRI, date);
    entity.setDutyId(dutyId);
    entity.setUserId(userId);
    entity.setDate(date);
    entity.setShift(request.getShift());
    entity.setStartTime(LocalDateTime.now());

    // Save the duty record
    entity = shortRailDutyRepository.save(entity);

    // Prepare response DTO
    StartDutyResDto responseDto = new StartDutyResDto();
    responseDto.setDate(RailMillCommonUtils.convertDateToString(entity.getDate()));
    responseDto.setDutyId(entity.getDutyId());
    responseDto.setShift(entity.getShift());

    responseDto.setStartTime(RailMillCommonUtils.extractTime(entity.getStartTime()));

    return responseDto;
  }

  @Override
  @Transactional
  public void endDuty(EndDutyReqDto request) {
    ShortRailDutyEntity entity = shortRailDutyRepository.findByDutyId(request.getDutyId())
        .orElseThrow(() -> new SmsResourceNotFoundException(new SmsErrorDetails(
            RailMillAppConstant.ERROR_CODE_RESOURCE,
            RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
            RailMillAppConstant.ERROR_TYPE_RESOURCE,
            "No duties found for the provided duty ID.")));

    // Check if the duty has already ended
    if (Objects.nonNull(entity.getEndTime())) {
      throw new SmsInvalidArgumentException(new SmsErrorDetails(
          RailMillAppConstant.ERROR_CODE_VALIDATION,
          RailMillAppConstant.ERROR_TYPE_CODE_VALIDATION,
          RailMillAppConstant.ERROR_TYPE_VALIDATION,
          "Duty already ended."));
    }

    // Set end time and remarks
    entity.setShiftRemarks(request.getShiftRemarks());
    entity.setEndTime(LocalDateTime.now());
    shortRailDutyRepository.save(entity);
  }

  @Override
  public DutyStatusResDto checkDutyStatus(String authorizationHeader) {
    String userId = RailMillCommonUtils.getUserIdFromAuthHeader(authorizationHeader);

    // Check ongoing duty for the user
    String dutyId = shortRailDutyRepository.checkDutyStatus(userId);
    DutyStatusResDto responseDto = new DutyStatusResDto();
    responseDto.setDutyId(dutyId);
    responseDto.setIsDutyInProgess(dutyId != null);

    return responseDto;
  }

  @Override
  public StartDutyResDto getOngoingDutyDtls(String ah) {
    String userId = RailMillCommonUtils.getUserIdFromAuthHeader(ah);
    Optional<ShortRailDutyEntity> rdeOpt = shortRailDutyRepository.getOngoingDutyDtls(userId);

    StartDutyResDto sdrd = new StartDutyResDto();
    if (rdeOpt.isPresent()) {
      ShortRailDutyEntity rde = rdeOpt.get();
      sdrd.setDutyId(rde.getDutyId());
      sdrd.setStartTime(RailMillCommonUtils.extractTime(rde.getStartTime()));
      sdrd.setShift(rde.getShift());
      sdrd.setDate(RailMillCommonUtils.convertDateToString(rde.getDate()));

    }

    return sdrd;
  }

  @Override
  @Transactional
  public void saveShortRailMasterDetails(ShortRailMasterDto req) {
    // Validate if the related Short Rail Duty exists
    ShortRailDutyEntity dutyEntity = shortRailDutyRepository.findById(req.getDutyId())
        .orElseThrow(() -> new SmsResourceNotFoundException(
            new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "No duty found for the provided duty ID.")));

    // Map DTO to Entity
    ShortRailMasterEntity masterEntity = new ShortRailMasterEntity();
    LocalDateTime createdAt = LocalDateTime.now();
    // masterEntity.setDutyId(req.getDutyId());
    ShortRailMasterKey key = new ShortRailMasterKey();
    key.setRailSectionInspected(req.getRailSectionInspected());
    key.setRailGradeInspected(req.getRailGradeInspected());
    key.setDutyId(req.getDutyId());
    masterEntity.setId(key);
    masterEntity.setAcceptance26mClA(req.getAcceptance26mClA());
    masterEntity.setAcceptance26mClA01(req.getAcceptance26mClA01());
    masterEntity.setAcceptance26mClB(req.getAcceptance26mClB());
    masterEntity.setAcceptance26mIu(req.getAcceptance26mIu());
    masterEntity.setAcceptance25mClA(req.getAcceptance25mClA());
    masterEntity.setAcceptance25mClA01(req.getAcceptance25mClA01());
    masterEntity.setAcceptance25mClB(req.getAcceptance25mClB());
    masterEntity.setAcceptance25mIu(req.getAcceptance25mIu());
    masterEntity.setAcceptance24mClA(req.getAcceptance24mClA());
    masterEntity.setAcceptance24mClA01(req.getAcceptance24mClA01());
    masterEntity.setAcceptance24mClB(req.getAcceptance24mClB());
    masterEntity.setAcceptance24mIu(req.getAcceptance24mIu());
    masterEntity.setAcceptance13mClA(req.getAcceptance13mClA());
    masterEntity.setAcceptance13mClA01(req.getAcceptance13mClA01());
    masterEntity.setAcceptance13mClB(req.getAcceptance13mClB());
    masterEntity.setAcceptance13mIu(req.getAcceptance13mIu());
    masterEntity.setAcceptance12mClA(req.getAcceptance12mClA());
    masterEntity.setAcceptance12mClA01(req.getAcceptance12mClA01());
    masterEntity.setAcceptance12mClB(req.getAcceptance12mClB());
    masterEntity.setAcceptance12mIu(req.getAcceptance12mIu());
    masterEntity.setAcceptance11mClA(req.getAcceptance11mClA());
    masterEntity.setAcceptance11mClA01(req.getAcceptance11mClA01());
    masterEntity.setAcceptance11mClB(req.getAcceptance11mClB());
    masterEntity.setAcceptance11mIu(req.getAcceptance11mIu());
    masterEntity.setAcceptance10mClA(req.getAcceptance10mClA());
    masterEntity.setAcceptance10mClA01(req.getAcceptance10mClA01());
    masterEntity.setAcceptance10mClB(req.getAcceptance10mClB());
    masterEntity.setAcceptance10mIu(req.getAcceptance10mIu());
    masterEntity.setRejection26m(req.getRejection26m());
    masterEntity.setRejection13m(req.getRejection13m());
    masterEntity.setCutbar26m(req.getCutbart26m());
    masterEntity.setCutbar13m(req.getCutbart13m());
    masterEntity.setRefinish26m(req.getRefinish26m());
    masterEntity.setRefinish13m(req.getRefinish13m());
    masterEntity.setUtnpRejection26m(req.getUtnpRejection26m());
    masterEntity.setUtnpRejection13m(req.getUtnpRejection13m());
    masterEntity.setUtnpCutbar26m(req.getUtnpCutbart26m());
    masterEntity.setUtnpCutbar13m(req.getUtnpCutbart13m());
    masterEntity.setUtnpRefinish26m(req.getUtnpRefinish26m());
    masterEntity.setUtnpRefinish13m(req.getUtnpRefinish13m());
    masterEntity.setCreatedAt(createdAt);
    masterEntity.setRemarks(req.getRemarks());

    // Save Entity to the database
    srmr.save(masterEntity);

    for (RejectionAnalysisDtlsDto radd : req.getRejectionAnalysisDtls()) {
      ShortRailRejectionAnalysisEntity srrae = new ShortRailRejectionAnalysisEntity();
      srrae.setCreatedAt(createdAt);
      srrae.setDefect13m(radd.getRejection13mAnalysisDefect());
      srrae.setDefect13mCount(radd.getRejection13mAnalysisNumber());
      srrae.setDefect26m(radd.getRejection26mAnalysisDefect());
      srrae.setDefect26mCount(radd.getRejection26mAnalysisNumber());
      srrae.setDutyId(req.getDutyId());
      srrae.setRailGradeInspected(req.getRailGradeInspected());
      srrae.setRailSectionInspected(req.getRailSectionInspected());

      srrar.save(srrae);
    }
  }

  @Override
  @Transactional
  public void updateShortRailMasterDetails(ShortRailMasterDto req) {
    // Validate if the related Short Rail Duty exists
    ShortRailDutyEntity dutyEntity = shortRailDutyRepository.findById(req.getDutyId())
        .orElseThrow(() -> new SmsResourceNotFoundException(
            new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "No duty found for the provided duty ID.")));

    ShortRailMasterEntity masterEntity = srmr.findById(new ShortRailMasterKey(req.getRailSectionInspected(), req.getRailGradeInspected(), req.getDutyId()))
        .orElseThrow(() -> new SmsResourceNotFoundException(
            new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "No Short Rail Master found for the provided ID.")));

    // Map DTO to Entity
    // ShortRailMasterEntity masterEntity = new ShortRailMasterEntity();
    LocalDateTime createdAt = LocalDateTime.now();
    // masterEntity.setDutyId(req.getDutyId());
    ShortRailMasterKey key = new ShortRailMasterKey();
    key.setRailSectionInspected(req.getRailSectionInspected());
    key.setRailGradeInspected(req.getRailGradeInspected());
    key.setDutyId(req.getDutyId());
    masterEntity.setId(key);
    masterEntity.setAcceptance26mClA(req.getAcceptance26mClA());
    masterEntity.setAcceptance26mClA01(req.getAcceptance26mClA01());
    masterEntity.setAcceptance26mClB(req.getAcceptance26mClB());
    masterEntity.setAcceptance26mIu(req.getAcceptance26mIu());
    masterEntity.setAcceptance25mClA(req.getAcceptance25mClA());
    masterEntity.setAcceptance25mClA01(req.getAcceptance25mClA01());
    masterEntity.setAcceptance25mClB(req.getAcceptance25mClB());
    masterEntity.setAcceptance25mIu(req.getAcceptance25mIu());
    masterEntity.setAcceptance24mClA(req.getAcceptance24mClA());
    masterEntity.setAcceptance24mClA01(req.getAcceptance24mClA01());
    masterEntity.setAcceptance24mClB(req.getAcceptance24mClB());
    masterEntity.setAcceptance24mIu(req.getAcceptance24mIu());
    masterEntity.setAcceptance13mClA(req.getAcceptance13mClA());
    masterEntity.setAcceptance13mClA01(req.getAcceptance13mClA01());
    masterEntity.setAcceptance13mClB(req.getAcceptance13mClB());
    masterEntity.setAcceptance13mIu(req.getAcceptance13mIu());
    masterEntity.setAcceptance12mClA(req.getAcceptance12mClA());
    masterEntity.setAcceptance12mClA01(req.getAcceptance12mClA01());
    masterEntity.setAcceptance12mClB(req.getAcceptance12mClB());
    masterEntity.setAcceptance12mIu(req.getAcceptance12mIu());
    masterEntity.setAcceptance11mClA(req.getAcceptance11mClA());
    masterEntity.setAcceptance11mClA01(req.getAcceptance11mClA01());
    masterEntity.setAcceptance11mClB(req.getAcceptance11mClB());
    masterEntity.setAcceptance11mIu(req.getAcceptance11mIu());
    masterEntity.setAcceptance10mClA(req.getAcceptance10mClA());
    masterEntity.setAcceptance10mClA01(req.getAcceptance10mClA01());
    masterEntity.setAcceptance10mClB(req.getAcceptance10mClB());
    masterEntity.setAcceptance10mIu(req.getAcceptance10mIu());
    masterEntity.setRejection26m(req.getRejection26m());
    masterEntity.setRejection13m(req.getRejection13m());
    masterEntity.setCutbar26m(req.getCutbart26m());
    masterEntity.setCutbar13m(req.getCutbart13m());
    masterEntity.setRefinish26m(req.getRefinish26m());
    masterEntity.setRefinish13m(req.getRefinish13m());
    masterEntity.setUtnpRejection26m(req.getUtnpRejection26m());
    masterEntity.setUtnpRejection13m(req.getUtnpRejection13m());
    masterEntity.setUtnpCutbar26m(req.getUtnpCutbart26m());
    masterEntity.setUtnpCutbar13m(req.getUtnpCutbart13m());
    masterEntity.setUtnpRefinish26m(req.getUtnpRefinish26m());
    masterEntity.setUtnpRefinish13m(req.getUtnpRefinish13m());
    masterEntity.setCreatedAt(createdAt);
    masterEntity.setRemarks(req.getRemarks());

    // Save Entity to the database
    srmr.save(masterEntity);
    
    for (RejectionAnalysisDtlsDto radd : req.getRejectionAnalysisDtls()) {
      if(Objects.isNull(radd.getId())){
        ShortRailRejectionAnalysisEntity srrae = new ShortRailRejectionAnalysisEntity();
        System.out.println("NULL HAI" + req.getDutyId());
        srrae.setCreatedAt(createdAt);
        srrae.setDefect13m(radd.getRejection13mAnalysisDefect());
        srrae.setDefect13mCount(radd.getRejection13mAnalysisNumber());
        srrae.setDefect26m(radd.getRejection26mAnalysisDefect());
        srrae.setDefect26mCount(radd.getRejection26mAnalysisNumber());
        srrae.setDutyId(req.getDutyId());
        srrae.setRailGradeInspected(req.getRailGradeInspected());
        srrae.setRailSectionInspected(req.getRailSectionInspected());
        srrar.save(srrae);
      }else{
         ShortRailRejectionAnalysisEntity srrae = srrar.findById(radd.getId()).get();
        srrae.setId(radd.getId());
        srrae.setDefect13m(radd.getRejection13mAnalysisDefect());
        srrae.setDefect13mCount(radd.getRejection13mAnalysisNumber());
        srrae.setDefect26m(radd.getRejection26mAnalysisDefect());
        srrae.setDefect26mCount(radd.getRejection26mAnalysisNumber());
        srrar.save(srrae);
      }
      
    }
  }

  @Override
  @Transactional
  public List<ShortRailMasterResDto> getShortRailMasterByDutyId(String dutyId) {
    // Fetch record by dutyId
    List<ShortRailMasterEntity> masterEntities = srmr.findByDutyId(dutyId);

    // if (masterEntities.isEmpty()) {
    //   throw new SmsResourceNotFoundException(
    //       new SmsErrorDetails(
    //           RailMillAppConstant.ERROR_CODE_RESOURCE,
    //           RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
    //           RailMillAppConstant.ERROR_TYPE_RESOURCE,
    //           "No Short Rail Master records found for the provided duty ID."));
    // }

    // Map entities to response DTOs
    List<ShortRailMasterResDto> responseList = new ArrayList<>();
    for (ShortRailMasterEntity entity : masterEntities) {
      ShortRailMasterResDto resDto = new ShortRailMasterResDto();
      resDto.setRailSectionInspected(entity.getId().getRailSectionInspected());
      resDto.setRailGradeInspected(entity.getId().getRailGradeInspected());
      resDto.setDutyId(entity.getId().getDutyId());
      resDto.setAcceptance26mClA(entity.getAcceptance26mClA());
      resDto.setAcceptance26mClA01(entity.getAcceptance26mClA01());
      resDto.setAcceptance26mClB(entity.getAcceptance26mClB());
      resDto.setAcceptance26mIu(entity.getAcceptance26mIu());
      resDto.setAcceptance25mClA(entity.getAcceptance25mClA());
      resDto.setAcceptance25mClA01(entity.getAcceptance25mClA01());
      resDto.setAcceptance25mClB(entity.getAcceptance25mClB());
      resDto.setAcceptance25mIu(entity.getAcceptance25mIu());
      resDto.setAcceptance24mClA(entity.getAcceptance24mClA());
      resDto.setAcceptance24mClA01(entity.getAcceptance24mClA01());
      resDto.setAcceptance24mClB(entity.getAcceptance24mClB());
      resDto.setAcceptance24mIu(entity.getAcceptance24mIu());
      resDto.setAcceptance13mClA(entity.getAcceptance13mClA());
      resDto.setAcceptance13mClA01(entity.getAcceptance13mClA01());
      resDto.setAcceptance13mClB(entity.getAcceptance13mClB());
      resDto.setAcceptance13mIu(entity.getAcceptance13mIu());
      resDto.setAcceptance12mClA(entity.getAcceptance12mClA());
      resDto.setAcceptance12mClA01(entity.getAcceptance12mClA01());
      resDto.setAcceptance12mClB(entity.getAcceptance12mClB());
      resDto.setAcceptance12mIu(entity.getAcceptance12mIu());
      resDto.setAcceptance11mClA(entity.getAcceptance11mClA());
      resDto.setAcceptance11mClA01(entity.getAcceptance11mClA01());
      resDto.setAcceptance11mClB(entity.getAcceptance11mClB());
      resDto.setAcceptance11mIu(entity.getAcceptance11mIu());
      resDto.setAcceptance10mClA(entity.getAcceptance10mClA());
      resDto.setAcceptance10mClA01(entity.getAcceptance10mClA01());
      resDto.setAcceptance10mClB(entity.getAcceptance10mClB());
      resDto.setAcceptance10mIu(entity.getAcceptance10mIu());
      resDto.setRejection26m(entity.getRejection26m());
      resDto.setRejection13m(entity.getRejection13m());
      resDto.setCutbart26m(entity.getCutbar26m());
      resDto.setCutbart13m(entity.getCutbar13m());
      resDto.setRefinish26m(entity.getRefinish26m());
      resDto.setRefinish13m(entity.getRefinish13m());
      resDto.setUtnpRejection26m(entity.getUtnpRejection26m());
      resDto.setUtnpRejection13m(entity.getUtnpRejection13m());
      resDto.setUtnpCutbart26m(entity.getUtnpCutbar26m());
      resDto.setUtnpCutbart13m(entity.getUtnpCutbar13m());
      resDto.setUtnpRefinish26m(entity.getUtnpRefinish26m());
      resDto.setUtnpRefinish13m(entity.getUtnpRefinish13m());
      resDto.setRemarks(entity.getRemarks());

      List<ShortRailRejectionAnalysisEntity> srraeList = srrar
          .findByDutyIdAndRailGradeInspectedAndRailSectionInspected(
              entity.getId().getDutyId(),
              entity.getId().getRailGradeInspected(),
              entity.getId().getRailSectionInspected());

      List<RejectionAnalysisDtlsResDto> raddList = srraeList.stream()
          .map((ShortRailRejectionAnalysisEntity srrae) -> {
            RejectionAnalysisDtlsResDto radd = new RejectionAnalysisDtlsResDto();
            radd.setId(srrae.getId());
            radd.setRejection13mAnalysisDefect(srrae.getDefect13m());
            radd.setRejection13mAnalysisNumber(srrae.getDefect13mCount());
            radd.setRejection26mAnalysisDefect(srrae.getDefect26m());
            radd.setRejection26mAnalysisNumber(
                srrae.getDefect26mCount());

            return radd;
          }).collect(Collectors.toList());

      resDto.setRejectionAnalysisDtls(raddList);

      responseList.add(resDto);

    }
    return responseList;
  }

  @Override
  @Transactional
  public List<ShortRailSummaryResDto> getShortRailSummaries(String dutyId) {
    // Fetch all Short Rail Master entities with the given duty ID
    List<ShortRailMasterEntity> masterEntities = srmr.findByDutyId(dutyId);

    // if (masterEntities.isEmpty()) {
    //   throw new SmsResourceNotFoundException(
    //       new SmsErrorDetails(
    //           RailMillAppConstant.ERROR_CODE_RESOURCE,
    //           RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
    //           RailMillAppConstant.ERROR_TYPE_RESOURCE,
    //           "No records found for the provided duty ID."));
    // }

    // Prepare the list of response DTOs
    List<ShortRailSummaryResDto> summaryList = new ArrayList<>();

    for (ShortRailMasterEntity masterEntity : masterEntities) {
      // Determine the standard weight based on the rail section inspected
      double stdWt;
      String railSection = masterEntity.getId().getRailSectionInspected();
      if (railSection.equalsIgnoreCase("60E1")) {
          stdWt = 60.21;
      } else if (railSection.equalsIgnoreCase("60E1A1")) {
          stdWt = 72.97;
      } else if (railSection.equalsIgnoreCase("UIC60")) {
          stdWt = 60.34;
      } else if (railSection.equalsIgnoreCase("136RE")) {
          stdWt = 67.364;
      } else {
          stdWt = 51.89; // Default for IRS52 and other sections
      }

      // Calculate Total Number Accepted for each length
      int totalAccepted26m = sumAccepted(masterEntity.getAcceptance26mClA(),
          masterEntity.getAcceptance26mClA01(),
          masterEntity.getAcceptance26mClB(),
          masterEntity.getAcceptance26mIu());

      int totalAccepted25m = sumAccepted(masterEntity.getAcceptance25mClA(),
          masterEntity.getAcceptance25mClA01(),
          masterEntity.getAcceptance25mClB(),
          masterEntity.getAcceptance25mIu());

      int totalAccepted24m = sumAccepted(masterEntity.getAcceptance24mClA(),
          masterEntity.getAcceptance24mClA01(),
          masterEntity.getAcceptance24mClB(),
          masterEntity.getAcceptance24mIu());

      int totalAccepted13m = sumAccepted(masterEntity.getAcceptance13mClA(),
          masterEntity.getAcceptance13mClA01(),
          masterEntity.getAcceptance13mClB(),
          masterEntity.getAcceptance13mIu());

      int totalAccepted12m = sumAccepted(masterEntity.getAcceptance12mClA(),
          masterEntity.getAcceptance12mClA01(),
          masterEntity.getAcceptance12mClB(),
          masterEntity.getAcceptance12mIu());

      int totalAccepted11m = sumAccepted(masterEntity.getAcceptance11mClA(),
          masterEntity.getAcceptance11mClA01(),
          masterEntity.getAcceptance11mClB(),
          masterEntity.getAcceptance11mIu());

      int totalAccepted10m = sumAccepted(masterEntity.getAcceptance10mClA(),
          masterEntity.getAcceptance10mClA01(),
          masterEntity.getAcceptance10mClB(),
          masterEntity.getAcceptance10mIu());

      // Calculate Total Number Rejected (direct rejection pieces)
      int totalRejected = (masterEntity.getRejection26m() != null ? masterEntity.getRejection26m() : 0) +
                         (masterEntity.getRejection13m() != null ? masterEntity.getRejection13m() : 0);

      // Total Tonnes Calculations
      double totalTonnesAccepted = stdWt * ((26 * totalAccepted26m) +
          (25 * totalAccepted25m) +
          (24 * totalAccepted24m) +
          (13 * totalAccepted13m) +
          (12 * totalAccepted12m) +
          (11 * totalAccepted11m) +
          (10 * totalAccepted10m));

      // Calculate rejection weight based on leftover parts from accepted pieces
      // Updated leftover calculation: considering pieces cut from both 26m and 13m rails
      double leftoverLength =
          // From 26m rails: leftover when accepting 25m, 24m pieces
          ((26 - 25) * totalAccepted25m) +  // 1m leftover per 25m piece from 26m rail
          ((26 - 24) * totalAccepted24m) +  // 2m leftover per 24m piece from 26m rail

          // From 13m rails: leftover when accepting 12m, 11m, 10m pieces
          ((13 - 12) * totalAccepted12m) +  // 1m leftover per 12m piece from 13m rail
          ((13 - 11) * totalAccepted11m) +  // 2m leftover per 11m piece from 13m rail
          ((13 - 10) * totalAccepted10m);   // 3m leftover per 10m piece from 13m rail

          // Note: 26m and 13m pieces are cut directly from their respective rail lengths with no leftover

      // Total rejection weight = direct rejection weight + leftover weight
      double totalTonnesRejected = stdWt * ((26 * totalRejected) + leftoverLength);

      double totalTonnesInspected = totalTonnesAccepted + totalTonnesRejected;

      // Prepare Response for this entity
      ShortRailSummaryResDto responseDto = new ShortRailSummaryResDto();
      responseDto.setTotalTonnesAccepted(totalTonnesAccepted);
      responseDto.setTotalTonnesRejected(totalTonnesRejected);
      responseDto.setTotalTonnesInspected(totalTonnesInspected);

      // Add to the list
      summaryList.add(responseDto);
    }

    return summaryList;
  }

  // Utility Method to Calculate Total Accepted
  private int sumAccepted(Integer... values) {
    return Arrays.stream(values).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
  }

  @Transactional
  @Override
  public void deleteRejectionAnalysisById(IdDto req) {
    if (!srrar.existsById(req.getId())) {
      throw new SmsResourceNotFoundException(
          new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
              RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
              RailMillAppConstant.ERROR_TYPE_RESOURCE,
              "No duty found for the provided duty ID."));
    }
    srrar.deleteById(req.getId());
  }

  @Override
  public ShortRailMasterResDto getShortRailMasterById(MasterByIdDto req) {
    ShortRailMasterEntity res = srmr.findById(
        new ShortRailMasterKey(req.getRailSectionInspected(), req.getRailGradeInspected(), req.getDutyId()))
        .orElseThrow(() -> new SmsResourceNotFoundException(new SmsErrorDetails(
            RailMillAppConstant.ERROR_CODE_RESOURCE,
            RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
            RailMillAppConstant.ERROR_TYPE_RESOURCE,
            "No record found for the provided details.")));

    ShortRailMasterResDto resDto = new ShortRailMasterResDto();
    resDto.setRailSectionInspected(res.getId().getRailSectionInspected());
    resDto.setRailGradeInspected(res.getId().getRailGradeInspected());
    resDto.setDutyId(res.getId().getDutyId());
    resDto.setAcceptance26mClA(res.getAcceptance26mClA());
    resDto.setAcceptance26mClA01(res.getAcceptance26mClA01());
    resDto.setAcceptance26mClB(res.getAcceptance26mClB());
    resDto.setAcceptance26mIu(res.getAcceptance26mIu());
    resDto.setAcceptance25mClA(res.getAcceptance25mClA());
    resDto.setAcceptance25mClA01(res.getAcceptance25mClA01());
    resDto.setAcceptance25mClB(res.getAcceptance25mClB());
    resDto.setAcceptance25mIu(res.getAcceptance25mIu());
    resDto.setAcceptance24mClA(res.getAcceptance24mClA());
    resDto.setAcceptance24mClA01(res.getAcceptance24mClA01());
    resDto.setAcceptance24mClB(res.getAcceptance24mClB());
    resDto.setAcceptance24mIu(res.getAcceptance24mIu());
    resDto.setAcceptance13mClA(res.getAcceptance13mClA());
    resDto.setAcceptance13mClA01(res.getAcceptance13mClA01());
    resDto.setAcceptance13mClB(res.getAcceptance13mClB());
    resDto.setAcceptance13mIu(res.getAcceptance13mIu());
    resDto.setAcceptance12mClA(res.getAcceptance12mClA());
    resDto.setAcceptance12mClA01(res.getAcceptance12mClA01());
    resDto.setAcceptance12mClB(res.getAcceptance12mClB());
    resDto.setAcceptance12mIu(res.getAcceptance12mIu());
    resDto.setAcceptance11mClA(res.getAcceptance11mClA());
    resDto.setAcceptance11mClA01(res.getAcceptance11mClA01());
    resDto.setAcceptance11mClB(res.getAcceptance11mClB());
    resDto.setAcceptance11mIu(res.getAcceptance11mIu());
    resDto.setAcceptance10mClA(res.getAcceptance10mClA());
    resDto.setAcceptance10mClA01(res.getAcceptance10mClA01());
    resDto.setAcceptance10mClB(res.getAcceptance10mClB());
    resDto.setAcceptance10mIu(res.getAcceptance10mIu());
    resDto.setRejection26m(res.getRejection26m());
    resDto.setRejection13m(res.getRejection13m());
    resDto.setCutbart26m(res.getCutbar26m());
    resDto.setCutbart13m(res.getCutbar13m());
    resDto.setRefinish26m(res.getRefinish26m());
    resDto.setRefinish13m(res.getRefinish13m());
    resDto.setUtnpRejection26m(res.getUtnpRejection26m());
    resDto.setUtnpRejection13m(res.getUtnpRejection13m());
    resDto.setUtnpCutbart26m(res.getUtnpCutbar26m());
    resDto.setUtnpCutbart13m(res.getUtnpCutbar13m());
    resDto.setUtnpRefinish26m(res.getUtnpRefinish26m());
    resDto.setUtnpRefinish13m(res.getUtnpRefinish13m());
    resDto.setRemarks(res.getRemarks());

    List<ShortRailRejectionAnalysisEntity> srraeList = srrar
        .findByDutyIdAndRailGradeInspectedAndRailSectionInspected(
            res.getId().getDutyId(),
            res.getId().getRailGradeInspected(),
            res.getId().getRailSectionInspected());

    List<RejectionAnalysisDtlsResDto> raddList = srraeList.stream()
        .map((ShortRailRejectionAnalysisEntity srrae) -> {
          RejectionAnalysisDtlsResDto radd = new RejectionAnalysisDtlsResDto();
          radd.setId(srrae.getId());
          radd.setRejection13mAnalysisDefect(srrae.getDefect13m());
          radd.setRejection13mAnalysisNumber(srrae.getDefect13mCount());
          radd.setRejection26mAnalysisDefect(srrae.getDefect26m());
          radd.setRejection26mAnalysisNumber(
              srrae.getDefect26mCount());

          return radd;
        }).collect(Collectors.toList());

    resDto.setRejectionAnalysisDtls(raddList);

    return resDto;
  }

  @Override
  @Transactional
  public void deleteInspectedRgRs(SriRgRsDeleteDto req){
    if(!srmr.existsById(new ShortRailMasterKey(req.getRailSection(), req.getRailGrade(), req.getDutyId()))){
      throw new SmsResourceNotFoundException(
        new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
            RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
            RailMillAppConstant.ERROR_TYPE_RESOURCE,
            "Record not found. Cannot delete."));
    }

    srmr.deleteById(new ShortRailMasterKey(req.getRailSection(), req.getRailGrade(), req.getDutyId()));
  }

  @Override
  public List<WorkstationRemarksDto> getWorkStationRemarks(String dutyId){
    ShortRailDutyEntity srde = shortRailDutyRepository.findById(dutyId)
       .orElseThrow(() -> new SmsResourceNotFoundException(
            new SmsErrorDetails(RailMillAppConstant.ERROR_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_CODE_RESOURCE,
                RailMillAppConstant.ERROR_TYPE_RESOURCE,
                "No record found for the provided duty ID." )));
    List<Object[]> resObj = srmr.getWorkstationRemarks(srde.getShift(), srde.getDate());
    return resObj.stream()
                .map(row -> new WorkstationRemarksDto(
                        (String) row[0],  // shiftDtls
                        (String) row[1],  // shiftRemarks
                        row[2] != null ? ((java.sql.Timestamp) row[2]).toLocalDateTime() : null // startTime
                ))
                .collect(Collectors.toList());
  }


}
