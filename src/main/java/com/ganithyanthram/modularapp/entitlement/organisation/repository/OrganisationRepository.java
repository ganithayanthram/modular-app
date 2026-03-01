package com.ganithyanthram.modularapp.entitlement.organisation.repository;

import com.ganithyanthram.modularapp.db.jooq.tables.daos.OrganisationDao;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Organisation;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ganithyanthram.modularapp.db.jooq.Tables.ORGANISATION;

/**
 * Custom repository for Organisation entity.
 * Wraps JOOQ-generated DAO with business-specific query methods.
 */
@Repository
public class OrganisationRepository {
    
    private final DSLContext dsl;
    private final OrganisationDao dao;
    
    public OrganisationRepository(DSLContext dsl, Configuration jooqConfiguration) {
        this.dsl = dsl;
        this.dao = new OrganisationDao(jooqConfiguration);
    }
    
    /**
     * Create a new organisation
     */
    public UUID create(Organisation organisation) {
        dao.insert(organisation);
        return organisation.getId();
    }
    
    /**
     * Find organisation by ID (active only)
     */
    public Optional<Organisation> findByIdAndIsActiveTrue(UUID id) {
        return dsl.selectFrom(ORGANISATION)
                .where(ORGANISATION.ID.eq(id))
                .and(ORGANISATION.IS_ACTIVE.isTrue())
                .fetchOptionalInto(Organisation.class);
    }
    
    /**
     * Find organisation by ID (including inactive)
     */
    public Optional<Organisation> findById(UUID id) {
        return dsl.selectFrom(ORGANISATION)
                .where(ORGANISATION.ID.eq(id))
                .fetchOptionalInto(Organisation.class);
    }
    
    /**
     * Find all active organisations with pagination
     */
    public List<Organisation> findByIsActiveTrue(int offset, int limit) {
        return dsl.selectFrom(ORGANISATION)
                .where(ORGANISATION.IS_ACTIVE.isTrue())
                .orderBy(ORGANISATION.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Organisation.class);
    }
    
    /**
     * Search organisations by name (case-insensitive, active only)
     */
    public List<Organisation> searchByName(String name, int offset, int limit) {
        return dsl.selectFrom(ORGANISATION)
                .where(ORGANISATION.NAME.containsIgnoreCase(name))
                .and(ORGANISATION.IS_ACTIVE.isTrue())
                .orderBy(ORGANISATION.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Organisation.class);
    }
    
    /**
     * Check if organisation with name exists (active only)
     */
    public boolean existsByNameAndIsActiveTrue(String name) {
        return dsl.fetchExists(
                dsl.selectFrom(ORGANISATION)
                        .where(ORGANISATION.NAME.eq(name))
                        .and(ORGANISATION.IS_ACTIVE.isTrue())
        );
    }
    
    /**
     * Update organisation
     */
    public void update(Organisation organisation) {
        dao.update(organisation);
    }
    
    /**
     * Soft delete organisation (set is_active = false)
     */
    public void softDelete(UUID id) {
        dsl.update(ORGANISATION)
                .set(ORGANISATION.IS_ACTIVE, false)
                .where(ORGANISATION.ID.eq(id))
                .execute();
    }
    
    /**
     * Activate organisation
     */
    public void activate(UUID id) {
        dsl.update(ORGANISATION)
                .set(ORGANISATION.IS_ACTIVE, true)
                .where(ORGANISATION.ID.eq(id))
                .execute();
    }
    
    /**
     * Deactivate organisation
     */
    public void deactivate(UUID id) {
        dsl.update(ORGANISATION)
                .set(ORGANISATION.IS_ACTIVE, false)
                .where(ORGANISATION.ID.eq(id))
                .execute();
    }
    
    /**
     * Count active organisations
     */
    public long countActive() {
        return dsl.selectCount()
                .from(ORGANISATION)
                .where(ORGANISATION.IS_ACTIVE.isTrue())
                .fetchOne(0, Long.class);
    }
}
