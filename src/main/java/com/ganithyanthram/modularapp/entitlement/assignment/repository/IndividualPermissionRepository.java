package com.ganithyanthram.modularapp.entitlement.assignment.repository;

import com.ganithyanthram.modularapp.db.jooq.tables.daos.IndividualPermissionDao;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.IndividualPermission;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ganithyanthram.modularapp.db.jooq.Tables.INDIVIDUAL_PERMISSION;

/**
 * Custom repository for IndividualPermission entity.
 * Wraps JOOQ-generated DAO with business-specific query methods.
 */
@Repository
public class IndividualPermissionRepository {
    
    private final DSLContext dsl;
    private final IndividualPermissionDao dao;
    
    public IndividualPermissionRepository(DSLContext dsl, Configuration jooqConfiguration) {
        this.dsl = dsl;
        this.dao = new IndividualPermissionDao(jooqConfiguration);
    }
    
    /**
     * Create a new individual permission override
     */
    public UUID create(IndividualPermission individualPermission) {
        dao.insert(individualPermission);
        return individualPermission.getId();
    }
    
    /**
     * Find permission override by ID
     */
    public Optional<IndividualPermission> findById(UUID id) {
        return dsl.selectFrom(INDIVIDUAL_PERMISSION)
                .where(INDIVIDUAL_PERMISSION.ID.eq(id))
                .fetchOptionalInto(IndividualPermission.class);
    }
    
    /**
     * Find all permission overrides for an individual
     */
    public List<IndividualPermission> findByIndividualId(UUID individualId) {
        return dsl.selectFrom(INDIVIDUAL_PERMISSION)
                .where(INDIVIDUAL_PERMISSION.INDIVIDUAL_ID.eq(individualId))
                .fetchInto(IndividualPermission.class);
    }
    
    /**
     * Find permission overrides for an individual in a specific organisation
     */
    public Optional<IndividualPermission> findByIndividualIdAndOrgId(UUID individualId, UUID orgId) {
        return dsl.selectFrom(INDIVIDUAL_PERMISSION)
                .where(INDIVIDUAL_PERMISSION.INDIVIDUAL_ID.eq(individualId))
                .and(INDIVIDUAL_PERMISSION.ORG_ID.eq(orgId))
                .fetchOptionalInto(IndividualPermission.class);
    }
    
    /**
     * Check if permission override exists for individual in organisation
     */
    public boolean existsByIndividualIdAndOrgId(UUID individualId, UUID orgId) {
        return dsl.fetchExists(
                dsl.selectFrom(INDIVIDUAL_PERMISSION)
                        .where(INDIVIDUAL_PERMISSION.INDIVIDUAL_ID.eq(individualId))
                        .and(INDIVIDUAL_PERMISSION.ORG_ID.eq(orgId))
        );
    }
    
    /**
     * Update permission override
     */
    public void update(IndividualPermission individualPermission) {
        dao.update(individualPermission);
    }
    
    /**
     * Delete permission override by ID
     */
    public void deleteById(UUID id) {
        dsl.deleteFrom(INDIVIDUAL_PERMISSION)
                .where(INDIVIDUAL_PERMISSION.ID.eq(id))
                .execute();
    }
    
    /**
     * Delete all permission overrides for an individual
     */
    public void deleteByIndividualId(UUID individualId) {
        dsl.deleteFrom(INDIVIDUAL_PERMISSION)
                .where(INDIVIDUAL_PERMISSION.INDIVIDUAL_ID.eq(individualId))
                .execute();
    }
    
    /**
     * Delete permission override for individual in organisation
     */
    public void deleteByIndividualIdAndOrgId(UUID individualId, UUID orgId) {
        dsl.deleteFrom(INDIVIDUAL_PERMISSION)
                .where(INDIVIDUAL_PERMISSION.INDIVIDUAL_ID.eq(individualId))
                .and(INDIVIDUAL_PERMISSION.ORG_ID.eq(orgId))
                .execute();
    }
    
    /**
     * Count permission overrides for an individual
     */
    public long countByIndividualId(UUID individualId) {
        return dsl.selectCount()
                .from(INDIVIDUAL_PERMISSION)
                .where(INDIVIDUAL_PERMISSION.INDIVIDUAL_ID.eq(individualId))
                .fetchOne(0, Long.class);
    }
}
