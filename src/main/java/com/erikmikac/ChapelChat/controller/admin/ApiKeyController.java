package com.erikmikac.ChapelChat.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikmikac.ChapelChat.model.admin.ApiKeyDto;
import com.erikmikac.ChapelChat.model.admin.NewKeyDto;
import com.erikmikac.ChapelChat.service.admin.ApiKeyService;

@RestController
@RequestMapping("/admin/api-keys")
public class ApiKeyController {
  private final ApiKeyService svc;
  public ApiKeyController(ApiKeyService svc) { this.svc = svc; }

  @GetMapping
  public List<ApiKeyDto> list() { 
    return null;
    // return svc.list(); 
  }

  @PostMapping
  public NewKeyDto create() { 
    return null;
  }
    // return svc.create(); } // returns plaintext once

  @DeleteMapping("/{id}")
  public void revoke(@PathVariable String id) { 
    
   // svc.revoke(id);
   }
}