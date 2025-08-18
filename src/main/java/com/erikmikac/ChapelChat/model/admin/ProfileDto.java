package com.erikmikac.ChapelChat.model.admin;
/**
 * Editable church profile payload for the Knowledge tab.
 * Wraps the JSONB profile plus minimal publishing metadata.
 *
 * @param slug         unique slug for the church (case-insensitive if using CITEXT)
 * @param profile      arbitrary JSON object with church details (service times, parking, leaders, etc.)
 * @param is_published whether this profile is currently published to the widget
 */
public record ProfileDto(String slug, Object profile, boolean is_published) {}