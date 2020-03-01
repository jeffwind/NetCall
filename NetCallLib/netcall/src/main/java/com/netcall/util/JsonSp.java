package com.netcall.util;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 不需要context的SharedPreferences，可以任意指定路径
 */
public class JsonSp implements SharedPreferences {

    private static final String FILE_SUFFIX = ".json";

    private static String sRootPath;

    private String path;
    private JSONObject json;
    private List<OnSharedPreferenceChangeListener> onSpChangedListeners = new ArrayList<>();

    public static void setRootPath(String rootPath) {
        sRootPath = rootPath;
    }

    public static JsonSp getSp(String name) {
        if (sRootPath == null) {
            throw new IllegalStateException("Please init first because root path is null!");
        }
        return getSp(sRootPath, name);
    }

    public static JsonSp getSp(String rootPath, String name) {
        if (TextUtils.isEmpty(rootPath) || TextUtils.isEmpty(name)) {
            throw new IllegalStateException("rootPath[" + rootPath + "] or name[" + name + "] is not legal!");
        }
        rootPath = rootPath.endsWith("/") ? rootPath : rootPath + "/";
        name = name.contains(".") ? name : name + FILE_SUFFIX;
        String path = rootPath + name;
        JsonSp jsonSp = new JsonSp(path);
        return jsonSp;
    }

    private JsonSp(String path) {
        this.path = path;
        String text = FileUtil.readFile(path);
        text = text == null ? "{}" : text;
        try {
            json = new JSONObject(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, ?> getAll() {
        Iterator<String> keys = json.keys();
        Map<String, Object> map = new HashMap<>();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = json.get(key);
                map.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public String getString(String key, String defValue) {
        try {
            return json.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> set = new HashSet<>();
        try {
            JSONArray jsonArr = json.getJSONArray(key);
            for (int i = 0; i < jsonArr.length(); i++) {
                set.add(jsonArr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return set;
    }

    @Override
    public int getInt(String key, int defValue) {
        try {
            return json.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        try {
            return json.getLong(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        try {
            return (float)json.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        try {
            return json.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return defValue;
    }

    @Override
    public boolean contains(String key) {
        return json.has(key);
    }

    @Override
    public Editor edit() {
        return new MEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        onSpChangedListeners.add(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        onSpChangedListeners.remove(listener);
    }
    
    private class MEditor implements Editor {

        private JSONObject backupJson;

        private MEditor() {
            backupJson = copyJsonFrom(json);
        }

        @Override
        public Editor putString(String key, String value) {
            return putObj(key, value);
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            JSONArray jsonArray = new JSONArray();
            Iterator<String> iter = values.iterator();
            while (iter.hasNext()) {
                jsonArray.put(iter.next());
            }
            putObj(key, jsonArray.toString());
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            return putObj(key, value);
        }

        @Override
        public Editor putLong(String key, long value) {
            return putObj(key, value);
        }

        @Override
        public Editor putFloat(String key, float value) {
            return putObj(key, (double) value);
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            return putObj(key, value);
        }

        @Override
        public Editor remove(String key) {
            backupJson.remove(key);
            invokeChangeListener(key);
            return this;
        }

        @Override
        public Editor clear() {
            Iterator<String> keyIter = json.keys();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                backupJson.remove(key);
                invokeChangeListener(key);
            }
            return this;
        }

        @Override
        public boolean commit() {
            String text = backupJson.toString();
            return FileUtil.writeFile(path, text);
        }

        @Override
        public void apply() {
            json = copyJsonFrom(backupJson);
            new Thread() {
                @Override
                public void run() {
                    commit();
                }
            }.start();
        }

        private JSONObject copyJsonFrom(JSONObject from) {

            JSONObject to;
            String text = from.toString();
            try {
                to = new JSONObject(text);
            } catch (JSONException e) {
                e.printStackTrace();
                to = from;
            }
            return to;
        }

        private <T extends Object> Editor putObj(String key, T value) {
            try {
                backupJson.put(key, value);
                invokeChangeListener(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        private void invokeChangeListener(String key) {

            for (OnSharedPreferenceChangeListener l : onSpChangedListeners) {
                l.onSharedPreferenceChanged(JsonSp.this, key);
            }
        }
    }  
}
