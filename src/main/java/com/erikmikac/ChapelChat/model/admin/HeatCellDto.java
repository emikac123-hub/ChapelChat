package com.erikmikac.ChapelChat.model.admin;
/**
 * Heatmap cell value for weekday × hour grids.
 * Returned by /admin/analytics/heatmap.
 *
 * @param dow            day of week (0=Sun … 6=Sat)
 * @param hour           hour of day (0–23)
 * @param conversations  number of conversations in this bucket
 */
public record HeatCellDto(int dow, int hour, long conversations) {}