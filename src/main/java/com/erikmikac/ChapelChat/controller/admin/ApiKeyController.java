package com.erikmikac.ChapelChat.controller.admin;

import org.springframework.web.bind.annotation.*;

import com.erikmikac.ChapelChat.model.admin.ApiKeyDto;
import com.erikmikac.ChapelChat.model.admin.NewKeyDto;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;

import java.util.List;

@RestController
@RequestMapping("/admin/api-keys")
public class ApiKeyController {
  private final ApiKeyService svc;
  public ApiKeyController(ApiKeyService svc) { this.svc = svc; }

  @GetMapping
  public List<ApiKeyDto> list() { return svc.list(); }

  @PostMapping
  public NewKeyDto create() { return svc.create(); } // returns plaintext once

  @DeleteMapping("/{id}")
  public void revoke(@PathVariable String id) { svc.revoke(id); }
}