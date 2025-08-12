package com.erikmikac.ChapelChat.service.admin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.erikmikac.ChapelChat.exceptions.ChurchProfileNotFoundException;
import com.erikmikac.ChapelChat.model.ChurchProfile;
import com.erikmikac.ChapelChat.model.PromptWithChecksum;
import com.erikmikac.ChapelChat.repository.ChurchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChurchProfileService {

    private final ChurchRepository churchRepository;
    private static final String PROFILE_DIR = "src/main/resources/churches/";
    private final ObjectMapper objectMapper;

    public ChurchProfileService(ObjectMapper objectMapper, ChurchRepository churchRepository) {
        this.objectMapper = objectMapper;
        this.churchRepository = churchRepository;
    }

    public PromptWithChecksum getSystemPromptAndChecksumFor(String churchId) throws ChurchProfileNotFoundException {
        try {
            ChurchProfile profile = objectMapper.readValue(
                    new ClassPathResource("churches/" + churchId + ".json").getInputStream(),
                    ChurchProfile.class);

            String prompt = buildSystemPrompt(profile);
            String checksum = getProfileChecksum(profile);

            return new PromptWithChecksum(prompt, checksum);

        } catch (IOException e) {
            log.error("Error: Church Profile Not Found: ", e);
            throw new ChurchProfileNotFoundException("Could not load profile for church: " + churchId, e);
        }
    }

    private String buildSystemPrompt(ChurchProfile profile) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(String.format(
                "%s You are an AI assistant for %s, a %s church located at %s. ",
                profile.getTone(),
                profile.getOrganizationName(),
                profile.getDenomination(),
                profile.getLocation()));

        if (profile.getLeader() != null) {
            prompt.append(String.format("The head %s is %s. ", profile.getLeader().getTitle(),
                    profile.getLeader().getName()));
        }

        if (profile.getContact() != null) {
            prompt.append(String.format("Contact info: phone %s, email %s, website %s. ",
                    profile.getContact().getPhone(),
                    profile.getContact().getEmail(),
                    profile.getContact().getWebsite()));
        }

        if (profile.getGatherings() != null && !profile.getGatherings().isEmpty()) {
            prompt.append("Gatherings include: ");
            for (ChurchProfile.Gathering g : profile.getGatherings()) {
                prompt.append(String.format("%s at %s (%s)%s; ",
                        g.getDay(), g.getTime(), g.getLabel(),
                        Boolean.TRUE.equals(g.getIsLivestreamed()) ? " - livestreamed" : ""));
            }
        }

        if (profile.getPrograms() != null && !profile.getPrograms().isEmpty()) {
            prompt.append("Programs offered: ");
            for (ChurchProfile.Program p : profile.getPrograms()) {
                prompt.append(String.format("%s for %s (%s); ", p.getName(), p.getAudience(), p.getSchedule()));
            }
        }

        if (profile.getSacramentsOrPractices() != null && !profile.getSacramentsOrPractices().isEmpty()) {
            prompt.append("Practices include: ");
            for (ChurchProfile.Practice p : profile.getSacramentsOrPractices()) {
                prompt.append(String.format("%s - %s; ", p.getName(), p.getDescription()));
            }
        }

        if (profile.getAccessibility() != null) {
            prompt.append("Accessibility features: ");
            if (Boolean.TRUE.equals(profile.getAccessibility().getWheelchairAccessible()))
                prompt.append("Wheelchair accessible; ");
            if (Boolean.TRUE.equals(profile.getAccessibility().getHearingAssistanceAvailable()))
                prompt.append("Hearing assistance available; ");
            if (Boolean.TRUE.equals(profile.getAccessibility().getChildcareAvailable()))
                prompt.append("Childcare available; ");
        }

        if (profile.getDonation() != null) {
            prompt.append(String.format("Donations can be made through %s at %s. ",
                    profile.getDonation().getPlatform(),
                    profile.getDonation().getDonationUrl()));
        }

        if (profile.getBeliefs() != null && !profile.getBeliefs().isBlank()) {
            prompt.append(String.format("This church believes: %s. ", profile.getBeliefs()));
        }

        if (profile.getEvents() != null && !profile.getEvents().isEmpty()) {
            prompt.append("Upcoming events: ");
            for (ChurchProfile.Event e : profile.getEvents()) {
                prompt.append(String.format("%s on %s (%s); ",
                        e.getName(),
                        e.getDate().toString(),
                        e.getDescription()));
            }
        }

        if (profile.getLanguages() != null && !profile.getLanguages().isEmpty()) {
            prompt.append("Services offered in: ");
            prompt.append(String.join(", ", profile.getLanguages()));
            prompt.append(". ");
        }

        if (profile.getFaq() != null && !profile.getFaq().isEmpty()) {
            prompt.append("Frequently asked questions: ");
            for (ChurchProfile.FAQ f : profile.getFaq()) {
                prompt.append(String.format("Q: %s A: %s; ", f.getQuestion(), f.getAnswer()));
            }
        }

        if (profile.getSocialMedia() != null) {
            prompt.append("Social media: ");
            if (profile.getSocialMedia().getFacebook() != null)
                prompt.append("Facebook: ").append(profile.getSocialMedia().getFacebook()).append("; ");
            if (profile.getSocialMedia().getTwitter() != null)
                prompt.append("Twitter: ").append(profile.getSocialMedia().getTwitter()).append("; ");
            if (profile.getSocialMedia().getInstagram() != null)
                prompt.append("Instagram: ").append(profile.getSocialMedia().getInstagram()).append("; ");
            if (profile.getSocialMedia().getYoutube() != null)
                prompt.append("YouTube: ").append(profile.getSocialMedia().getYoutube()).append("; ");
            if (profile.getSocialMedia().getTikTok() != null)
                prompt.append("TikTok: ").append(profile.getSocialMedia().getTikTok()).append("; ");
        }

        if (profile.getLastUpdated() != null) {
            prompt.append(String.format("Profile last updated on %s. ", profile.getLastUpdated().toString()));
        }

        if (profile.getMedia() != null && profile.getMedia().getPromoVideo() != null) {
            prompt.append(
                    String.format("A promotional video is available here: %s. ", profile.getMedia().getPromoVideo()));
        }

        return prompt.toString().trim();
    }

    public String getContactEmailFor(String churchId) {
        return churchRepository.findContactEmailByChurchId(churchId)
                .orElse(null); // Or throw if you prefer
    }

    public ChurchProfile getProfile(String churchId) {
        try {
            File file = Paths.get(PROFILE_DIR, churchId + ".json").toFile();
            if (!file.exists()) {
                throw new IllegalArgumentException("Church profile not found: " + churchId);
            }
            return objectMapper.readValue(file, ChurchProfile.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading church profile for " + churchId, e);
        }
    }

    public void updateProfile(String churchId, ChurchProfile profile) {
        try {
            File file = Paths.get(PROFILE_DIR, churchId + ".json").toFile();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, profile);
        } catch (IOException e) {
            throw new RuntimeException("Error writing church profile for " + churchId, e);
        }
    }

    /**
     * Benefit Description
     * 🧼 Prevent ghost bugs You can explain why a chatbot gave a weird answer —
     * "that profile config is old"
     * 📊 Filter analytics by version Don’t include outdated data in
     * tone/performance reporting
     * 🛠️ Trigger reprocessing Re-run logs through the bot after profile updates if
     * needed
     * 🧠 Debugging See if hallucinations were tied to specific doctrinal misconfigs
     * 
     * @param profile
     * @return
     */
    public String getProfileChecksum(ChurchProfile profile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String serialized = mapper.writeValueAsString(profile);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(serialized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash profile", e);
        }
    }

}
