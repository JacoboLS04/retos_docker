package co.edu.uniquindio.perfiles.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "profiles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"})
})
public class Profile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @URL
    @Column(name = "page_url")
    private String pageUrl;

    @Size(max = 50)
    private String nickname;

    @Column(name = "contact_public")
    private boolean contactPublic;

    @Size(max = 255)
    private String address;

    @Size(max = 2000)
    private String bio;

    @Size(max = 255)
    private String organization;

    @Size(max = 100)
    private String country;

    @ElementCollection
    @CollectionTable(name = "profile_social_links", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "platform")
    @Column(name = "url")
    private Map<String, String> socialLinks = new HashMap<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
