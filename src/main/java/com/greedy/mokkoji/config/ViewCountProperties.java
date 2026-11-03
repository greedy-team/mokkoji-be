package com.greedy.mokkoji.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "view-count")
public class ViewCountProperties {

    private boolean enabled = true;

    @NotNull
    private Duration commandTimeout = Duration.ofMillis(50);

    @NotNull
    private Duration dedupTtl = Duration.ofMinutes(30);

    @NotNull
    private Duration failureBackoff = Duration.ofSeconds(5);

    @NotBlank
    private String keyPrefix = "mokkoji:view-count";

    @Min(1)
    private int maxBatchesPerRun = 32;

    @Min(1)
    private int maxPendingBatches = 256;
}
