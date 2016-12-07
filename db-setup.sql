CREATE USER machtest WITH PASSWORD 'testdb';
CREATE DATABASE machtest_dev WITH OWNER machtest;
CREATE DATABASE machtest_test WITH OWNER machtest;
\c machtest_dev
CREATE EXTENSION "uuid-ossp";
\c machtest_test
CREATE EXTENSION "uuid-ossp";

