package com.erikmikac.ChapelChat.model;

public record AskContext(
  AskRequest askRequest,
  String churchId,
  String ip,
  String userAgent,
  String prompt,
  String profileChecksum,
  String requestId // for log tracability
) {}