package com.raxim.myscoutee.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("config")
public class ConfigProperties {
    private String adminUser;
    private String firebaseServiceAccountPath;

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getFirebaseServiceAccountPath() {
        return firebaseServiceAccountPath;
    }

    public void setFirebaseServiceAccountPath(String firebaseServiceAccountPath) {
        this.firebaseServiceAccountPath = firebaseServiceAccountPath;
    }
}
