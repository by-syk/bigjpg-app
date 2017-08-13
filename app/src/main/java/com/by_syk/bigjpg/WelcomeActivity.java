package com.by_syk.bigjpg;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.by_syk.bigjpg.bean.LoginBean;
import com.by_syk.bigjpg.bean.UserBean;
import com.by_syk.bigjpg.impl.BigjpgServerService;
import com.by_syk.bigjpg.util.C;
import com.by_syk.bigjpg.util.ExtraUtil;
import com.by_syk.bigjpg.util.RetrofitHelper;
import com.by_syk.lib.sp.SP;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by By_syk on 2017-08-06.
 */

public class WelcomeActivity extends AppCompatActivity {
    private TextView tv;

    private Uri reqUri;

    private boolean reqLaunch;

    @UserBean.Grade
    private int userGrade = UserBean.GRADE_UNDEFINED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        init();

        (new LoginTask()).execute();
    }

    private void init() {
        tv = (TextView) findViewById(R.id.tv);

        reqUri = getReqUri();
        if (reqUri != null) {
            reqLaunch = true;
        }
    }

    public void onLaunch(View view) {
        if (userGrade == UserBean.GRADE_UNDEFINED) {
            reqLaunch = true;
            return;
        }
        launch();
    }

    private void launch() {
        MainActivity.launch(this, userGrade, reqUri);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showButton() {
        View view = findViewById(R.id.bt_launch);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1.0f)
                .start();
    }

    private Uri getReqUri() {
        if (!Intent.ACTION_SEND.equals(getIntent().getAction())) {
            return null;
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return null;
        }
        return (Uri) bundle.get(Intent.EXTRA_STREAM);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private class LoginTask extends AsyncTask<String, String, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            if (!ExtraUtil.isNetworkConnected(WelcomeActivity.this)) {
                publishProgress(getString(R.string.status_no_network));
                return UserBean.GRADE_UNDEFINED;
            }

            SP sp = new SP(WelcomeActivity.this);
            String cookie = sp.getString("cookie", null);
            Log.d(C.LOG_TAG, "cookie: " + cookie);
            if (cookie != null) {
                publishProgress(getString(R.string.slogan_check));
                UserBean userBean = getUserGrade(cookie);
                if (userBean == null) {
                    publishProgress(getString(R.string.status_check_failed));
                    return UserBean.GRADE_UNDEFINED;
                }
                if (!userBean.isStatusOk()) {
                    if (!userBean.isStatusExpire()) { // 非 cookie 失效（超过一年）
                        publishProgress(getString(R.string.status_check_failed));
                        return UserBean.GRADE_UNDEFINED;
                    }
                    sp.delete("cookie");
                }
                return userBean.getGrade();
            }

            String user = sp.getString("user", null);
            String pwd = sp.getString("pwd", null);
            if (user == null || pwd == null) {
                return UserBean.GRADE_FREE;
            }
            publishProgress(getString(R.string.slogan_login));
            LoginBean bean = login(user, pwd);
            if (bean == null) {
                publishProgress(getString(R.string.status_login_failed));
                return UserBean.GRADE_UNDEFINED;
            }
            if (!bean.isStatusOk()) {
                if (bean.isStatusErrPwd()) {
                    publishProgress(getString(R.string.status_err_pwd));
                    return UserBean.GRADE_UNDEFINED;
                }
                publishProgress(getString(R.string.status_login_failed));
                return UserBean.GRADE_UNDEFINED;
            }
            cookie = bean.getCookie();
            if (cookie != null) {
                sp.save("cookie", cookie);
                publishProgress(getString(R.string.slogan_check));
                UserBean userBean = getUserGrade(cookie);
                if (userBean == null || !userBean.isStatusOk()) {
                    publishProgress(getString(R.string.status_check_failed));
                    return UserBean.GRADE_UNDEFINED;
                }
                return userBean.getGrade();
            }
            publishProgress(getString(R.string.status_login_failed));
            return UserBean.GRADE_UNDEFINED;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            tv.append("\n" + values[0]);
        }

        @Override
        protected void onPostExecute(Integer userGrade) {
            super.onPostExecute(userGrade);

            if (userGrade == UserBean.GRADE_UNDEFINED) {
                userGrade = UserBean.GRADE_FREE;
            } else {
                tv.append("\n" + UserBean.parseGrade(WelcomeActivity.this, userGrade));
            }
            WelcomeActivity.this.userGrade = userGrade;
            if (reqLaunch) {
                launch();
            } else {
                showButton();
            }
        }

        private LoginBean login(@NonNull String user, @NonNull String pwd) {
            BigjpgServerService service = RetrofitHelper.getInstance()
                    .getService(BigjpgServerService.class);
            Call<LoginBean> call = service.login(user, pwd);
            try {
                Response<LoginBean> response = call.execute();
                LoginBean bean = response.body();
                if (bean != null && bean.isStatusOk()) {
                    String cookie = response.headers().get("Set-Cookie");
                    if (cookie != null) {
                        bean.setCookie(cookie.split(";")[0]);
                    }
                }
                return bean;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private UserBean getUserGrade(@NonNull String cookie) {
            BigjpgServerService service = RetrofitHelper.getInstance()
                    .getService(BigjpgServerService.class);
            Call<UserBean> call = service.getUserInfo(cookie);
            try {
                return call.execute().body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
