package com.centerm.epos.xml.bean;


import java.util.HashMap;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/9</br>
 */

public class PreferDataPool {

    private Map<String, Integer> intMap;
    private Map<String, String> strMap;
    private Map<String, Boolean> boolMap;
    private Map<String, Double> doubleMap;
    private Map<String, Long> longMap;

    public void put(String key, int value) {
        if (intMap == null) {
            intMap = new HashMap<>();
        }
        intMap.put(key, value);
    }

    public void put(String key, String value) {
        if (strMap == null) {
            strMap = new HashMap<>();
        }
        strMap.put(key, value);
    }

    public void put(String key, boolean value) {
        if (boolMap == null) {
            boolMap = new HashMap<>();
        }
        boolMap.put(key, value);
    }

    public void put(String key, double value) {
        if (doubleMap == null) {
            doubleMap = new HashMap<>();
        }
        doubleMap.put(key, value);
    }

    public void put(String key, long value) {
        if (longMap == null) {
            longMap = new HashMap<>();
        }
        longMap.put(key, value);
    }

    public int getInt(String key, int defValue) {
        if (intMap == null) {
            return defValue;
        }
        Integer value = intMap.get(key);
        return value == null ? defValue : value;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public String getString(String key) {
        if (strMap == null) {
            return null;
        }
        return strMap.get(key);
    }

    public boolean getBoolean(String key, boolean defValue) {
        if (boolMap == null) {
            return defValue;
        }
        Boolean b = boolMap.get(key);
        return b == null ? defValue : b;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public double getDouble(String key, double defValue) {
        if (doubleMap == null) {
            return defValue;
        }
        Double value = doubleMap.get(key);
        return value == null ? defValue : value;
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public long getLong(String key, long defValue) {
        if (longMap == null) {
            return defValue;
        }
        Long value = longMap.get(key);
        return value == null ? defValue : value;
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public boolean exist(String key, Class type) {
        if (Integer.class.equals(type)) {
            if (intMap != null) {
                return intMap.containsKey(key);
            }
        } else if (String.class.equals(type)) {
            if (strMap != null) {
                return strMap.containsKey(key);
            }
        } else if (Boolean.class.equals(type)) {
            if (boolMap != null) {
                return boolMap.containsKey(key);
            }
        } else if (Long.class.equals(type)) {
            if (longMap != null) {
                return longMap.containsKey(key);
            }
        } else if (Double.class.equals(type)) {
            if (doubleMap != null) {
                return doubleMap.containsKey(key);
            }
        }
        return false;
    }

    public void putStrings(Map<String, String> map) {
        if (strMap == null) {
            strMap = new HashMap<>();
        }
        strMap.putAll(map);
    }

    public void putBooleans(Map<String, Boolean> map) {
        if (boolMap == null) {
            boolMap = new HashMap<>();
        }
        boolMap.putAll(map);
    }

    public void putDoubles(Map<String, Double> map) {
        if (doubleMap == null) {
            doubleMap = new HashMap<>();
        }
        doubleMap.putAll(map);
    }

    public void putLongs(Map<String, Long> map) {
        if (longMap == null) {
            longMap = new HashMap<>();
        }
        longMap.putAll(map);
    }

    public void putInts(Map<String, Integer> map) {
        if (intMap == null) {
            intMap = new HashMap<>();
        }
        intMap.putAll(map);
    }

    public void putAll(PreferDataPool pool) {
        if (pool == null) {
            return;
        }
        if (intMap == null) {
            intMap = new HashMap<>();
        }
        if (pool.intMap != null)
            intMap.putAll(pool.intMap);
        if (longMap == null) {
            longMap = new HashMap<>();
        }
        if (pool.longMap != null)
            longMap.putAll(pool.longMap);
        if (doubleMap == null) {
            doubleMap = new HashMap<>();
        }
        if (pool.doubleMap != null)
            doubleMap.putAll(pool.doubleMap);
        if (boolMap == null) {
            boolMap = new HashMap<>();
        }
        if (pool.boolMap != null)
            boolMap.putAll(pool.boolMap);
        if (strMap == null) {
            strMap = new HashMap<>();
        }
        if (pool.strMap != null)
            strMap.putAll(pool.strMap);
    }


    @Override
    public String toString() {
        return "PreferDataPool{" +
                "intMap=" + intMap +
                ", strMap=" + strMap +
                ", boolMap=" + boolMap +
                ", doubleMap=" + doubleMap +
                ", longMap=" + longMap +
                '}';
    }
}
