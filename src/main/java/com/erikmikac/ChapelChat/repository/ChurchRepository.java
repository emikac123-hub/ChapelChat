package com.erikmikac.ChapelChat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erikmikac.ChapelChat.entity.AppUser;

public interface ChurchRepository extends JpaRepository<AppUser, Long> {
    Optional<String> findContactEmailByChurchId(String churchId);

}
