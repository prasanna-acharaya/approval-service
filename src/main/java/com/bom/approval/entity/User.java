package com.bom.approval.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String userName;
    private String role;
    private String email;
    private String reportsTo;
    private Map<String, Object> customFields;
    private Set<String> approvedDsaIds = new HashSet<>();
}
