package com.erikmikac.ChapelChat.service;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.erikmikac.ChapelChat.exceptions.ChurchProfileNotFoundException;
import com.erikmikac.ChapelChat.model.ChurchProfile;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChurchProfileService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getSystemPromptFor(String churchId) throws ChurchProfileNotFoundException {
        try {
            ChurchProfile profile = objectMapper.readValue(
                    new ClassPathResource("churches/" + churchId + ".json").getInputStream(),
                    ChurchProfile.class);
            return buildSystemPrompt(profile);

        } catch (IOException e) {
            throw new ChurchProfileNotFoundException("Could not load profile for church: " + churchId, e);
        }
    }

    private String buildSystemPrompt(ChurchProfile profile) {
        return String.format(
                "%s You are an AI assistant for %s, located at %s. The head pastor is %s. Sunday service is at %s. The youth group meets on %s at %s (%s).",
                profile.getTone(),
                profile.getChurchName(),
                profile.getLocation(),
                profile.getPastor(),
                profile.getServiceTimes().get("sunday"),
                profile.getYouthGroup().getDay(),
                profile.getYouthGroup().getTime(),
                profile.getYouthGroup().getDescription());
    }
}
