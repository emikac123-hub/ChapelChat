package com.erikmikac.ChapelChat.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikmikac.ChapelChat.exceptions.ChurchProfileNotFoundException;
import com.erikmikac.ChapelChat.model.AskRequest;
import com.erikmikac.ChapelChat.model.AskResponse;
import com.erikmikac.ChapelChat.service.ChurchApiKeyService;
import com.erikmikac.ChapelChat.service.ChurchProfileService;
import com.erikmikac.ChapelChat.service.OpenAiService;

@RestController
@RequestMapping("/ask")
public class AskController {

    private final ChurchProfileService profileService;
    private final OpenAiService aiService;

    @Autowired
    private ChurchApiKeyService apiKeyService;

    public AskController(ChurchProfileService profileService, OpenAiService aiService) {
        this.profileService = profileService;
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<AskResponse> ask(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestBody AskRequest request) {
        final Optional<String> churchIdOpt = apiKeyService.getChurchIdForValidKey(apiKey);
        if (churchIdOpt.isEmpty()) {
            return ResponseEntity.status(401).body(new AskResponse("Invalid or revoked API key."));
        }

        try {
            final String churchId = churchIdOpt.get();
            final String prompt = profileService.getSystemPromptFor(churchId);
            final String answer = aiService.generateAnswer(prompt, request.getMessage());
            return ResponseEntity.ok(new AskResponse(answer));
        } catch (ChurchProfileNotFoundException e) {
            return ResponseEntity.badRequest().body(new AskResponse("That church's profile could not be found."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new AskResponse("An unexpected error occurred."));
        }
    }

}
