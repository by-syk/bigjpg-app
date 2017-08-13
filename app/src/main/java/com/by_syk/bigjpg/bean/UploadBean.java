package com.by_syk.bigjpg.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by By_syk on 2017-08-02.
 */

public class UploadBean {
    @SerializedName("status")
    private String status;

    @SerializedName("info")
    private String id;

    @SerializedName("time")
    private int minAbout;

    public String getStatus() {
        return status;
    }

    public boolean isStatusOk() {
        return "ok".equals(status);
    }

    /**
     * 免费版仅有20次/月
     */
    public boolean isStatusOverLimit() {
        return "month_limit".equals(status);
    }

    /**
     * 免费版同时只能放大两张图片
     */
    public boolean isStatusParallelLimit() {
        return "parallel_limit".equals(status);
    }

    /**
     * 非图片文件
     */
    public boolean isStatusNotImg() {
        return "type_error".equals(status);
    }

    public String getId() {
        return id;
    }

    public int getMinAbout() {
        return minAbout;
    }
}
