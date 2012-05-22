(ns mongocrawl.core
  (:require [tentacles.users :as users])
  (:require [tentacles.repos :as repos])
  (:use [expectations])
  (:require [clojure.pprint :as pp]))

(defn reduced-user [u]
  (let [relevant-fields [:followers
                         :name
                         :location
                         :login
                         :public_repos]]
    (zipmap relevant-fields
            (map u relevant-fields))))

(def clojure (repos/specific-repo "clojure" "clojure"))

(def visited-people (ref #{}))
(def visited-repos (ref #{}))

(def clojure-collabs
  (map :login (repos/collaborators "clojure" "clojure")))

(defn repos-of-user [u]
  (map :name (repos/user-repos u)))

(defn users-of-repo [u r]
  (map :login (repos/collaborators u r)))

(defn update-users [users repos]
  (let [all-users (set (apply concat (vals repos)))
        new-names (remove users all-users)
        new-repos (map repos-of-user new-names)
        new-users (zipmap new-names new-repos)]
    (merge users new-users)))

(defn rekey [user repos]
  (for [repo repos] [user repo]))
(expect [[:a 1] [:a 2] [:a 3]]
        (rekey :a [1 2 3]))

(defn users-to-userrepos [users]
  (apply concat (map #(rekey % (get users %)) 
       (keys users))))
(expect [[:a 1] [:a 2] [:a 3] [:b 3] [:b 4] [:b 5]]
        (users-to-userrepos {:a [1 2 3] :b [3 4 5]}   ))

(defn update-repos [users repos]
  (let [all-user-repos (users-to-userrepos users)
        new-user-repos (remove repos all-user-repos)
        new-names (map (fn [[x y]] (users-of-repo x y)) new-user-repos)
        new-repos (zipmap new-user-repos new-names)]
    (merge repos new-repos)))

        
(defn step 
  ([users repos]
  [(update-users users repos) (update-repos users repos)])
  ([[users repos]] (step users repos)))

(pp/pprint (step (step {"sympy" ["sympy"]} 
                       {})))
