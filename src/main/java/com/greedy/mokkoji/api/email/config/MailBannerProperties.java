package com.greedy.mokkoji.api.email.config;

import com.greedy.mokkoji.enums.university.UniversityCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;


@ConfigurationProperties(prefix = "mokkoji.mail")
public record MailBannerProperties(Map<UniversityCode, String> bannerUrls) {

    public MailBannerProperties {
        bannerUrls = bannerUrls == null ? Map.of() : Map.copyOf(bannerUrls);
    }

    public Optional<String> bannerUrlOf(final UniversityCode universityCode) {
        return Optional.ofNullable(bannerUrls.get(universityCode))
                .filter(url -> !url.isBlank());
    }
}
