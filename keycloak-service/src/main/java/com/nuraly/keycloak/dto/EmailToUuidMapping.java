package com.nuraly.keycloak.dto;

public class EmailToUuidMapping {
    private String email;
    private String uuid;

    public EmailToUuidMapping() {
    }

    public EmailToUuidMapping(String email, String uuid) {
        this.email = email;
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "EmailToUuidMapping{" +
                "email='" + email + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
