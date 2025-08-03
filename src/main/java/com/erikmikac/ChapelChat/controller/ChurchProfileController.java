package com.erikmikac.ChapelChat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikmikac.ChapelChat.model.ChurchProfile;
import com.erikmikac.ChapelChat.service.ChurchProfileService;

@RestController
@RequestMapping("/api/church")
public class ChurchProfileController {

    @Autowired
    private ChurchProfileService profileService;

    @GetMapping("/{churchId}")
    public ChurchProfile getChurchProfile(@PathVariable String churchId) {
        return profileService.getProfile(churchId);
    }

    @PutMapping("/{churchId}")
    public ResponseEntity<Void> updateChurchProfile(@PathVariable String churchId, @RequestBody ChurchProfile profile) {
        profileService.updateProfile(churchId, profile);
        return ResponseEntity.ok().build();
    }
}
