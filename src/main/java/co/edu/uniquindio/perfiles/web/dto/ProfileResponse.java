package co.edu.uniquindio.perfiles.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

public class ProfileResponse {
    @Schema(description = "Identificador del perfil")
    private UUID id;
    private String pageUrl;
    private String nickname;
    private boolean contactPublic;
    private String address;
    private String bio;
    private String organization;
    private String country;
    private Map<String, String> socialLinks;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public boolean isContactPublic() { return contactPublic; }
    public void setContactPublic(boolean contactPublic) { this.contactPublic = contactPublic; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Map<String, String> getSocialLinks() { return socialLinks; }
    public void setSocialLinks(Map<String, String> socialLinks) { this.socialLinks = socialLinks; }
}
