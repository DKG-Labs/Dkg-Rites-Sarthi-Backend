package com.sarthi.Sms.service.Impl;

import com.sarthi.Sms.dto.auth.UserListResponseDto;
import com.sarthi.Sms.service.UserLookupService;
import com.sarthi.entity.UserMaster;
import com.sarthi.repository.UserMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Rail-mill user lookup backed by SARTHI's user_master (the Rites backend had its own user table).
 * Read-only: never creates or changes users.
 */
@Service("railmillUserLookupService")
@RequiredArgsConstructor
public class RailMillUserLookupServiceImpl implements UserLookupService {

    private final UserMasterRepository userMasterRepository;

    @Override
    public List<UserListResponseDto> getActiveUsersForDropdown() {
        return userMasterRepository.findAll().stream()
                .filter(u -> u.getStatus() == null || !"Inactive".equalsIgnoreCase(String.valueOf(u.getStatus())))
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<String> getUserDisplayName(String userId) {
        return find(userId).map(u -> code(u) + " - " + name(u) + (u.getRoleName() != null ? " (" + u.getRoleName() + ")" : ""));
    }

    @Override
    public Optional<UserListResponseDto> getUserById(String userId) {
        return find(userId).map(this::toDto);
    }

    @Override
    public String formatIeNameForDisplay(String ieNameValue) {
        if (ieNameValue == null || ieNameValue.isBlank()) return "";
        if (ieNameValue.contains(" - ")) return ieNameValue;                 // already "code - name"
        return getUserDisplayName(ieNameValue.trim()).orElse(ieNameValue);
    }

    private Optional<UserMaster> find(String userId) {
        if (userId == null || userId.isBlank()) return Optional.empty();
        try {
            Optional<UserMaster> byId = userMasterRepository.findByUserId(Integer.valueOf(userId.trim()));
            if (byId.isPresent()) return byId;
        } catch (NumberFormatException ignored) { }
        return userMasterRepository.findFirstByEmployeeCode(userId.trim());
    }

    private UserListResponseDto toDto(UserMaster u) {
        UserListResponseDto d = new UserListResponseDto();
        d.setUserId(String.valueOf(u.getUserId()));
        d.setEmployeeId(code(u));
        d.setFirstName(name(u));
        d.setLastName("");
        d.setUserType(u.getRoleName());
        d.setIsActive(u.getStatus() == null || !"Inactive".equalsIgnoreCase(String.valueOf(u.getStatus())));
        d.setCreateDate(u.getCreatedDate());
        return d;
    }

    private static String code(UserMaster u) { return u.getEmployeeCode() != null ? u.getEmployeeCode() : String.valueOf(u.getUserId()); }

    private static String name(UserMaster u) {
        if (u.getFullName() != null && !u.getFullName().isBlank()) return u.getFullName().trim();
        return u.getUsername();
    }
}
