psql -d postgres < db-setup.sql
psql -d postgres -U machtest < tables.sql
psql -d postgres -U machtest < data.sql
