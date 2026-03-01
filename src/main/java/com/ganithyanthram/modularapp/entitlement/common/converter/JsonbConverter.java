package com.ganithyanthram.modularapp.entitlement.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSONB;
import org.jooq.impl.AbstractConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * JOOQ converter for generic JSONB columns containing Map<String, Object>.
 * Converts between PostgreSQL JSONB and Java Map.
 * Used for meta_data, validations, documents columns.
 */
public class JsonbConverter extends AbstractConverter<JSONB, Map<String, Object>> {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> TYPE_REF = new TypeReference<>() {};
    
    public JsonbConverter() {
        super(JSONB.class, (Class<Map<String, Object>>) (Class<?>) Map.class);
    }
    
    @Override
    public Map<String, Object> from(JSONB databaseObject) {
        if (databaseObject == null || databaseObject.data() == null || databaseObject.data().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return OBJECT_MAPPER.readValue(databaseObject.data(), TYPE_REF);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize Map from JSONB", e);
        }
    }
    
    @Override
    public JSONB to(Map<String, Object> userObject) {
        if (userObject == null || userObject.isEmpty()) {
            return JSONB.valueOf("{}");
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(userObject);
            return JSONB.valueOf(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Map to JSONB", e);
        }
    }
}
