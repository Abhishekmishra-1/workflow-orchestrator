package com.workflow.userservice.service;

import com.workflow.userservice.dto.UserCreateDto;
import com.workflow.userservice.dto.UserResponseDto;
import com.workflow.userservice.dto.UserUpdateDto;
import com.workflow.userservice.entity.Role;
import com.workflow.userservice.entity.Tenant;
import com.workflow.userservice.entity.User;
import com.workflow.userservice.mapper.UserMapper;
import com.workflow.userservice.repository.RoleRepository;
import com.workflow.userservice.repository.TenantRepository;
import com.workflow.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserResponseDto createUser(UserCreateDto dto) {
        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tenant not found"));

        // Check if username already exists
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Username already exists");
        }

        // Build user entity
        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword()) // In production, hash this password
                .email(dto.getEmail())
                .tenant(tenant)
                .build();

        // Add roles if provided
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<Role> roles = dto.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role not found: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByTenant(Long tenantId) {
        List<User> users = tenantId != null 
                ? userRepository.findByTenantId(tenantId)
                : userRepository.findAll();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getRoleIds() != null) {
            Set<Role> roles = dto.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role not found: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}
