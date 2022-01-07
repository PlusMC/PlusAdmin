package com.playplusmc.plusadmin.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtherUtils {

    public static <T> List<T> getKeys(Map<T, T> map, T value ) {
        List<T> keys = new ArrayList<>();
        for (Map.Entry<T, T> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }
}
