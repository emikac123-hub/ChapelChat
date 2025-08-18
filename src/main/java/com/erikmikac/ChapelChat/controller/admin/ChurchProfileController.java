package com.erikmikac.ChapelChat.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/church-profile")
public class ChurchProfileController {

    // private final ChurchProfileService svc;

    // public ChurchProfileController(ChurchProfileService svc) {
    //     this.svc = svc;
    // }

    // @GetMapping
    // public ProfileDto get() {
    //     return svc.get();
    // }

    // @PutMapping
    // public ProfileDto upsert(@RequestBody ProfileDto dto) {
    //     return svc.upsert(dto);
    // }

    // @GetMapping("/export")
    // public ProfileDto exportJson() {
    //     return svc.get();
    // }

    // @PostMapping("/import")
    // public ProfileDto importJson(@RequestBody ProfileDto dto) {
    //     return svc.upsert(dto);
    // }
}
