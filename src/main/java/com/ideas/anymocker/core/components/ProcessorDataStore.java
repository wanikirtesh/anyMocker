package com.ideas.anymocker.core.components;

import java.util.HashMap;
import java.util.Map;

public class ProcessorDataStore {
    private static final Map<String,Object> store = new HashMap<>();
    public static void putObject(String key,Object o){
        store.put(key,o);
    }

    public static Object getDataObject(String key){
        return store.get(key);
    }
}
