(ns gossipy
  (:require [clojure.java.io :as io])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]
           [com.cognitect.transit TransitFactory TransitFactory$Format Reader Writer]))

;;https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
(defn tile [lat lon zoom]
  (let [zoom-shifted (bit-shift-left 1 zoom)
        lat-radians (Math/toRadians lat)
        xtile (int (Math/floor (* (/ (+ 180 lon) 360) zoom-shifted)))
        ytile (int (Math/floor (* (/ (- 1
                                        (/
                                          (Math/log (+ (Math/tan lat-radians)
                                                       (/ 1 (Math/cos lat-radians))))
                                          Math/PI))
                                     2)
                                  zoom-shifted)))]
    (str zoom
         "/"
         (cond (< xtile 0) 0
               (>= xtile zoom-shifted) (- zoom-shifted 1)
               :else xtile)
         "/"
         (cond (< ytile 0) 0
               (>= ytile zoom-shifted) (- zoom-shifted 1)
               :else ytile))))

(tile 15.8798452 108.3735817 7)

(def out (io/output-stream "test.json"))
(def writer (TransitFactory/writer TransitFactory$Format/JSON_VERBOSE out))
(.. writer (write [1 2 3]))

(.. writer (write {"first-name" "sonny" "locations" [ [1 2] [3 4]]}))




