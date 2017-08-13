package com.by_syk.bigjpg.bean;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.by_syk.bigjpg.util.C;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by By_syk on 2017-08-02.
 */

public class QueryBean {
    private Map<String, Item> items = new HashMap<>();

    public QueryBean(JsonObject joQuery) {
        parse(joQuery);
    }

    public boolean isDone(@NonNull String id) {
        Item item = items.get(id);
        return item != null && "success".equals(item.status);
    }

    public boolean isProcessing(@NonNull String id) {
        Item item = items.get(id);
        return item != null && "process".equals(item.status);
    }

    public boolean isWaiting(@NonNull String id) {
        Item item = items.get(id);
        return item != null && "new".equals(item.status);
    }

    @Nullable
    public String getUrl(@NonNull String id) {
        Item item = items.get(id);
        if (item != null) {
            return item.url;
        }
        return null;
    }

    private void parse(JsonObject joQuery) {
        if (joQuery == null) {
            return;
        }
        Log.d(C.LOG_TAG, joQuery.toString());
        for (Map.Entry<String, JsonElement> entry : joQuery.entrySet()) {
            JsonArray jaItem = entry.getValue().getAsJsonArray();
            if (jaItem == null || jaItem.size() != 2) {
                continue;
            }
            Item item = new Item();
            if (!jaItem.get(0).isJsonNull()) {
                item.status = jaItem.get(0).getAsString();
            }
            if (!jaItem.get(1).isJsonNull()) {
                item.url = jaItem.get(1).getAsString();
            }
            items.put(entry.getKey(), item);
        }
    }

    private class Item {
        private String status;
        private String url;
    }
}
