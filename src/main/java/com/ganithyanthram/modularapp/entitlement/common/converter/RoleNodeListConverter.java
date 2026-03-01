package com.ganithyanthram.modularapp.entitlement.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import org.jooq.JSONB;
import org.jooq.impl.AbstractConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * JOOQ converter for JSONB columns containing List<RoleNode>.
 * Converts between PostgreSQL JSONB and Java List<RoleNode>.
 */
public class RoleNodeListConverter extends AbstractConverter<JSONB, List<RoleNode>> {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<RoleNode>> TYPE_REF = new TypeReference<>() {};
    
    public RoleNodeListConverter() {
        super(JSONB.class, (Class<List<RoleNode>>) (Class<?>) List.class);
    }
    
    @Override
    public List<RoleNode> from(JSONB databaseObject) {
        if (databaseObject == null || databaseObject.data() == null || databaseObject.data().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return OBJECT_MAPPER.readValue(databaseObject.data(), TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize RoleNode list from JSONB", e);
        }
    }
    
    @Override
    public JSONB to(List<RoleNode> userObject) {
        if (userObject == null || userObject.isEmpty()) {
            return JSONB.valueOf("[]");
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(userObject);
            return JSONB.valueOf(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize RoleNode list to JSONB", e);
        }
    }
}
