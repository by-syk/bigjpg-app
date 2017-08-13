package com.by_syk.bigjpg.bean;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

/**
 * Created by By_syk on 2017-08-06.
 */

public class UploadConfigBean {
    // 图片类型
    @SerializedName("style")
    @Type
    private String type;

    // 放大倍数
    @SerializedName("x2")
    @Scale
    private String scale;

    // 降噪程度
    @SerializedName("noise")
    @Denoise
    private String denoise;

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("files_size")
    private long fileSize;

    @SerializedName("file_width")
    private int imgWidth;

    @SerializedName("file_height")
    private int imgHeight;

    // TODO 额外添加
    @SerializedName("app")
    private boolean app = true;

    @StringDef({TYPE_ART, TYPE_PHOTO})
    @interface Type {}
    @StringDef({SCALE_2X, SCALE_4X, SCALE_8X, SCALE_16X})
    @interface Scale {}
    @StringDef({DENOISE_NO, DENOISE_LOW, DENOISE_MIDDLE, DENOISE_HIGH, DENOISE_SUPER})
    @interface Denoise {}

    public static final String TYPE_ART = "art";
    public static final String TYPE_PHOTO = "photo";
    public static final String SCALE_2X = "1";
    public static final String SCALE_4X = "2";
    public static final String SCALE_8X = "3";
    public static final String SCALE_16X = "4";
    public static final String DENOISE_NO = "-1";
    public static final String DENOISE_LOW = "-1";
    public static final String DENOISE_MIDDLE = "0";
    public static final String DENOISE_HIGH = "1";
    public static final String DENOISE_SUPER = "2";

    public static class Builder {
        private UploadConfigBean bean = new UploadConfigBean();

        public Builder type(@Type String type) {
            bean.type = type;
            return this;
        }

        public Builder scale(@Scale String scale) {
            bean.scale = scale;
            return this;
        }

        public Builder denoise(@Denoise String denoise) {
            bean.denoise = denoise;
            return this;
        }

        public Builder fileName(String fileName) {
            bean.fileName = fileName;
            return this;
        }

        public Builder fileSize(long fileSize) {
            bean.fileSize = fileSize;
            return this;
        }

        public Builder imgWidth(int imgWidth) {
            bean.imgWidth = imgWidth;
            return this;
        }

        public Builder imgHeight(int imgHeight) {
            bean.imgHeight = imgHeight;
            return this;
        }

        public UploadConfigBean build() {
            return bean;
        }
    }
}
