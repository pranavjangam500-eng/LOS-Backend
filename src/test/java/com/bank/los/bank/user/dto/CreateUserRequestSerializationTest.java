package com.bank.los.bank.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CreateUserRequestSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should successfully deserialize user payload into CreateUserRequest without conflicting setter errors")
    void testUserPayloadDeserialization() {
        String json = """
        {
          "organization_id": 3,
          "organization_code": "",
          "emp_no": "EMP-001",
          "first_name": "Pranav",
          "middle_name": "",
          "last_name": "surya",
          "dob": "2001-01-12",
          "email": "pranav@allianzapay.com",
          "password": "Govinda@1234",
          "mobile": "7972043645",
          "gender": "MALE",
          "designation": "Relationship Manager",
          "role": "SUPER_ADMIN",
          "2fA": false,
          "status": "OPERATIVE",
          "allow_multibranch": 0,
          "login_branch": 0,
          "allow_login_in_holidays": 0,
          "login_time": "09:39",
          "logout_time": "22:39",
          "no_of_bad_logins": 0
        }
        """;

        CreateUserRequest request = assertDoesNotThrow(() ->
                objectMapper.readValue(json, CreateUserRequest.class)
        );

        assertThat(request).isNotNull();
        assertThat(request.getOrganizationId()).isEqualTo(3L);
        assertThat(request.getEmpNo()).isEqualTo("EMP-001");
        assertThat(request.getFirstName()).isEqualTo("Pranav");
        assertThat(request.getLastName()).isEqualTo("surya");
        assertThat(request.getEmail()).isEqualTo("pranav@allianzapay.com");
        assertThat(request.getTwoFaEnabled()).isFalse();
        assertThat(request.getMultiBranchAccess()).isFalse();
        assertThat(request.getLoginOnHolidays()).isFalse();
        assertThat(request.getLoginBranch()).isEqualTo("0");
        assertThat(request.getLoginBranchId()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should deserialize UpdateUserRequest without conflicting setter errors")
    void testUpdateUserRequestDeserialization() {
        String json = """
        {
          "first_name": "Pranav",
          "last_name": "surya",
          "allow_multibranch": 1,
          "allow_login_in_holidays": 1,
          "login_branch": "5"
        }
        """;

        UpdateUserRequest request = assertDoesNotThrow(() ->
                objectMapper.readValue(json, UpdateUserRequest.class)
        );

        assertThat(request).isNotNull();
        assertThat(request.getMultiBranchAccess()).isTrue();
        assertThat(request.getLoginOnHolidays()).isTrue();
        assertThat(request.getLoginBranch()).isEqualTo("5");
        assertThat(request.getLoginBranchId()).isEqualTo(5L);
    }
}
