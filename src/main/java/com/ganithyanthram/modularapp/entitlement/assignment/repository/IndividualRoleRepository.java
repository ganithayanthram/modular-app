package com.ganithyanthram.modularapp.entitlement.assignment.repository;

import com.ganithyanthram.modularapp.db.jooq.tables.daos.IndividualRoleDao;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.IndividualRole;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ganithyanthram.modularapp.db.jooq.Tables.INDIVIDUAL_ROLE;

/**
 * Custom repository for IndividualRole entity.
 * Wraps JOOQ-generated DAO with business-specific query methods.
 */
@Repository
public class IndividualRoleRepository {
    
    private final DSLContext dsl;
    private final IndividualRoleDao dao;
    
    public IndividualRoleRepository(DSLContext dsl, Configuration jooqConfiguration) {
        this.dsl = dsl;
        this.dao = new IndividualRoleDao(jooqConfiguration);
    }
    
    /**
     * Create a new individual-role assignment
     */
    public UUID create(IndividualRole individualRole) {
        dao.insert(individualRole);
        return individualRole.getId();
    }
    
    /**
     * Find assignment by ID
     */
    public Optional<IndividualRole> findById(UUID id) {
        return dsl.selectFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.ID.eq(id))
                .fetchOptionalInto(IndividualRole.class);
    }
    
    /**
     * Find all roles assigned to an individual
     */
    public List<IndividualRole> findByIndividualId(UUID individualId) {
        return dsl.selectFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.INDIVIDUAL_ID.eq(individualId))
                .fetchInto(IndividualRole.class);
    }
    
    /**
     * Find all roles assigned to an individual in a specific organisation
     */
    public List<IndividualRole> findByIndividualIdAndOrgId(UUID individualId, UUID orgId) {
        return dsl.selectFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.INDIVIDUAL_ID.eq(individualId))
                .and(INDIVIDUAL_ROLE.ORG_ID.eq(orgId))
                .fetchInto(IndividualRole.class);
    }
    
    /**
     * Find all individuals with a specific role
     */
    public List<IndividualRole> findByRoleId(UUID roleId) {
        return dsl.selectFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.ROLE_ID.eq(roleId))
                .fetchInto(IndividualRole.class);
    }
    
    /**
     * Find specific assignment
     */
    public Optional<IndividualRole> findByIndividualIdAndRoleId(UUID individualId, UUID roleId) {
        return dsl.selectFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.INDIVIDUAL_ID.eq(individualId))
                .and(INDIVIDUAL_ROLE.ROLE_ID.eq(roleId))
                .fetchOptionalInto(IndividualRole.class);
    }
    
    /**
     * Check if role is assigned to individual
     */
    public boolean existsByIndividualIdAndRoleId(UUID individualId, UUID roleId) {
        return dsl.fetchExists(
                dsl.selectFrom(INDIVIDUAL_ROLE)
                        .where(INDIVIDUAL_ROLE.INDIVIDUAL_ID.eq(individualId))
                        .and(INDIVIDUAL_ROLE.ROLE_ID.eq(roleId))
        );
    }
    
    /**
     * Delete assignment by individual and role
     */
    public void deleteByIndividualIdAndRoleId(UUID individualId, UUID roleId) {
        dsl.deleteFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.INDIVIDUAL_ID.eq(individualId))
                .and(INDIVIDUAL_ROLE.ROLE_ID.eq(roleId))
                .execute();
    }
    
    /**
     * Delete all assignments for an individual
     */
    public void deleteByIndividualId(UUID individualId) {
        dsl.deleteFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.INDIVIDUAL_ID.eq(individualId))
                .execute();
    }
    
    /**
     * Delete all assignments for a role
     */
    public void deleteByRoleId(UUID roleId) {
        dsl.deleteFrom(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.ROLE_ID.eq(roleId))
                .execute();
    }
    
    /**
     * Count role assignments for an individual
     */
    public long countByIndividualId(UUID individualId) {
        return dsl.selectCount()
                .from(INDIVIDUAL_ROLE)
                .where(INDIVIDUAL_ROLE.INDIVIDUAL_ID.eq(individualId))
                .fetchOne(0, Long.class);
    }
}
