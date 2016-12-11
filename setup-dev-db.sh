psql -d postgres < db-setup.sql
psql -d machtest_dev -U machtest < tables.sql
psql -d machtest_dev -U machtest < data.sql
