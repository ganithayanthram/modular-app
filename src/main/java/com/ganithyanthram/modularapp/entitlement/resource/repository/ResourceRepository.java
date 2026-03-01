package com.ganithyanthram.modularapp.entitlement.resource.repository;

import com.ganithyanthram.modularapp.db.jooq.tables.daos.ResourceDao;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Resource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ganithyanthram.modularapp.db.jooq.Tables.RESOURCE;

/**
 * Custom repository for Resource entity.
 * Wraps JOOQ-generated DAO with business-specific query methods.
 */
@Repository
public class ResourceRepository {
    
    private final DSLContext dsl;
    private final ResourceDao dao;
    
    public ResourceRepository(DSLContext dsl, Configuration jooqConfiguration) {
        this.dsl = dsl;
        this.dao = new ResourceDao(jooqConfiguration);
    }
    
    /**
     * Create a new resource
     */
    public UUID create(Resource resource) {
        dao.insert(resource);
        return resource.getId();
    }
    
    /**
     * Find resource by ID (active only)
     */
    public Optional<Resource> findByIdAndIsActiveTrue(UUID id) {
        return dsl.selectFrom(RESOURCE)
                .where(RESOURCE.ID.eq(id))
                .and(RESOURCE.IS_ACTIVE.isTrue())
                .fetchOptionalInto(Resource.class);
    }
    
    /**
     * Find resource by ID (including inactive)
     */
    public Optional<Resource> findById(UUID id) {
        return dsl.selectFrom(RESOURCE)
                .where(RESOURCE.ID.eq(id))
                .fetchOptionalInto(Resource.class);
    }
    
    /**
     * Find all active resources with pagination
     */
    public List<Resource> findByIsActiveTrue(int offset, int limit) {
        return dsl.selectFrom(RESOURCE)
                .where(RESOURCE.IS_ACTIVE.isTrue())
                .orderBy(RESOURCE.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Resource.class);
    }
    
    /**
     * Find resources by type (active only)
     */
    public List<Resource> findByTypeAndIsActiveTrue(String type, int offset, int limit) {
        return dsl.selectFrom(RESOURCE)
                .where(RESOURCE.TYPE.eq(type))
                .and(RESOURCE.IS_ACTIVE.isTrue())
                .orderBy(RESOURCE.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Resource.class);
    }
    
    /**
     * Find child resources by parent resource ID
     */
    public List<Resource> findByParentResourceId(UUID parentResourceId) {
        return dsl.selectFrom(RESOURCE)
                .where(RESOURCE.PARENT_RESOURCE_ID.eq(parentResourceId))
                .and(RESOURCE.IS_ACTIVE.isTrue())
                .fetchInto(Resource.class);
    }
    
    /**
     * Find root resources (no parent)
     */
    public List<Resource> findRootResources() {
        return dsl.selectFrom(RESOURCE)
                .where(RESOURCE.PARENT_RESOURCE_ID.isNull())
                .and(RESOURCE.IS_ACTIVE.isTrue())
                .orderBy(RESOURCE.CREATED_ON.desc())
                .fetchInto(Resource.class);
    }
    
    /**
     * Check if resource with name exists (active only)
     */
    public boolean existsByNameAndIsActiveTrue(String name) {
        return dsl.fetchExists(
                dsl.selectFrom(RESOURCE)
                        .where(RESOURCE.NAME.eq(name))
                        .and(RESOURCE.IS_ACTIVE.isTrue())
        );
    }
    
    /**
     * Update resource
     */
    public void update(Resource resource) {
        dao.update(resource);
    }
    
    /**
     * Soft delete resource (set is_active = false)
     */
    public void softDelete(UUID id) {
        dsl.update(RESOURCE)
                .set(RESOURCE.IS_ACTIVE, false)
                .where(RESOURCE.ID.eq(id))
                .execute();
    }
    
    /**
     * Count active resources
     */
    public long countActive() {
        return dsl.selectCount()
                .from(RESOURCE)
                .where(RESOURCE.IS_ACTIVE.isTrue())
                .fetchOne(0, Long.class);
    }
    
    /**
     * Count active resources by type
     */
    public long countByTypeAndIsActiveTrue(String type) {
        return dsl.selectCount()
                .from(RESOURCE)
                .where(RESOURCE.TYPE.eq(type))
                .and(RESOURCE.IS_ACTIVE.isTrue())
                .fetchOne(0, Long.class);
    }
}
