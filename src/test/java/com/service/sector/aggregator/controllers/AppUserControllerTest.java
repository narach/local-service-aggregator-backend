package com.service.sector.aggregator.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.entity.Role;
import com.service.sector.aggregator.data.enums.RoleName;
import com.service.sector.aggregator.data.repositories.AppUserRepository;
import com.service.sector.aggregator.data.repositories.RoleRepository;

import com.service.sector.aggregator.service.JwtService;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AppUserController.class)
public class AppUserControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    AppUserRepository userRepo;
    @MockBean
    RoleRepository roleRepo;

    /* simple password encoder stub */
    @MockBean
    PasswordEncoder encoder;

    @MockBean
    JwtService jwtService;

    private Role adminRole, customerRole;

    @BeforeEach
    void setUp() {
        adminRole    = new Role(1L, RoleName.ADMINISTRATOR, "admin");
        customerRole = new Role(4L, RoleName.CUSTOMER, "customer");

        given(roleRepo.findByRoleName(RoleName.ADMINISTRATOR))
                .willReturn(Optional.of(adminRole));
        given(roleRepo.findByRoleName(RoleName.CUSTOMER))
                .willReturn(Optional.of(customerRole));

        given(encoder.encode(any())).willReturn("$bcrypt$");
        BDDMockito.willAnswer(inv -> inv.getArgument(0))
                .given(userRepo).save(any());
    }

    private String json(AppUserRequest r) throws Exception { return mapper.writeValueAsString(r); }

    // ── SUCCESS ───────────────────────────────────────────────────────────────
    @Test
    void registerByEmail() throws Exception {
        given(userRepo.existsByEmail("joe@mail.com")).willReturn(false);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AppUserRequest(
                                "joe@mail.com", null, "Joe", "Password1!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("joe@mail.com"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }

    @Test void registerByPhone() throws Exception {
        given(userRepo.existsByPhone("+38268754722")).willReturn(false);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AppUserRequest(
                                null, "+38268754722", "Ann", "Password1!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("+38268754722"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }

    // ── FAILURES ──────────────────────────────────────────────────────────────
    @Test void noContact() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AppUserRequest(
                                null, null, "Anon", "Password1!"))))
                .andExpect(status().isBadRequest());
    }

    @Test void duplicateEmail() throws Exception {
        given(userRepo.existsByEmail("dup@mail.com")).willReturn(true);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AppUserRequest(
                                "dup@mail.com", null, "Dup", "Password1!"))))
                .andExpect(status().isConflict());
    }

    @Test void missingRealName() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AppUserRequest(
                                "a@b.com", null, "", "Password1!"))))
                .andExpect(status().isBadRequest());
    }

    @Test void weakPassword() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AppUserRequest(
                                "weak@mail.com", null, "Weak", "abc"))))
                .andExpect(status().isBadRequest());
    }
}
