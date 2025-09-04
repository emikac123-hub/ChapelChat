package com.erikmikac.ChapelChat.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationProfile {
    private String organizationName;
    private String slug;
    private String location;
    private Geo geo;
    private Contact contact;
    private String tone;
    private List<String> languages;
    private SocialMedia socialMedia;
    private Media media;
    private List<FAQ> faq;

    // Optional, per-vertical details (only one will typically be non-null)
    private ChurchDetails church; // CHURCH-only fields
    private SmbDetails smb;       // SMB-only fields

    private LocalDateTime lastUpdated;

    // ===== common nested types (from your current class) =====
    @Data public static class Geo { Double latitude; Double longitude; }
    @Data public static class Contact { String phone; String email; String website; }
    @Data public static class FAQ { String question; String answer; }
    @Data public static class SocialMedia { String facebook, instagram, twitter, youtube, tikTok; }
    @Data public static class Media { String heroImage, logoImage, promoVideo; List<String> gallery; }

    // ===== church-specific =====
    @Data public static class ChurchDetails {
        private String denomination;
        private Leader leader;
        private List<Gathering> gatherings;
        private List<Program> programs;
        private List<Event> events;
        private String beliefs;
        private List<Practice> sacramentsOrPractices;

        @Data public static class Leader { String title; String name; String bio; String imageUrl; }
        @Data public static class Gathering { String day; String time; String label; String location; Boolean isLivestreamed; String livestreamUrl; }
        @Data public static class Program { String name; String audience; String schedule; String description; }
        @Data public static class Event { String name; LocalDate date; String time; String location; String description; }
        @Data public static class Practice { String name; String description; }
    }

    // ===== SMB-specific =====
    @Data public static class SmbDetails {
        private String industry;                 // e.g., dental, HVAC, boutique
        private List<BusinessHour> hours;        // structured hours
        private List<Service> services;          // offerings with pricing/desc
        private List<Department> departments;    // Sales, Support, Billing, etc.
        private Policies policies;               // returns, refunds, terms
        private String about;                    // brief “about us”
        @Data public static class BusinessHour { String day; String open; String close; Boolean closed; }
        @Data public static class Service { String name; String description; String price; }
        @Data public static class Department { String name; String phone; String email; String url; }
        @Data public static class Policies { String returns; String shipping; String cancellations; String privacy; }
    }
}
