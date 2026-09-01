-- 3NF schema verify (H2 Console)
SHOW TABLES;

SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'ORDERS'
ORDER BY ordinal_position;

-- orders 表不應有 owner_name / ticker（3NF）
SELECT COUNT(*) AS violation_count
FROM information_schema.columns
WHERE table_name = 'ORDERS'
  AND column_name IN ('OWNER_NAME', 'TICKER', 'COMPANY_NAME');

-- 1→N→1 關聯抽樣
SELECT a.account_no, o.id AS order_id, s.ticker, o.side, o.quantity
FROM orders o
JOIN accounts a ON o.account_id = a.id
JOIN symbols s ON o.symbol_id = s.id
ORDER BY a.account_no, o.id;
