(ns macchiato.test.sql
  (:require [clojure.test :refer [deftest testing are is async]]
            [macchiato.fs :refer [directory? slurp]]
            [macchiato.sql :as sql]
            [schema.core :as s]))



(deftest test-read-text-file
  (is (= "HELLO WORLD" (slurp "test/files/simple-text.txt"))))

(deftest is-sql?-test
  (are [sql? name] (= sql? (sql/sql-file? name))
                   true "test/files/query.SQL"
                   true "test/files/query.sql"
                   true "test/files/insert-name.sql"
                   false "test/files/simple-text.txt"
                   false "test/files/folder.sql"            ; It's actually a folder
                   false "test/sql"
                   false ""
                   false nil))


(deftest load-queries-test
  ;; Compare as set, we can't rely on any particular order
  (is (= #{{:name  "insert-name"
            :query "INSERT INTO names (name) values ($1);"}
           {:name  "query"
            :query "SELECT * FROM names;"}}
         (set (sql/load-queries "test/files"))))
  ;; The return value validates against the schema
  (is (s/validate sql/Queries (sql/load-queries "test/files")))
  ;; A folder with no valid queries returns an empty list
  (is (empty? (sql/load-queries "test/files/folder.sql"))))


(deftest list-sql-files-test
  ;; File names are returned with the path prepended
  (is (= ["test/files/insert-name.sql" "test/files/query.sql"]
         (sql/list-sql-files "test/files/")))
  ;; We don't need to prepend the separator to the path
  (is (= ["test/files/insert-name.sql" "test/files/query.sql"]
         (sql/list-sql-files "test/files")))
  ;; Non-sql files are ignored altogether, regardless of folder name
  (is (= [] (sql/list-sql-files "test/files/folder.sql"))))


(deftest make-query-map-test
  ;; Test validation
  (is (thrown? js/Error (sql/make-queries [{:name "hello" :query 1}])))
  (is (thrown? js/Error (sql/make-queries [{:name "hello"}])))
  (is (thrown? js/Error (sql/make-queries [{:query "select * from somewhere;"}])))
  ;; Create queries
  (let [query-map (sql/make-query-map (sql/load-queries "test/files"))
        fn-query  (:query query-map)
        fn-insert (:insert-name query-map)]
    ;; We get a map with the proper keys, and the values are functions
    (is (map? query-map))
    (is (fn? fn-query))
    (is (fn? fn-insert))
    ;; The first parameter of the function returned is the function that will
    ;; be invoked for the query, the second are arguments.
    ;;
    ;; We'll test this using the str function, since it allows us to play with
    ;; and skmanipulate the query string being wrapped without executing it.
    ;;
    ;; In normal use, we'd pass something that can actually run the query.
    (is (= "SELECT * FROM names;" (fn-query str)))
    (is (= "INSERT INTO names (name) values ($1);" (fn-insert str)))
    ;; Verify that we can pass parameters
    (is (= "SELECT * FROM names;Hello world"
           (fn-query str "Hello" " " "world")))
    (is (= "INSERT INTO names (name) values ($1);Robert');DROP TABLE Students;--"
           (fn-insert str "Robert');DROP TABLE Students;--")))
    ;; Verify that we can pass parameters to the query function, and the fn-query
    ;; gets them as a list
    ;;
    ;; Yes, this test looks dirty, but I want to make it clear that there is
    ;; no magic going on here, just function application with the query
    ;; string and parameters we got.
    (is (= "SELECT * FROM names;123" (fn-query str  1 2 3)))))

