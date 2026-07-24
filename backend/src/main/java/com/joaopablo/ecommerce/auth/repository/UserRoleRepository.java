package com.joaopablo.ecommerce.auth.repository;

import com.joaopablo.ecommerce.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    boolean existsByUser_IdAndRole_Name(UUID userId, String roleName);

    List<UserRole> findByUser_Id(UUID userId);

    List<UserRole> findByRole_Name(String roleName);
}
