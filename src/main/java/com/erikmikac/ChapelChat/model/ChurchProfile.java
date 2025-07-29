package com.erikmikac.ChapelChat.model;

import java.util.Map;

import lombok.Data;

@Data
public class ChurchProfile {
    private String churchName;
    private String pastor;
    private String location;
    private Map<String, String> serviceTimes;
    private YouthGroup youthGroup;
    private String tone;

    @Data
    public static class YouthGroup {
        private String day;
        private String time;
        private String description;
        // getters and setters
    }

    // getters and setters
}
