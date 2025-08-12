package com.erikmikac.ChapelChat.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikmikac.ChapelChat.model.ChurchProfile;
import com.erikmikac.ChapelChat.model.admin.ProfileDto;
import com.erikmikac.ChapelChat.service.admin.ChurchProfileService;

@RestController
@RequestMapping("/admin/church-profile")
public class ChurchProfileController {

    private final ChurchProfileService svc;

    public ChurchProfileController(ChurchProfileService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ProfileDto get() {
        return svc.get();
    }

    @PutMapping
    public ProfileDto upsert(@RequestBody ProfileDto dto) {
        return svc.upsert(dto);
    }

    @GetMapping("/export")
    public ProfileDto exportJson() {
        return svc.get();
    }

    @PostMapping("/import")
    public ProfileDto importJson(@RequestBody ProfileDto dto) {
        return svc.upsert(dto);
    }
}
