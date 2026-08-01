package com.alness.lifemaster.permissions.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alness.lifemaster.permissions.entity.PermissionEntity;
import com.alness.lifemaster.permissions.entity.PermissionId;

public interface PermissionRepository extends JpaRepository<PermissionEntity, PermissionId> {

    @Query("""
            select permission
            from PermissionEntity permission
            join fetch permission.module module
            join fetch permission.profile profile
            where profile.id in :profileIds
              and profile.erased = false
              and module.erased = false
              and module member of profile.modules
            """)
    List<PermissionEntity> findEffectiveByProfileIds(@Param("profileIds") Collection<UUID> profileIds);

    @Query("""
            select permission
            from PermissionEntity permission
            join fetch permission.module module
            join fetch permission.profile profile
            where profile.id in :profileIds
              and lower(module.permissionKey) = lower(:permissionKey)
              and profile.erased = false
              and module.erased = false
              and module member of profile.modules
            """)
    List<PermissionEntity> findEffectiveByProfileIdsAndPermissionKey(
            @Param("profileIds") Collection<UUID> profileIds,
            @Param("permissionKey") String permissionKey);

    @Query("""
            select permission
            from PermissionEntity permission
            join fetch permission.module module
            join fetch permission.profile profile
            where (:profileId is null or profile.id = :profileId)
              and (:moduleId is null or module.id = :moduleId)
            order by profile.name, module.name
            """)
    List<PermissionEntity> findForAdministration(
            @Param("profileId") UUID profileId,
            @Param("moduleId") UUID moduleId);
}
