package com.joaopablo.ecommerce.auth.repository;

import com.joaopablo.ecommerce.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    boolean existsByUser_IdAndRole_Name(Long userId, String roleName);

    List<UserRole> findByUser_Id(Long userId);

    List<UserRole> findByRole_Name(String roleName);
}
