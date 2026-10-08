package com.bank.los.administration.usermanagement.service;

import com.bank.los.bank.user.dto.CreateUserRequest;
import com.bank.los.bank.user.dto.UpdateUserRequest;
import com.bank.los.bank.user.dto.UserResponse;
import com.bank.los.bank.user.service.UserService;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankUserManagementService {

    private final UserService userService;

    public List<UserResponse> listAllUsers(UserPrincipal principal, Long organizationId) {
        return userService.getAllUsers(principal, organizationId);
    }

    public List<UserResponse> listAllUsersForOrganization(UserPrincipal principal, Long organizationId) {
        return userService.getAllUsers(principal, organizationId);
    }

    public UserResponse getUserById(UserPrincipal principal, Long userId, Long organizationId) {
        return userService.getUserById(principal, userId, organizationId);
    }

    public UserResponse provisionBankAdmin(UserPrincipal principal, CreateUserRequest request) {
        return userService.createUser(principal, request);
    }

    public UserResponse verifyBankUser(UserPrincipal principal, Long userId, Long organizationId) {
        return userService.verifyUser(principal, userId);
    }

    public UserResponse updateUser(UserPrincipal principal, Long userId, UpdateUserRequest request, Long organizationId) {
        return userService.updateUser(principal, userId, request, organizationId);
    }
}
