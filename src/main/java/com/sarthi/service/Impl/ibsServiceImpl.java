package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.ibsDtos.AuthRequestDto;
import com.sarthi.dto.ibsDtos.AuthResponseDto;
import com.sarthi.entity.UserMaster;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.service.JwtService;
import com.sarthi.service.ibsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ibsServiceImpl implements ibsService {

    private UserMasterRepository userMasterRepository;

    private JwtService jwtService;


    @Override
    public AuthResponseDto integrationLogin(
            AuthRequestDto request) {

        UserMaster user = userMasterRepository
                .findFirstByEmployeeCode(request.getLoginId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(
                                AppConstant.ERROR_CODE_INVALID,
                                AppConstant.ERROR_TYPE_CODE_INVALID,
                                AppConstant.ERROR_TYPE_INVALID,
                                "Invalid credentials."
                        )));

        // Password Validation
        if (!request.getPassword().equals(user.getPassword())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_INVALID,
                            AppConstant.ERROR_TYPE_INVALID,
                            "Invalid credentials."
                    ));
        }

        // Generate JWT Token
        String token = jwtService.generateToken(user);

        return new AuthResponseDto(
                token,
                "Bearer",
                3600L
        );
    }


}
