package co.edu.uniquindio.perfiles.service;

import co.edu.uniquindio.perfiles.model.Profile;
import co.edu.uniquindio.perfiles.repository.ProfileRepository;
import co.edu.uniquindio.perfiles.web.dto.ProfilePatchRequest;
import co.edu.uniquindio.perfiles.web.dto.ProfileRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final ProfileRepository repository;

    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
    }

    public Profile getByUserIdOrThrow(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for userId=" + userId));
    }

    @Transactional
    public Profile create(Long userId, ProfileRequest req) {
        if (repository.existsByUserId(userId)) {
            throw new IllegalStateException("Profile already exists for userId=" + userId);
        }
        Profile p = new Profile();
        p.setUserId(userId);
        applyRequest(p, req);
        return repository.save(p);
    }

    @Transactional
    public Profile replace(Long userId, ProfileRequest req) {
        Profile p = repository.findByUserId(userId).orElseGet(() -> {
            Profile np = new Profile();
            np.setUserId(userId);
            return np;
        });
        applyRequest(p, req);
        return repository.save(p);
    }

    @Transactional
    public Profile patch(Long userId, ProfilePatchRequest req) {
        Profile p = getByUserIdOrThrow(userId);
        if (req.getPageUrl() != null) p.setPageUrl(req.getPageUrl());
        if (req.getNickname() != null) p.setNickname(req.getNickname());
        if (req.getContactPublic() != null) p.setContactPublic(req.getContactPublic());
        if (req.getAddress() != null) p.setAddress(req.getAddress());
        if (req.getBio() != null) p.setBio(req.getBio());
        if (req.getOrganization() != null) p.setOrganization(req.getOrganization());
        if (req.getCountry() != null) p.setCountry(req.getCountry());
        if (req.getSocialLinks() != null) p.setSocialLinks(req.getSocialLinks());
        return repository.save(p);
    }

    @Transactional
    public void delete(Long userId) {
        if (!repository.existsByUserId(userId)) {
            throw new EntityNotFoundException("Profile not found for userId=" + userId);
        }
        repository.deleteByUserId(userId);
    }

    private void applyRequest(Profile p, ProfileRequest req) {
        p.setPageUrl(req.getPageUrl());
        p.setNickname(req.getNickname());
        p.setContactPublic(Boolean.TRUE.equals(req.getContactPublic()));
        p.setAddress(req.getAddress());
        p.setBio(req.getBio());
        p.setOrganization(req.getOrganization());
        p.setCountry(req.getCountry());
        p.setSocialLinks(req.getSocialLinks());
    }
}
