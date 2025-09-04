package com.erikmikac.ChapelChat.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.service.AskService;
import com.erikmikac.ChapelChat.tenant.web.CurrentOrgId;
import com.erikmikac.ChapelChat.tenant.web.CurrentTenantId;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping
@Slf4j
@Validated
public class AskController {
    private final AskService askService;

    public AskController(AskService askService) {
        this.askService = askService;

    }

    @PostMapping(path = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AskResponse> ask(@CurrentOrgId String orgId,
            @CurrentTenantId @Nullable String tenantId,
            @RequestBody AskRequest askRequest,
            HttpServletRequest request) {
        // (optional) add orgId/tenantId into askRequest if your AskContext expects it
        return askService.processAsk(askRequest, request);
    }

}
