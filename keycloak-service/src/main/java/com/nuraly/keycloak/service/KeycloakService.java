package com.nuraly.keycloak.service;

import com.nuraly.keycloak.dto.EmailToUuidMapping;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class KeycloakService {

    private static final Logger LOG = Logger.getLogger(KeycloakService.class);

    @ConfigProperty(name = "keycloak.base-url")
    String keycloakBaseUrl;

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    @ConfigProperty(name = "keycloak.client-id")
    String clientId;

    @ConfigProperty(name = "keycloak.client-secret", defaultValue = "")
    Optional<String> clientSecret;

    @ConfigProperty(name = "keycloak.username", defaultValue = "")
    Optional<String> username;

    @ConfigProperty(name = "keycloak.password", defaultValue = "")
    Optional<String> password;

    @Inject
    CacheService cacheService;

    private Keycloak keycloak;
    private UsersResource usersResource;

    @PostConstruct
    void init() {
        try {
            LOG.info("Initializing Keycloak service with:");
            LOG.info("  Server URL: " + keycloakBaseUrl);
            LOG.info("  Realm: " + realm);
            LOG.info("  Client ID: " + clientId);
            
            KeycloakBuilder builder = KeycloakBuilder.builder()
                    .serverUrl(keycloakBaseUrl)
                    .realm(realm)
                    .clientId(clientId);

            // Prefer client credentials over username/password
            if (clientSecret.isPresent() && !clientSecret.get().trim().isEmpty()) {
                LOG.info("Using client credentials authentication");
                builder.clientSecret(clientSecret.get())
                       .grantType("client_credentials");
            } else if (username.isPresent() && password.isPresent() && 
                      !username.get().trim().isEmpty() && !password.get().trim().isEmpty()) {
                LOG.info("Using username/password authentication (not recommended for production)");
                LOG.info("  Username: " + username.get());
                builder.username(username.get())
                       .password(password.get());
            } else {
                throw new IllegalStateException("Either client-secret or username/password must be provided");
            }

            this.keycloak = builder.build();

            // Test the connection by getting a token
            try {
                String token = keycloak.tokenManager().getAccessTokenString();
                LOG.info("Successfully obtained access token: " + token.substring(0, Math.min(20, token.length())) + "...");
            } catch (Exception tokenException) {
                LOG.error("Failed to obtain access token - check Keycloak configuration", tokenException);
                throw tokenException;
            }

            this.usersResource = keycloak.realm(realm).users();
            LOG.info("Keycloak service initialized successfully with client credentials");
        } catch (Exception e) {
            LOG.error("Failed to initialize Keycloak service. Please verify:", e);
            LOG.error("1. Keycloak is running at: " + keycloakBaseUrl);
            LOG.error("2. Realm '" + realm + "' exists");
            LOG.error("3. Client '" + clientId + "' exists and has correct secret");
            LOG.error("4. Client has 'Service Accounts Enabled' = true");
            LOG.error("5. Client has proper roles (view-users, query-users)");
            throw new RuntimeException("Failed to initialize Keycloak service", e);
        }
    }

    public Optional<UserRepresentation> getUserByEmail(String email) {
        String cacheKey = "user:email:" + email;
        
        try {
            LOG.debug("Searching for user with email: " + email);
            
            // Check cache first
            Optional<UserRepresentation> cached = cacheService.get(cacheKey, UserRepresentation.class);
            if (cached.isPresent()) {
                LOG.debug("Found user in cache for email: " + email);
                return cached;
            }

            // Query Keycloak
            LOG.debug("Querying Keycloak for user with email: " + email);
            List<UserRepresentation> users = usersResource.search(null, null, null, email, 0, 1);
            LOG.debug("Keycloak returned " + users.size() + " users for email: " + email);
            
            Optional<UserRepresentation> user = users.stream()
                    .filter(u -> email.equals(u.getEmail()))
                    .findFirst();

            // Cache result (including empty results to avoid repeated queries)
            if (user.isPresent()) {
                LOG.debug("Found matching user, caching result for email: " + email);
                cacheService.set(cacheKey, user.get(), 3600); // 1 hour cache
            } else {
                LOG.debug("No matching user found for email: " + email);
                // Cache the fact that no user was found to avoid repeated queries
                cacheService.set(cacheKey + ":notfound", "true", 300); // 5 minute cache for not found
            }

            return user;
        } catch (jakarta.ws.rs.ProcessingException e) {
            if (e.getCause() instanceof jakarta.ws.rs.NotFoundException) {
                LOG.error("Keycloak authentication failed. Check:");
                LOG.error("1. Keycloak server URL: " + keycloakBaseUrl);
                LOG.error("2. Realm exists: " + realm);
                LOG.error("3. Client credentials are correct");
                LOG.error("4. Client has 'Service Accounts Enabled'");
            }
            LOG.error("Error fetching user by email: " + email, e);
            throw new RuntimeException("Error fetching user by email", e);
        } catch (jakarta.ws.rs.ForbiddenException e) {
            LOG.error("PERMISSION DENIED: Service account '" + clientId + "' lacks required permissions");
            LOG.error("To fix this:");
            LOG.error("1. Go to Keycloak Admin Console: " + keycloakBaseUrl + "/admin/");
            LOG.error("2. Navigate to Clients → " + clientId + " → Service accounts roles");
            LOG.error("3. Assign roles from 'realm-management':");
            LOG.error("   - view-users");
            LOG.error("   - query-users");
            LOG.error("   - view-realm");
            throw new RuntimeException("Service account lacks permissions to search users. See logs for setup instructions.", e);
        } catch (Exception e) {
            LOG.error("Unexpected error fetching user by email: " + email, e);
            throw new RuntimeException("Error fetching user by email", e);
        }
    }

    public Optional<UserRepresentation> getUserById(String id) {
        String cacheKey = "user:id:" + id;
        
        try {
            // Check cache first
            Optional<UserRepresentation> cached = cacheService.get(cacheKey, UserRepresentation.class);
            if (cached.isPresent()) {
                return cached;
            }

            // Query Keycloak
            UserRepresentation user = usersResource.get(id).toRepresentation();
            
            // Cache result
            if (user != null) {
                cacheService.set(cacheKey, user, 3600); // 1 hour cache
                return Optional.of(user);
            }

            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Error fetching user by ID: " + id, e);
            return Optional.empty();
        }
    }

    public List<UserRepresentation> getUsersByIds(List<String> ids) {
        return ids.stream()
                .map(this::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<String> getUserIdsByEmails(List<String> emails) {
        return emails.stream()
                .map(this::getEmailToUuidMapping)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<EmailToUuidMapping> getEmailToUuidMappings(List<String> emails) {
        return emails.stream()
                .map(email -> {
                    Optional<String> uuid = getEmailToUuidMapping(email);
                    return uuid.map(u -> new EmailToUuidMapping(email, u)).orElse(null);
                })
                .filter(mapping -> mapping != null)
                .collect(Collectors.toList());
    }

    public Optional<String> getEmailToUuidMapping(String email) {
        return getUserByEmail(email).map(UserRepresentation::getId);
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }
}
