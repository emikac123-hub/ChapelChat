package com.erikmikac.ChapelChat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erikmikac.ChapelChat.entity.Organization;
import com.erikmikac.ChapelChat.enums.Roles;

public interface OrganizationRepository extends JpaRepository<Organization, String> {

    @Query("""
            select distinct u.username
            from AppUser u
            where u.organization.id = :orgId
              and :role MEMBER OF u.roles
            """)
    List<String> findEmailsByOrgIdAndRole(@Param("orgId") String orgId,
            @Param("role") Roles role);

    // Convenience wrapper specifically for ADMINs, returning Optional
    default Optional<List<String>> findAdminEmailsOptional(String orgId) {
        List<String> emails = findEmailsByOrgIdAndRole(orgId, Roles.ADMIN);
        return emails.isEmpty() ? Optional.empty() : Optional.of(emails);
    }
}
