package com.erikmikac;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erikmikac.ChapelChat.service.OpenAiService;

@RestController
public class TestGptController {

    private final OpenAiService openAiService;

    public TestGptController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @GetMapping("/test-gpt")
    public String testGpt() {
        String systemPrompt = "You are a helpful assistant for Hope Baptist Church. The head pastor is Rev. John Matthews. Be brief and respectful.";
        String userMessage = "Who is your pastor?";
        return openAiService.generateAnswer(systemPrompt, userMessage);
    }
}
