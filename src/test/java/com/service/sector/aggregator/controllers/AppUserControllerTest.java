package com.service.sector.aggregator.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.sector.aggregator.data.dto.ActivationRequest;
import com.service.sector.aggregator.data.dto.AppUserRequest;
import com.service.sector.aggregator.data.entity.AppUser;
import com.service.sector.aggregator.data.entity.Role;
import com.service.sector.aggregator.data.enums.ActivationStatus;
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

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AppUserController.class)
public class AppUserControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockBean
    AppUserRepository userRepo;
    @MockBean
    RoleRepository roleRepo;

    @MockBean
    JwtService jwt;

    private Role customerRole;

    private String json(Object o) throws Exception {
        return om.writeValueAsString(o);
    }

    @BeforeEach
    void setUp() {
        customerRole = new Role(4L, RoleName.CUSTOMER, "customer");
        given(roleRepo.findByRoleName(RoleName.CUSTOMER))
                .willReturn(Optional.of(customerRole));

        // default save → echo argument
        BDDMockito.willAnswer(inv -> inv.getArgument(0)).given(userRepo).save(any());
        given(jwt.generateToken(anyLong())).willReturn("jwt-token");
    }

    @Nested @DisplayName("Registration")
    class Register {
        @Test void success() throws Exception {
            given(userRepo.existsByPhone("+38777777777")).willReturn(false);

            mvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new AppUserRequest("+38777777777", "Alice"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.phone").value("+38777777777"))
                    .andExpect(jsonPath("$.activationStatus").value("PENDING"));
        }

        @Test void duplicatePhone() throws Exception {
            given(userRepo.existsByPhone("+38000111222")).willReturn(true);

            mvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new AppUserRequest("+38000111222", "Dup"))))
                    .andExpect(status().isConflict());
        }

        @Test void missingPhone() throws Exception {
            mvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new AppUserRequest(null, "Anon"))))
                    .andExpect(status().isBadRequest());
        }

        @Test void missingRealName() throws Exception {
            mvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new AppUserRequest("+38511112222", ""))))
                    .andExpect(status().isBadRequest());
        }

        @Test void invalidPhonePattern() throws Exception {
            mvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new AppUserRequest("123abc", "Bob"))))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
//    @Nested @DisplayName("Activation")
//    class Activation {
//        private final String phone = "+38260000000";
//
//        private AppUser pending() {
//            return AppUser.builder()
//                    .id(10L)
//                    .phone(phone)
//                    .realName("Pending")
//                    .roles(Set.of(customerRole))
//                    .activationCode("654321")
//                    .activationStatus(ActivationStatus.PENDING)
//                    .createdAt(OffsetDateTime.now())
//                    .build();
//        }
//
//        @Test void success_customCode() throws Exception {
//            // Arrange — stub repo to return a pending user for ANY phone string
//            given(userRepo.findByPhone(anyString()))
//                    .willReturn(Optional.of(pending()));
//
//            // Act & Assert
//            mvc.perform(post("/api/users/activate")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(json(new ActivationRequest(phone, "654321"))))
//                    .andExpect(status().isNoContent());
//        }
//    }
}
