package com.ganithyanthram.modularapp.entitlement.individual.repository;

import com.ganithyanthram.modularapp.db.jooq.tables.daos.IndividualDao;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Individual;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ganithyanthram.modularapp.db.jooq.Tables.INDIVIDUAL;

/**
 * Custom repository for Individual entity.
 * Wraps JOOQ-generated DAO with business-specific query methods.
 */
@Repository
public class IndividualRepository {
    
    private final DSLContext dsl;
    private final IndividualDao dao;
    
    public IndividualRepository(DSLContext dsl, Configuration jooqConfiguration) {
        this.dsl = dsl;
        this.dao = new IndividualDao(jooqConfiguration);
    }
    
    /**
     * Create a new individual
     */
    public UUID create(Individual individual) {
        dao.insert(individual);
        return individual.getId();
    }
    
    /**
     * Find individual by ID (active only)
     */
    public Optional<Individual> findByIdAndIsActiveTrue(UUID id) {
        return dsl.selectFrom(INDIVIDUAL)
                .where(INDIVIDUAL.ID.eq(id))
                .and(INDIVIDUAL.IS_ACTIVE.isTrue())
                .fetchOptionalInto(Individual.class);
    }
    
    /**
     * Find individual by ID (including inactive)
     */
    public Optional<Individual> findById(UUID id) {
        return dsl.selectFrom(INDIVIDUAL)
                .where(INDIVIDUAL.ID.eq(id))
                .fetchOptionalInto(Individual.class);
    }
    
    /**
     * Find individual by email
     */
    public Optional<Individual> findByEmail(String email) {
        return dsl.selectFrom(INDIVIDUAL)
                .where(INDIVIDUAL.EMAIL.eq(email))
                .and(INDIVIDUAL.IS_ACTIVE.isTrue())
                .fetchOptionalInto(Individual.class);
    }
    
    /**
     * Find all active individuals with pagination
     */
    public List<Individual> findByIsActiveTrue(int offset, int limit) {
        return dsl.selectFrom(INDIVIDUAL)
                .where(INDIVIDUAL.IS_ACTIVE.isTrue())
                .orderBy(INDIVIDUAL.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Individual.class);
    }
    
    /**
     * Search individuals by name or email (case-insensitive, active only)
     */
    public List<Individual> searchByNameOrEmail(String searchTerm, int offset, int limit) {
        return dsl.selectFrom(INDIVIDUAL)
                .where(INDIVIDUAL.NAME.containsIgnoreCase(searchTerm)
                        .or(INDIVIDUAL.EMAIL.containsIgnoreCase(searchTerm)))
                .and(INDIVIDUAL.IS_ACTIVE.isTrue())
                .orderBy(INDIVIDUAL.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Individual.class);
    }
    
    /**
     * Check if individual with email exists (active only)
     */
    public boolean existsByEmailAndIsActiveTrue(String email) {
        return dsl.fetchExists(
                dsl.selectFrom(INDIVIDUAL)
                        .where(INDIVIDUAL.EMAIL.eq(email))
                        .and(INDIVIDUAL.IS_ACTIVE.isTrue())
        );
    }
    
    /**
     * Update individual
     */
    public void update(Individual individual) {
        dao.update(individual);
    }
    
    /**
     * Soft delete individual (set is_active = false)
     */
    public void softDelete(UUID id) {
        dsl.update(INDIVIDUAL)
                .set(INDIVIDUAL.IS_ACTIVE, false)
                .where(INDIVIDUAL.ID.eq(id))
                .execute();
    }
    
    /**
     * Activate individual
     */
    public void activate(UUID id) {
        dsl.update(INDIVIDUAL)
                .set(INDIVIDUAL.IS_ACTIVE, true)
                .where(INDIVIDUAL.ID.eq(id))
                .execute();
    }
    
    /**
     * Deactivate individual
     */
    public void deactivate(UUID id) {
        dsl.update(INDIVIDUAL)
                .set(INDIVIDUAL.IS_ACTIVE, false)
                .where(INDIVIDUAL.ID.eq(id))
                .execute();
    }
    
    /**
     * Count active individuals
     */
    public long countActive() {
        return dsl.selectCount()
                .from(INDIVIDUAL)
                .where(INDIVIDUAL.IS_ACTIVE.isTrue())
                .fetchOne(0, Long.class);
    }
}
