package com.nuraly.keycloak.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuraly.keycloak.client.model.EmailToUuidMapping;
import com.nuraly.keycloak.client.model.EmailToUuidRequest;
import com.nuraly.keycloak.client.model.EmailToUuidResponse;
import com.nuraly.keycloak.client.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.ConfigProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * HTTP client implementation for Keycloak service operations.
 * Makes REST API calls to the Keycloak service server.
 */
@ApplicationScoped
public class HttpKeycloakClient implements KeycloakClient {
    
    private static final Logger LOG = Logger.getLogger(HttpKeycloakClient.class.getName());
    
    @ConfigProperty(name = "keycloak.service.base-url", defaultValue = "http://localhost:7012")
    String baseUrl;
    
    @ConfigProperty(name = "keycloak.service.timeout-seconds", defaultValue = "30")
    int timeoutSeconds;
    
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    
    public HttpKeycloakClient() {
        // Default constructor for CDI
    }
    
    public HttpKeycloakClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.timeoutSeconds = 30; // Set default timeout
        init();
    }
    
    @PostConstruct
    void init() {
        // Ensure we have a valid timeout
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 30; // Default to 30 seconds
        }
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        this.objectMapper = new ObjectMapper();
        
        // Ensure baseUrl doesn't end with slash
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        LOG.info("Initialized Keycloak HTTP client with base URL: " + baseUrl);
    }
    
    @Override
    public Optional<User> getUserByEmail(String email) {
        try {
            String url = baseUrl + "/api/v1/users/by-email/" + email;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                User user = objectMapper.readValue(response.body(), User.class);
                return Optional.of(user);
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                LOG.warning("Failed to get user by email: " + email + ", status: " + response.statusCode());
                return Optional.empty();
            }
        } catch (Exception e) {
            LOG.severe("Error getting user by email: " + email + ", error: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> getUserById(String id) {
        try {
            String url = baseUrl + "/api/v1/users/by-id/" + id;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                User user = objectMapper.readValue(response.body(), User.class);
                return Optional.of(user);
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                LOG.warning("Failed to get user by id: " + id + ", status: " + response.statusCode());
                return Optional.empty();
            }
        } catch (Exception e) {
            LOG.severe("Error getting user by id: " + id + ", error: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> getUsersByIds(List<String> ids) {
        try {
            EmailToUuidRequest request = new EmailToUuidRequest(ids);
            String url = baseUrl + "/api/v1/users/by-ids";
            
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                User[] users = objectMapper.readValue(response.body(), User[].class);
                return List.of(users);
            } else {
                LOG.warning("Failed to get users by ids, status: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOG.severe("Error getting users by ids, error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public Optional<String> getEmailToUuidMapping(String email) {
        Optional<User> user = getUserByEmail(email);
        return user.map(User::getId);
    }
    
    @Override
    public List<String> getUserIdsByEmails(List<String> emails) {
        try {
            EmailToUuidRequest request = new EmailToUuidRequest(emails);
            String url = baseUrl + "/api/v1/users/emails-to-uuids";
            
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String[] uuids = objectMapper.readValue(response.body(), String[].class);
                return List.of(uuids);
            } else {
                LOG.warning("Failed to get user ids by emails, status: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOG.severe("Error getting user ids by emails, error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<EmailToUuidMapping> getEmailToUuidMappings(List<String> emails) {
        try {
            EmailToUuidRequest request = new EmailToUuidRequest(emails);
            String url = baseUrl + "/api/v1/users/email-uuid-mappings";
            
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                EmailToUuidMapping[] mappings = objectMapper.readValue(response.body(), EmailToUuidMapping[].class);
                return List.of(mappings);
            } else {
                LOG.warning("Failed to get email to uuid mappings, status: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOG.severe("Error getting email to uuid mappings, error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public EmailToUuidResponse convertEmailsToUuids(EmailToUuidRequest request) {
        try {
            String url = baseUrl + "/api/v1/users/convert-emails-to-uuids";
            
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), EmailToUuidResponse.class);
            } else {
                LOG.warning("Failed to convert emails to uuids, status: " + response.statusCode());
                return new EmailToUuidResponse(new ArrayList<>(), request.getEmails());
            }
        } catch (Exception e) {
            LOG.severe("Error converting emails to uuids, error: " + e.getMessage());
            return new EmailToUuidResponse(new ArrayList<>(), request.getEmails());
        }
    }
    
    @Override
    public boolean isServiceHealthy() {
        try {
            String url = baseUrl + "/api/v1/health";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            LOG.warning("Keycloak service health check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Factory method for creating instances with custom configuration
     */
    public static HttpKeycloakClient create(String baseUrl) {
        return new HttpKeycloakClient(baseUrl);
    }
    
    /**
     * Factory method for creating instances using CDI configuration
     */
    public static KeycloakClient createFromConfig() {
        try {
            return CDI.current().select(HttpKeycloakClient.class).get();
        } catch (Exception cdiException) {
            // CDI not available, try configuration properties
            try {
                String baseUrl = ConfigProvider.getConfig()
                        .getOptionalValue("keycloak.service.base-url", String.class)
                        .orElse("http://localhost:7012");
                return new HttpKeycloakClient(baseUrl);
            } catch (Exception configException) {
                // Config provider not available either, use defaults
                return new HttpKeycloakClient("http://localhost:7012");
            }
        }
    }
}
