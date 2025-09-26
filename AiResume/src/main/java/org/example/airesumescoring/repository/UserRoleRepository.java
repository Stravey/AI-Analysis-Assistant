package org.example.airesumescoring.repository;

import org.example.airesumescoring.model.Role;
import org.example.airesumescoring.model.UserRole;
import org.example.airesumescoring.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(Users user);

    boolean existsByUserAndRole(Users user, Role role);

    void deleteByUserAndRole(Users user, Role role);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    void deleteByUser(@Param("userId") Long userId);
}
