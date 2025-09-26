package org.example.airesumescoring.repository;

import org.example.airesumescoring.model.Permission;
import org.example.airesumescoring.model.UserPermission;
import org.example.airesumescoring.model.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    @EntityGraph(attributePaths = {"permission"})

    @Query("SELECT up FROM UserPermission up JOIN FETCH up.permission WHERE up.user.id = :userId")
    List<UserPermission> findByUserIdWithPermissions(@Param("userId") Long userId);

    // 根据用户ID查询所有权限
    @Query("SELECT up FROM UserPermission up JOIN FETCH up.permission WHERE up.user.id = :userId")
    List<UserPermission> findByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"permission"})
    List<UserPermission> findByUser(Users user);

    boolean existsByUserAndPermission(Users user, Permission permission);

    void deleteByUserAndPermission(Users user, Permission permission);

    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.user = :user")
    int deleteByUser(@Param("user") Users user);
}
