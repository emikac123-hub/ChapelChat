package com.erikmikac.ChapelChat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erikmikac.ChapelChat.entity.Church;

public interface ChurchRepository extends JpaRepository<Church, String> {
    Optional<String> findContactEmailById(String id);

}
