package co.edu.uniquindio.perfiles.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.URL;

import java.util.Map;

/**
 * DTO para actualizaciones parciales; campos null no se modifican.
 */
public class ProfilePatchRequest {
    @Schema(description = "URL de la página personal del usuario")
    @URL(message = "Debe ser una URL válida")
    private String pageUrl;
    private String nickname;
    private Boolean contactPublic;
    private String address;
    private String bio;
    private String organization;
    private String country;
    private Map<String, String> socialLinks;

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Boolean getContactPublic() { return contactPublic; }
    public void setContactPublic(Boolean contactPublic) { this.contactPublic = contactPublic; }
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
