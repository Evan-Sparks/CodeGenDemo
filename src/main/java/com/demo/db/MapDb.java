package com.demo.db;

import java.util.HashMap;
import java.util.Map;

public class MapDb {
    private final Map<Class, Map<String, Object>> dbs = new HashMap<>();

    public Map<String, Object> getDb(Class type) {
        Map<String, Object> db = dbs.get(type);
        if (db == null) {
            db = new HashMap<>();
            dbs.put(type, db);
        }
        return db;
    }
}
