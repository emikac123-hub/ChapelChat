package com.erikmikac.ChapelChat.controller.admin;

import org.springframework.web.bind.annotation.*;

import com.erikmikac.ChapelChat.model.admin.DailyDto;
import com.erikmikac.ChapelChat.model.admin.HeatCellDto;
import com.erikmikac.ChapelChat.model.admin.ModelSplitDto;
import com.erikmikac.ChapelChat.model.admin.SlowRowDto;
import com.erikmikac.ChapelChat.model.admin.SummaryDto;
import com.erikmikac.ChapelChat.service.admin.AnalyticsService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/analytics")
public class AnalyticsController {

  private final AnalyticsService svc;
  public AnalyticsController(AnalyticsService svc) { this.svc = svc; }

  @GetMapping("/summary")
  public SummaryDto summary(@RequestParam String from, @RequestParam String to) {
    return svc.fetchSummary(from, from, to);
  }

  @GetMapping("/timeseries")
  public List<DailyDto> timeseries(@RequestParam String from, @RequestParam String to) {
    return svc.fetchDaily(from, to);
  }

  @GetMapping("/model-split")
  public List<ModelSplitDto> modelSplit(@RequestParam String from, @RequestParam String to) {
    return svc.fetchModelSplit(from, to);
  }

  @GetMapping("/heatmap")
  public List<HeatCellDto> heatmap(@RequestParam String from, @RequestParam String to) {
    return svc.fetchHeatmap(from, to);
  }

  @GetMapping("/slowest")
  public List<SlowRowDto> slowest(@RequestParam String from, @RequestParam String to) {
    return svc.fetchSlowest(from, to);
  }
}