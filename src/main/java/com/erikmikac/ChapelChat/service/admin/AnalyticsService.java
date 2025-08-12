package com.erikmikac.ChapelChat.service.admin;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.erikmikac.ChapelChat.model.admin.SummaryDto;
import com.erikmikac.ChapelChat.repository.admin.AnalyticsRepository;

@Service
public class AnalyticsService {
  private final AnalyticsRepository repo;

  public AnalyticsService(AnalyticsRepository repo) {
    this.repo = repo;
  }

  public SummaryDto fetchSummary(String churchId, OffsetDateTime from, OffsetDateTime to) {
    return repo.fetchSummary(churchId, from, to);
  }
}
