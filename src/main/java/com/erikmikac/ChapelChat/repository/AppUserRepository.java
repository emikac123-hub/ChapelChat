package com.erikmikac.ChapelChat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erikmikac.ChapelChat.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
