package com.ganithyanthram.modularapp.entitlement.role.repository;

import com.ganithyanthram.modularapp.db.jooq.tables.daos.RolesDao;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Roles;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ganithyanthram.modularapp.db.jooq.Tables.ROLES;

/**
 * Custom repository for Roles entity.
 * Wraps JOOQ-generated DAO with business-specific query methods.
 */
@Repository
public class RoleRepository {
    
    private final DSLContext dsl;
    private final RolesDao dao;
    
    public RoleRepository(DSLContext dsl, Configuration jooqConfiguration) {
        this.dsl = dsl;
        this.dao = new RolesDao(jooqConfiguration);
    }
    
    /**
     * Create a new role
     */
    public UUID create(Roles role) {
        dao.insert(role);
        return role.getId();
    }
    
    /**
     * Find role by ID (active only)
     */
    public Optional<Roles> findByIdAndIsActiveTrue(UUID id) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.ID.eq(id))
                .and(ROLES.IS_ACTIVE.isTrue())
                .fetchOptionalInto(Roles.class);
    }
    
    /**
     * Find role by ID (including inactive)
     */
    public Optional<Roles> findById(UUID id) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.ID.eq(id))
                .fetchOptionalInto(Roles.class);
    }
    
    /**
     * Find all roles by organisation (active only)
     */
    public List<Roles> findByOrgIdAndIsActiveTrue(UUID orgId, int offset, int limit) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.ORG_ID.eq(orgId))
                .and(ROLES.IS_ACTIVE.isTrue())
                .orderBy(ROLES.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Roles.class);
    }
    
    /**
     * Find all active roles with pagination
     */
    public List<Roles> findByIsActiveTrue(int offset, int limit) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.IS_ACTIVE.isTrue())
                .orderBy(ROLES.CREATED_ON.desc())
                .offset(offset)
                .limit(limit)
                .fetchInto(Roles.class);
    }
    
    /**
     * Find child roles by parent role ID
     */
    public List<Roles> findByParentRoleId(UUID parentRoleId) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.PARENT_ROLE_ID.eq(parentRoleId))
                .and(ROLES.IS_ACTIVE.isTrue())
                .fetchInto(Roles.class);
    }
    
    /**
     * Check if role with name exists in organisation (active only)
     */
    public boolean existsByNameAndOrgIdAndIsActiveTrue(String name, UUID orgId) {
        return dsl.fetchExists(
                dsl.selectFrom(ROLES)
                        .where(ROLES.NAME.eq(name))
                        .and(ROLES.ORG_ID.eq(orgId))
                        .and(ROLES.IS_ACTIVE.isTrue())
        );
    }
    
    /**
     * Update role
     */
    public void update(Roles role) {
        dao.update(role);
    }
    
    /**
     * Soft delete role (set is_active = false)
     */
    public void softDelete(UUID id) {
        dsl.update(ROLES)
                .set(ROLES.IS_ACTIVE, false)
                .where(ROLES.ID.eq(id))
                .execute();
    }
    
    /**
     * Activate role
     */
    public void activate(UUID id) {
        dsl.update(ROLES)
                .set(ROLES.IS_ACTIVE, true)
                .where(ROLES.ID.eq(id))
                .execute();
    }
    
    /**
     * Count active roles by organisation
     */
    public long countByOrgIdAndIsActiveTrue(UUID orgId) {
        return dsl.selectCount()
                .from(ROLES)
                .where(ROLES.ORG_ID.eq(orgId))
                .and(ROLES.IS_ACTIVE.isTrue())
                .fetchOne(0, Long.class);
    }
}
