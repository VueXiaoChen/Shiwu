package com.fast.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ConfigurationProperties(prefix = "fast")
public class fastConfig {

    private String profile;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        if (profile != null && (profile.startsWith("./") || profile.startsWith(".\\"))) {
            String userDir = System.getProperty("user.dir");
            String relativePath = profile.substring(2);
            File pomFile = new File(userDir, "pom.xml");
            if (pomFile.exists()) {
                userDir = new File(userDir).getParent();
            }
            profile = userDir + File.separator + relativePath;
        }
        this.profile = profile;
    }
}