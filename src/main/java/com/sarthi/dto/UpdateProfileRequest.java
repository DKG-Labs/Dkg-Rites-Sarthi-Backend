package com.sarthi.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String mobileNumber;
    private String alternateMobileNumber;
    private String emailAddress;
    private String designation;
    private String notificationPreferences;
    private String profilePhotoPath;
}
