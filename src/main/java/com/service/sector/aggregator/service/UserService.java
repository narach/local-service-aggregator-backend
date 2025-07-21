package com.service.sector.aggregator.service;

import com.service.sector.aggregator.data.dto.ActivationRequest;
import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.dto.AppUserResponse;
import com.service.sector.aggregator.data.dto.auth.AuthResponse;
import com.service.sector.aggregator.data.dto.auth.LoginRequest;

public interface UserService {
    AppUserResponse register(AppUserRequest request);

    AuthResponse login(LoginRequest request);

    void sendCode(String phone);
}
