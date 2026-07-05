package com.portfolio.entity;

/**
 * Enumeration of account types.
 */
public enum AccountType {

    /** A standard bank account (Savings, Checking, etc.). */
    BANK,

    /** A mobile or digital wallet (e.g., Paytm, PayPal, etc.). */
    WALLET,

    /** A Demat account specifically for equity investments. */
    EQUITY_DEMAT,

    /** A Demat account specifically for debt investments. */
    DEBT_DEMAT,

    /** A wallet for holding cryptocurrency assets. */
    CRYPTO_WALLET,

    /** Any other type of account or uncategorized. */
    OTHER
}
