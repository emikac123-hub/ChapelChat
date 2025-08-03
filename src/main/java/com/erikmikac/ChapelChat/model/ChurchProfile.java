package com.erikmikac.ChapelChat.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ChurchProfile {
    private String organizationName;
    private String slug;
    private String denomination;
    private String location;
    private Geo geo;
    private Contact contact;
    private String tone;
    private List<String> languages;
    private Leader leader;
    private List<Gathering> gatherings;
    private List<Program> programs;
    private List<Event> events;
    private String beliefs;

    @JsonProperty("sacramentsOrPractices")
    private List<Practice> sacramentsOrPractices;

    private List<FAQ> faq;
    private SocialMedia socialMedia;
    private Media media;
    private Donation donation;
    private Accessibility accessibility;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private LocalDateTime lastUpdated;

    @Data
    public static class Geo {
        private Double latitude;
        private Double longitude;
    }

    @Data
    public static class Contact {
        private String phone;
        private String email;
        private String website;
    }

    @Data
    public static class Leader {
        private String title;
        private String name;
        private String bio;
        private String imageUrl;
    }

    @Data
    public static class Gathering {
        private String day;
        private String time;
        private String label;
        private String location;
        private Boolean isLivestreamed;
        private String livestreamUrl;
    }

    @Data
    public static class Program {
        private String name;
        private String audience;
        private String schedule;
        private String description;
    }

    @Data
    public static class Event {
        private String name;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        private String time;
        private String location;
        private String description;
    }

    @Data
    public static class Practice {
        private String name;
        private String description;
    }

    @Data
    public static class FAQ {
        private String question;
        private String answer;
    }

    @Data
    public static class SocialMedia {
        private String facebook;
        private String instagram;
        private String twitter;
        private String youtube;
        private String tikTok;
    }

    @Data
    public static class Media {
        private String heroImage;
        private String logoImage;
        private List<String> gallery;
        private String promoVideo;
    }

    @Data
    public static class Donation {
        private String donationUrl;
        private String platform;
    }

    @Data
    public static class Accessibility {
        private Boolean wheelchairAccessible;
        private Boolean hearingAssistanceAvailable;
        private Boolean childcareAvailable;
    }
}
