package com.sarthi.SRailPad.service.inspectionCall;

import com.sarthi.SRailPad.dto.RailInitiationVerificationDto;
import com.sarthi.SRailPad.entity.inspectionCall.RailInitiationVerification;

import java.util.Optional;

/**
 * Service for saving and retrieving Section A & B verification data
 * for a Railpad inspection call.
 */
public interface RailInitiationVerificationService {

    /**
     * Save (or upsert) the initiation verification record.
     * If a record already exists for the callNo it will be updated.
     *
     * @param dto Verification data from the frontend
     * @return Saved entity
     */
    RailInitiationVerification save(RailInitiationVerificationDto dto);

    /**
     * Get verification record by call number.
     *
     * @param callNo Inspection call number e.g. RPF-0513002
     * @return Optional entity
     */
    Optional<RailInitiationVerification> getByCallNo(String callNo);
}
