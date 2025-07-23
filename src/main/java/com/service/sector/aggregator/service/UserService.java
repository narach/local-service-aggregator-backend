package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.auth.LoginRequest;
import com.service.sector.aggregator.data.entity.AppUser;

public interface UserService {
    AppUserResponse register(AppUserRequest request);

    AppUserResponse login(LoginRequest request);

    void sendCode(String phone);

    AppUserResponse getUserDetails(Long userId);
}
