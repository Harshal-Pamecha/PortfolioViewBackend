package com.portfolio.constants;

import lombok.NoArgsConstructor;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class DatabaseConstants {
    
    /** Database table names. */
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_FAMILY_MEMBERS = "family_members";
    public static final String TABLE_HOLDINGS = "holdings";
    public static final String TABLE_LOANS = "loans";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_PORTFOLIO_SNAPSHOTS = "portfolio_snapshots";
    public static final String TABLE_REFRESH_TOKENS = "refresh_tokens";
    
    /** Database column names. */
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FAMILY_MEMBER_ID = "family_member_id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_SOURCE_ACCOUNT_ID = "source_account_id";
    public static final String COLUMN_DESTINATION_ACCOUNT_ID = "destination_account_id";
    public static final String COLUMN_HOLDING_ID = "holding_id";
    
    /** Database column type definitions. */
    public static final String COLUMN_DEF_TEXT = "TEXT";
}
