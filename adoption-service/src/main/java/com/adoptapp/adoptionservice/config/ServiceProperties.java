package com.adoptapp.adoptionservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {

    private Client userService = new Client();
    private Client petService = new Client();

    public Client getUserService() { return userService; }
    public void setUserService(Client userService) { this.userService = userService; }
    public Client getPetService() { return petService; }
    public void setPetService(Client petService) { this.petService = petService; }

    public static class Client {
        private String url;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
