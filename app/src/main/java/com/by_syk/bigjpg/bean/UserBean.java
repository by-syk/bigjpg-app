package com.by_syk.bigjpg.bean;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.by_syk.bigjpg.R;
import com.by_syk.bigjpg.util.C;
import com.google.gson.annotations.SerializedName;

/**
 * Created by By_syk on 2017-08-02.
 */

public class UserBean {
    @SerializedName("status")
    private String status;

    @SerializedName("user")
    private User user;

    @IntDef({GRADE_UNDEFINED, GRADE_FREE, GRADE_BASIC, GRADE_STANDARD, GRADE_PRO})
    public @interface Grade {}

    public static final int GRADE_UNDEFINED = -1;
    // 免费版 2MB/20p/4x
    public static final int GRADE_FREE = 0;
    // 基础版 10MB/500p/16x
    public static final int GRADE_BASIC = 1;
    // 标准版 10MB/500p/16x
    public static final int GRADE_STANDARD = 2;
    // 高级版 10MB/2000p/16x
    public static final int GRADE_PRO = 3;

    public String getStatus() {
        return status;
    }

    public boolean isStatusOk() {
        return "ok".equals(status);
    }

    public boolean isStatusExpire() {
        return "no_login".equals(status);
    }

    @Grade
    public int getGrade() {
        if (user == null) {
            return GRADE_FREE;
        }
        Log.d(C.LOG_TAG, "month_limit: " + user.imgNum);
        switch (user.imgNum) {
            case 500:
                return GRADE_BASIC;
            case 501:
                return GRADE_STANDARD;
            case 2000:
                return GRADE_PRO;
            default:
                return GRADE_FREE;
        }
    }

    @NonNull
    public static String parseGrade(@NonNull Context context, @UserBean.Grade int grade) {
        switch (grade) {
            case UserBean.GRADE_FREE:
                return context.getString(R.string.user_grade_free);
            case UserBean.GRADE_BASIC:
                return context.getString(R.string.user_grade_basic);
            case UserBean.GRADE_STANDARD:
                return context.getString(R.string.user_grade_standard);
            case UserBean.GRADE_PRO:
                return context.getString(R.string.user_grade_pro);
        }
        return context.getString(R.string.user_grade_undefined);
    }

    private class User {
        @SerializedName("month_limit")
        private int imgNum;
    }
}
