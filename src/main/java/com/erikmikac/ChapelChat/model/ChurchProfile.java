package com.erikmikac.ChapelChat.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Represents the profile of a church, containing all the information
 * needed to generate responses for the chatbot.
 */
@Data
public class ChurchProfile {
    /** The name of the church or organization. */
    private String organizationName;
    /** A unique identifier for the church, used in URLs. */
    private String slug;
    /** The denomination of the church (e.g., Baptist, Catholic). */
    private String denomination;
    /** The physical address of the church. */
    private String location;
    /** The geographical coordinates of the church. */
    private Geo geo;
    /** Contact information for the church. */
    private Contact contact;
    /** The desired tone for the chatbot's responses (e.g., formal, friendly). */
    private String tone;
    /** A list of languages supported by the church. */
    private List<String> languages;
    /** Information about the church's leader. */
    private Leader leader;
    /** A list of regular gatherings or services. */
    private List<Gathering> gatherings;
    /** A list of programs offered by the church. */
    private List<Program> programs;
    /** A list of upcoming events. */
    private List<Event> events;
    /** A summary of the church's beliefs. */
    private String beliefs;

    /** A list of sacraments or practices observed by the church. */
    @JsonProperty("sacramentsOrPractices")
    private List<Practice> sacramentsOrPractices;

    /** A list of frequently asked questions and their answers. */
    private List<FAQ> faq;
    /** Links to the church's social media profiles. */
    private SocialMedia socialMedia;
    /** Media assets for the church, such as images and videos. */
    private Media media;
    /** Information about how to donate to the church. */
    private Donation donation;
    /** Information about accessibility options at the church. */
    private Accessibility accessibility;

    /** The date and time when the profile was last updated. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private LocalDateTime lastUpdated;

    /** Represents geographical coordinates. */
    @Data
    public static class Geo {
        /** The latitude of the location. */
        private Double latitude;
        /** The longitude of the location. */
        private Double longitude;
    }

    /** Represents contact information. */
    @Data
    public static class Contact {
        /** The phone number. */
        private String phone;
        /** The email address. */
        private String email;
        /** The church's website URL. */
        private String website;
    }

    /** Represents a leader at the church. */
    @Data
    public static class Leader {
        /** The title of the leader (e.g., Pastor, Reverend). */
        private String title;
        /** The name of the leader. */
        private String name;
        /** A short biography of the leader. */
        private String bio;
        /** A URL for the leader's image. */
        private String imageUrl;
    }

    /** Represents a regular gathering or service. */
    @Data
    public static class Gathering {
        /** The day of the week the gathering takes place. */
        private String day;
        /** The time of the gathering. */
        private String time;
        /** A label for the gathering (e.g., Morning Worship, Youth Group). */
        private String label;
        /** The location of the gathering. */
        private String location;
        /** Whether the gathering is livestreamed. */
        private Boolean isLivestreamed;
        /** The URL for the livestream. */
        private String livestreamUrl;
    }

    /** Represents a program offered by the church. */
    @Data
    public static class Program {
        /** The name of the program. */
        private String name;
        /** The target audience for the program. */
        private String audience;
        /** The schedule for the program. */
        private String schedule;
        /** A description of the program. */
        private String description;
    }

    /** Represents a special event. */
    @Data
    public static class Event {
        /** The name of the event. */
        private String name;

        /** The date of the event. */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        /** The time of the event. */
        private String time;
        /** The location of the event. */
        private String location;
        /** A description of the event. */
        private String description;
    }

    /** Represents a sacrament or practice. */
    @Data
    public static class Practice {
        /** The name of the practice. */
        private String name;
        /** A description of the practice. */
        private String description;
    }

    /** Represents a frequently asked question. */
    @Data
    public static class FAQ {
        /** The question. */
        private String question;
        /** The answer to the question. */
        private String answer;
    }

    /** Represents social media links. */
    @Data
    public static class SocialMedia {
        /** The Facebook page URL. */
        private String facebook;
        /** The Instagram profile URL. */
        private String instagram;
        /** The Twitter profile URL. */
        private String twitter;
        /** The YouTube channel URL. */
        private String youtube;
        /** The TikTok profile URL. */
        private String tikTok;
    }

    /** Represents media assets. */
    @Data
    public static class Media {
        /** A URL for a hero image. */
        private String heroImage;
        /** A URL for the church's logo. */
        private String logoImage;
        /** A list of URLs for a photo gallery. */
        private List<String> gallery;
        /** A URL for a promotional video. */
        private String promoVideo;
    }

    /** Represents donation information. */
    @Data
    public static class Donation {
        /** The URL for the donation page. */
        private String donationUrl;
        /** The platform used for donations (e.g., PayPal, Stripe). */
        private String platform;
    }

    /** Represents accessibility information. */
    @Data
    public static class Accessibility {
        /** Whether the facility is wheelchair accessible. */
        private Boolean wheelchairAccessible;
        /** Whether hearing assistance is available. */
        private Boolean hearingAssistanceAvailable;
        /** Whether childcare is available. */
        private Boolean childcareAvailable;
    }
}
