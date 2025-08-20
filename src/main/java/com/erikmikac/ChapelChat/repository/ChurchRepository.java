package com.erikmikac.ChapelChat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erikmikac.ChapelChat.entity.Church;
import com.erikmikac.ChapelChat.enums.Roles;

public interface ChurchRepository extends JpaRepository<Church, String> {

    @Query("""
            select distinct u.username
            from AppUser u
            where u.church.id = :churchId
              and :role MEMBER OF u.roles
            """)
    List<String> findEmailsByChurchIdAndRole(@Param("churchId") String churchId,
            @Param("role") Roles role);

    // Convenience wrapper specifically for ADMINs, returning Optional
    default Optional<List<String>> findAdminEmailsOptional(String churchId) {
        List<String> emails = findEmailsByChurchIdAndRole(churchId, Roles.ADMIN);
        return emails.isEmpty() ? Optional.empty() : Optional.of(emails);
    }
}
