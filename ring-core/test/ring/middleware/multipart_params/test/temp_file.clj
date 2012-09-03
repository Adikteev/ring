(ns ring.middleware.multipart-params.test.temp-file
  (:use clojure.test
        [ring.util.io :only (string-input-stream)]
        ring.middleware.multipart-params.temp-file))

(deftest test-temp-file-store
  (let [store  (temp-file-store)
        result (store
                {:filename "foo.txt"
                 :content-type "text/plain"
                 :stream (string-input-stream "foo")})]
    (is (= (:filename result) "foo.txt"))
    (is (= (:content-type result) "text/plain"))
    (is (= (:size result) 3))
    (is (instance? java.io.File (:tempfile result)))
    (is (.exists (:tempfile result)))
    (is (= (slurp (:tempfile result)) "foo"))))

(deftest test-temp-file-expiry
  (let [store  (temp-file-store {:expires-in 2})
        result (store
                {:filename "foo.txt"
                 :content-type "text/plain"
                 :stream (string-input-stream "foo")})]
    (is (.exists (:tempfile result)))
    (Thread/sleep 3000)
    (is (not (.exists (:tempfile result))))))

(defn all-threads []
  (.keySet (Thread/getAllStackTraces)))

(deftest test-temp-file-threads
  (let [threads0 (all-threads)
        store    (temp-file-store)
        threads1 (all-threads)]
    (is (= (count threads0)
           (count threads1)))
    (dotimes [_ 200]
      (store {:filename "foo.txt"
              :content-type "text/plain"
              :stream (string-input-stream "foo")}))
    (is (< (count (all-threads))
           100))))
