package com.sarthi.Sms.service;

import com.sarthi.Sms.dto.auth.UserListResponseDto;
import java.util.List;
import java.util.Optional;

public interface UserLookupService {
    
    /**
     * Get all active users for dropdown selection
     * @return List of active users
     */
    List<UserListResponseDto> getActiveUsersForDropdown();
    
    /**
     * Get user display name by user ID
     * @param userId The user ID to lookup
     * @return Formatted display name (Employee ID - Full Name (Role))
     */
    Optional<String> getUserDisplayName(String userId);
    
    /**
     * Get user details by user ID
     * @param userId The user ID to lookup
     * @return User details if found
     */
    Optional<UserListResponseDto> getUserById(String userId);

    /**
     * Format IE name for display - handles both old format (employee ID only) and new format (employee ID - name)
     * @param ieNameValue The stored IE name value
     * @return Formatted display string
     */
    String formatIeNameForDisplay(String ieNameValue);
}
