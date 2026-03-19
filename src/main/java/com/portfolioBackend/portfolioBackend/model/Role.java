package com.portfolioBackend.portfolioBackend.model;

/**
 * User roles for authorization.
 * SUPER_ADMIN has highest privileges, then ADMIN, then USER.
 */
public enum Role {
    USER,
    ADMIN,
    SUPER_ADMIN
}
