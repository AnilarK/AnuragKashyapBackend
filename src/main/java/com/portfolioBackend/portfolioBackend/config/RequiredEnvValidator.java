package com.portfolioBackend.portfolioBackend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Production-ready behavior: fail fast if required secrets are missing.
 * This prevents silent fallbacks like connecting to localhost:27017.
 */
@Component
public class RequiredEnvValidator implements ApplicationRunner {

    private final Environment environment;

    public RequiredEnvValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Local uses application-local.yml, so don't force env vars there.
        String[] active = environment.getActiveProfiles();
        boolean isLocal = active != null && java.util.Arrays.asList(active).contains("local");
        if (isLocal) {
            return;
        }

        List<String> missing = new ArrayList<>();

        // In prod (or any non-local profile), require env vars.
        require("MONGODB_URI", missing);
        require("JWT_SECRET", missing);

        // Email OTP requires these if you call the OTP endpoint:
        // require("MAIL_USERNAME", missing);
        // require("MAIL_PASSWORD", missing);

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required environment variables: " + String.join(", ", missing));
        }
    }

    private void require(String key, List<String> missing) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            missing.add(key);
        }
    }
}

