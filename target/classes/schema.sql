-- Performance Indexes generated from DB Audit
CREATE INDEX IF NOT EXISTS idx_holdings_user_id ON holdings (user_id);
CREATE INDEX IF NOT EXISTS idx_holdings_account_id ON holdings (account_id);
CREATE INDEX IF NOT EXISTS idx_holdings_ticker_name ON holdings (ticker_symbol, name);
CREATE INDEX IF NOT EXISTS idx_holdings_fifo_sell ON holdings (account_id, ticker_symbol, purchase_date);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_holding_id ON transactions (holding_id);
CREATE INDEX IF NOT EXISTS idx_transactions_accounts ON transactions (source_account_id, destination_account_id);

CREATE INDEX IF NOT EXISTS idx_accounts_family_user ON accounts (family_member_id, user_id);
CREATE INDEX IF NOT EXISTS idx_family_members_user ON family_members (user_id);

CREATE INDEX IF NOT EXISTS idx_portfolio_snapshots_user_date ON portfolio_snapshots (user_id, date);

CREATE INDEX IF NOT EXISTS idx_tickers_symbol ON tickers (ticker);

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_tickers_search_trgm ON tickers USING GIN (ticker gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_tickers_company_trgm ON tickers USING GIN (company_name gin_trgm_ops);
