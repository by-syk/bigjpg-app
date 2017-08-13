package com.by_syk.bigjpg;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.by_syk.bigjpg.bean.QueryBean;
import com.by_syk.bigjpg.bean.UploadConfigBean;
import com.by_syk.bigjpg.bean.UploadBean;
import com.by_syk.bigjpg.bean.UserBean;
import com.by_syk.bigjpg.dialog.AboutDialog;
import com.by_syk.bigjpg.dialog.LoginDialog;
import com.by_syk.bigjpg.impl.BigjpgServerService;
import com.by_syk.bigjpg.util.C;
import com.by_syk.bigjpg.util.ExtraUtil;
import com.by_syk.bigjpg.util.FileUtil;
import com.by_syk.bigjpg.util.ImgUtil;
import com.by_syk.bigjpg.util.RetrofitHelper;
import com.by_syk.bigjpg.widget.ConfigView;
import com.by_syk.bigjpg.widget.TimerProgressView;
import com.by_syk.lib.checkimgformat.CheckImgFormat;
import com.by_syk.lib.sp.SP;
import com.by_syk.lib.texttag.TextTag;
import com.by_syk.lib.urianalyser.UriAnalyser;
import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    private SP sp;

    private PhotoView photoView;
    private TimerProgressView viewTimerProgress;
    private TextView tvAction;
    private View viewHint;
    private View viewHintIcon;
    private View viewHintLogin;
    private TextView tvHintLogin;
    private ConfigView cvTypeArt;
    private ConfigView cvTypePhoto;
    private ConfigView cvScale2x;
    private ConfigView cvScale4x;
    private ConfigView cvScale8x;
    private ConfigView cvScale16x;
    private ConfigView cvDenoiseNo;
    private ConfigView cvDenoiseLow;
    private ConfigView cvDenoiseMiddle;
    private ConfigView cvDenoiseHigh;
    private ConfigView cvDenoiseSuper;

    private BottomSheetBehavior bottomSheetBehavior;

    private Uri imgUri;
    private File cacheFile;

    private int userGrade = UserBean.GRADE_FREE;

    private boolean isForeground = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState == null) {
            if (imgUri != null) { // 外部APP请求处理
                showImg(imgUri);
                tvAction.setEnabled(imgUri != null);
                return;
            }
            // 面板动画
            viewHintIcon.setAlpha(0);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvAction.postDelayed(new Runnable() {
                @Override
                public void run() {
                    collapsePanel();
                }
            }, 800);
        } else { // 屏幕方向旋转
            imgUri = savedInstanceState.getParcelable("imgUri");
            showImg(imgUri);
            tvAction.setEnabled(imgUri != null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        isForeground = true;

        cancelNotification();
    }

    @Override
    protected void onStop() {
        super.onStop();

        isForeground = false;
    }

    private void init() {
        sp = new SP(this);

        userGrade = getIntent().getIntExtra("userGrade", UserBean.GRADE_FREE);
        imgUri = getIntent().getParcelableExtra("imgUri");

        photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if (collapsePanel()) {
                    return;
                }
                viewHint.performClick();
            }
        });
        photoView.setOnOutsidePhotoTapListener(new OnOutsidePhotoTapListener() {
            @Override
            public void onOutsidePhotoTap(ImageView imageView) {
                if (collapsePanel()) {
                    return;
                }
                viewHint.performClick();
            }
        });

        viewTimerProgress = (TimerProgressView) findViewById(R.id.view_timer_progress);

        viewHint = findViewById(R.id.view_hint);
        viewHintIcon = findViewById(R.id.iv_hint);

        viewHintLogin = findViewById(R.id.view_login_desc);
        tvHintLogin = (TextView) findViewById(R.id.tv_login_desc);

        tvAction = (TextView) findViewById(R.id.tv_action);

        cvTypeArt = (ConfigView) findViewById(R.id.cv_type_art);
        cvTypePhoto = (ConfigView) findViewById(R.id.cv_type_photo);
        cvScale2x = (ConfigView) findViewById(R.id.cv_scale_2x);
        cvScale4x = (ConfigView) findViewById(R.id.cv_scale_4x);
        cvScale8x = (ConfigView) findViewById(R.id.cv_scale_8x);
        cvScale16x = (ConfigView) findViewById(R.id.cv_scale_16x);
        cvDenoiseNo = (ConfigView) findViewById(R.id.cv_denoise_no);
        cvDenoiseLow = (ConfigView) findViewById(R.id.cv_denoise_low);
        cvDenoiseMiddle = (ConfigView) findViewById(R.id.cv_denoise_middle);
        cvDenoiseHigh = (ConfigView) findViewById(R.id.cv_denoise_high);
        cvDenoiseSuper = (ConfigView) findViewById(R.id.cv_denoise_super);

        if (userGrade == UserBean.GRADE_UNDEFINED || userGrade == UserBean.GRADE_FREE) {
            cvScale8x.setText(new TextTag.Builder()
                    .text(cvScale8x.getText().toString())
                    .tag("  VIP  ")
                    .bgColor(ContextCompat.getColor(this, R.color.tag))
                    .build().render());
            cvScale16x.setText(new TextTag.Builder()
                    .text(cvScale16x.getText().toString())
                    .tag("  VIP  ")
                    .bgColor(ContextCompat.getColor(this, R.color.tag))
                    .build().render());
        }

        configType();
        configScale();
        configDenoise();

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.view_panel));
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            int height = getResources().getDimensionPixelSize(R.dimen.flower);

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (viewHint.getVisibility() == View.VISIBLE) {
                    viewHintIcon.setAlpha((1.0f - slideOffset) * 0.4f);
                    viewHintIcon.setTranslationY(-height * 0.1f * slideOffset);
                }
            }
        });
    }

    public void onChooseImg(View view) {
        if (collapsePanel()) {
            return;
        }
        if (!checkPermissions()) {
            return;
        }
        if ("processing".equals(tvAction.getTag())) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 0);
    }

    public void onAction(View view) {
        collapsePanel();

        switch ((String) view.getTag()) {
            case "optimize":
                if (!checkNetwork()) {
                    break;
                }
                if (!checkBigjpgLimit(imgUri)) {
                    break;
                }
                (new ProcessTask()).execute(imgUri);
                break;
            case "save":
                saveImg(cacheFile);
                break;
        }
    }

    public void onConfigType(View view) {
        int id = view.getId();
        cvTypeArt.setChecked(id == cvTypeArt.getId());
        cvTypePhoto.setChecked(id == cvTypePhoto.getId());
        sp.save("configType", (String) view.getTag());
    }

    public void onConfigScale(View view) {
        int id = view.getId();
        if (userGrade != UserBean.GRADE_UNDEFINED && userGrade != UserBean.GRADE_FREE) {
            cvScale2x.setChecked(id == cvScale2x.getId());
            cvScale4x.setChecked(id == cvScale4x.getId());
            cvScale8x.setChecked(id == cvScale8x.getId());
            cvScale16x.setChecked(id == cvScale16x.getId());
            sp.save("configScale", (String) view.getTag());
        } else {
            if (id == cvScale2x.getId() || id == cvScale4x.getId()) {
                cvScale2x.setChecked(id == cvScale2x.getId());
                cvScale4x.setChecked(id == cvScale4x.getId());
                cvScale8x.setChecked(false);
                cvScale16x.setChecked(false);
                sp.save("configScale", (String) view.getTag());
            } else {
                toggleLoginHint(true);
            }
        }
    }

    public void onConfigDenoise(View view) {
        int id = view.getId();
        cvDenoiseNo.setChecked(id == cvDenoiseNo.getId());
        cvDenoiseLow.setChecked(id == cvDenoiseLow.getId());
        cvDenoiseMiddle.setChecked(id == cvDenoiseMiddle.getId());
        cvDenoiseHigh.setChecked(id == cvDenoiseHigh.getId());
        cvDenoiseSuper.setChecked(id == cvDenoiseSuper.getId());
        sp.save("configDenoise", (String) view.getTag());
    }

    public void onLink(View view) {
        collapsePanel();

        (new AboutDialog()).show(getSupportFragmentManager(), "aboutDialog");
    }

    public void onLogin(View view) {
        collapsePanel();

        LoginDialog dialog = LoginDialog.newInstance(userGrade);
        dialog.setOnLoginListener(new LoginDialog.OnLoginListener() {
            @Override
            public void onLogin() {
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        dialog.show(getSupportFragmentManager(), "loginDialog");
    }

    private void showImg(Uri imgUri) {
        if (imgUri == null) {
            return;
        }

        viewHint.setVisibility(View.GONE);

        Point dimen = ImgUtil.getImgSize(this, imgUri);
        if (dimen != null) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (dimen.x < displayMetrics.widthPixels && dimen.y < displayMetrics.heightPixels) {
                photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
        photoView.setImageDrawable(null);
        photoView.setImageURI(imgUri);
    }

    public void configType() {
        String type;
        if (!sp.contains("configType")) {
            type = "art";
            sp.save("configType", type);
        } else {
            type = sp.getString("configType");
        }
        cvTypeArt.setChecked(Objects.equals(cvTypeArt.getTag(), type));
        cvTypePhoto.setChecked(Objects.equals(cvTypePhoto.getTag(), type));
    }

    public void configScale() {
        String scale;
        if (!sp.contains("configScale")) {
            scale = "1";
            sp.save("configScale", scale);
        } else {
            scale = sp.getString("configScale");
        }
        cvScale2x.setChecked(Objects.equals(cvScale2x.getTag(), scale));
        cvScale4x.setChecked(Objects.equals(cvScale4x.getTag(), scale));
        cvScale8x.setChecked(Objects.equals(cvScale8x.getTag(), scale));
        cvScale16x.setChecked(Objects.equals(cvScale16x.getTag(), scale));
    }

    public void configDenoise() {
        String denoise;
        if (!sp.contains("configDenoise")) {
            denoise = "0";
            sp.save("configDenoise", denoise);
        } else {
            denoise = sp.getString("configDenoise");
        }
        cvDenoiseNo.setChecked(Objects.equals(cvDenoiseNo.getTag(), denoise));
        cvDenoiseLow.setChecked(Objects.equals(cvDenoiseLow.getTag(), denoise));
        cvDenoiseMiddle.setChecked(Objects.equals(cvDenoiseMiddle.getTag(), denoise));
        cvDenoiseHigh.setChecked(Objects.equals(cvDenoiseHigh.getTag(), denoise));
        cvDenoiseSuper.setChecked(Objects.equals(cvDenoiseSuper.getTag(), denoise));
    }

    private boolean collapsePanel() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        }
        return false;
    }

    private void toggleLoginHint(boolean show) {
        if (!show) {
            viewHintLogin.setVisibility(View.INVISIBLE);
            return;
        }
        viewHintLogin.setVisibility(View.VISIBLE);
        tvHintLogin.setTranslationX(tvHintLogin.getWidth() * 2 / 3);
        tvHintLogin.setAlpha(0f);
        tvHintLogin.animate()
                .translationX(0f)
                .alpha(1.0f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void saveImg(File cacheFile) {
        if (!checkPermissions()) {
            return;
        }
        if (cacheFile == null) {
            return;
        }
        String imgPath = UriAnalyser.getRealPath(this, imgUri);
        if (imgPath == null) {
            return;
        }

        File imgFile = new File(imgPath);
        String fileName = imgFile.getName();
        int index = fileName.lastIndexOf(".");
        if (index >= 0) {
            fileName = fileName.substring(0, index);
        }
        fileName = fileName + "-bigjpg" + cacheFile.length();
        String format = CheckImgFormat.get(cacheFile, false);
        if (CheckImgFormat.FORMAT_UNDEFINED.equals(format)) {
            if (index >= 0) {
                format = fileName.substring(index);
            }
        }
        File outFile = new File(imgFile.getParent(), fileName + format);

        boolean ok = FileUtil.copyFile(cacheFile, outFile);
        if (ok) {
            FileUtil.record2Gallery(this, outFile, false);
            tvAction.setText(R.string.status_saved);
        } else {
            tvAction.setText(R.string.status_error);
        }
        tvAction.setEnabled(false);
    }

    private boolean checkNetwork() {
        if (!ExtraUtil.isNetworkConnected(this)) {
            tvAction.setEnabled(false);
            tvAction.setText(R.string.status_no_network);
            return false;
        }
        return true;
    }

    private boolean checkBigjpgLimit(Uri imgUri) {
        String imgPath = UriAnalyser.getRealPath(this, imgUri);
        long size = FileUtil.getSize(imgPath);
        if (userGrade == UserBean.GRADE_UNDEFINED || userGrade == UserBean.GRADE_FREE) {
            if (size > C.BIGJPG_MAX_SIZE_0) {
                tvAction.setEnabled(false);
                tvAction.setText(R.string.status_over_size_0);
                toggleLoginHint(true);
                return false;
            }
        } else {
            if (size > C.BIGJPG_MAX_SIZE_1) {
                tvAction.setEnabled(false);
                tvAction.setText(R.string.status_over_size_1);
                return false;
            }
        }

        Point dimen = ImgUtil.getImgSize(this, imgUri);
        if (dimen != null) {
            if (dimen.x > C.BIGJPG_MAX_W || dimen.y > C.BIGJPG_MAX_H) {
                tvAction.setEnabled(false);
                tvAction.setText(R.string.status_over_resolution);
                return false;
            }
        }

        return true;
    }

    private void postNotification(@NonNull Uri imgUri) {
        String fileName = (new File(UriAnalyser.getRealPath(this, imgUri))).getName();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_done)
                .setContentTitle(getString(R.string.notify_done))
                .setContentText(fileName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    @TargetApi(23)
    private boolean checkPermissions() {
        if (C.SDK < 23 || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putInt("userGrade", userGrade); // 从 getIntent() 取得
        outState.putParcelable("imgUri", imgUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 0) {
            return;
        }
        if (resultCode == RESULT_OK) {
            imgUri = data.getData();
            showImg(imgUri);
        }
        tvAction.setEnabled(imgUri != null);
        tvAction.setTag("optimize");
        tvAction.setText(R.string.action_optimize);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    public static void launch(@NonNull Context context, @UserBean.Grade int userGrade, @Nullable Uri imgUri) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("userGrade", userGrade);
        intent.putExtra("imgUri", imgUri);

        context.startActivity(intent);
    }

    private class ProcessTask extends AsyncTask<Uri, ProcessTask.Status, File> {
        private int queryTime = 180; // 轮询时长，默认3分钟
        private static final int WAIT_TIME = 300; // 等待时长，5分钟
        private static final int QUERY_PERIOD = 15; // 轮询周期，15秒

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvAction.setEnabled(false);
            tvAction.setTag("processing");
            tvAction.setText(getString(R.string.status_uploading));
            tvAction.setKeepScreenOn(true);
        }

        @Override
        protected File doInBackground(Uri... params) {
            Uri imgUri = params[0];

            UploadBean uploadBean = upload(imgUri);
            if (uploadBean == null) {
                publishProgress(new Status(R.string.status_err_upload, 0));
                return null;
            }
            if (!uploadBean.isStatusOk()) {
                Log.d(C.LOG_TAG, "upload status: " + uploadBean.getStatus());
                if (uploadBean.isStatusOverLimit()) {
                    publishProgress(new Status(R.string.status_over_times, 0, true));
                    return null;
                }
                if (uploadBean.isStatusNotImg()) {
                    publishProgress(new Status(R.string.status_not_img, 0));
                    return null;
                }
                if (uploadBean.isStatusParallelLimit()) {
                    publishProgress(new Status(R.string.status_try_later, 0));
                    return null;
                }
                publishProgress(new Status(getString(R.string.status_err_upload,
                        uploadBean.getStatus()), 0));
                return null;
            }

            int timer;
            for (timer = WAIT_TIME; timer >= 0; --timer) {
                SystemClock.sleep(1000);
                if (isDestroyed()) {
                    delete(uploadBean.getId());
                    return null;
                }
                if (timer % QUERY_PERIOD != 0) {
                    continue;
                }
                QueryBean queryBean = query(uploadBean.getId());
                if (queryBean == null || !queryBean.isWaiting(uploadBean.getId())) {
                    break;
                }
                publishProgress(new Status(R.string.status_waiting, QUERY_PERIOD));
            }
            if (timer < 0) {
                publishProgress(new Status(R.string.status_timeout, 0));
                return null;
            }

            int minutes = uploadBean.getMinAbout();
            if (minutes > 0 && minutes * 60 < queryTime) {
                queryTime = minutes * 60;
            }
            publishProgress(new Status(R.string.status_processing, queryTime));
            for (timer = queryTime; timer >= 0; --timer) {
                SystemClock.sleep(1000);
                if (isDestroyed()) {
                    delete(uploadBean.getId());
                    return null;
                }
                if (timer % 15 != 0) {
                    continue;
                }
                QueryBean queryBean = query(uploadBean.getId());
                if (queryBean == null) {
                    publishProgress(new Status(R.string.status_error, 0));
                    return null;
                }
                if (!queryBean.isDone(uploadBean.getId())) {
                    if (queryBean.isProcessing(uploadBean.getId())) {
                        continue;
                    }
                    publishProgress(new Status(R.string.status_error, 0));
                    return null;
                }
                publishProgress(new Status(R.string.status_downloading, 0));
                String url = queryBean.getUrl(uploadBean.getId());
                if (url == null) {
                    publishProgress(new Status(R.string.status_error, 0));
                    return null;
                }
                File cacheFile = download(url);
                if (cacheFile == null) {
                    publishProgress(new Status(R.string.status_error, 0));
                }
                return cacheFile;
            }
            publishProgress(new Status(R.string.status_timeout, 0));
            return null;
        }

        @Override
        protected void onProgressUpdate(Status... values) {
            super.onProgressUpdate(values);

            Status status = values[0];
            tvAction.setText(status.text);
            if (status.progress > 0) {
                viewTimerProgress.start(status.progress);
            } else {
                viewTimerProgress.stop();
            }
            if (status.hintLogin) {
                toggleLoginHint(true);
            }
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);

            if (isDestroyed()) {
                return;
            }

            cacheFile = file;

            if (file != null) {
                showImg(Uri.fromFile(file));
                tvAction.setEnabled(true);
                tvAction.setTag("save");
                tvAction.setText(R.string.action_save);
                if (!isForeground) {
                    postNotification(imgUri);
                }
            } else {
                tvAction.setTag("optimize");
            }
            tvAction.setKeepScreenOn(false);
            viewTimerProgress.stop();
        }

        private UploadBean upload(@NonNull Uri imgUri) {
            String imgPath = UriAnalyser.getRealPath(MainActivity.this, imgUri);
            if (imgPath == null) {
                return null;
            }

            Point dimen = ImgUtil.getImgSize(MainActivity.this, imgUri);
            if (dimen == null) {
                return null;
            }

            BigjpgServerService service = RetrofitHelper.getInstance()
                    .getService(BigjpgServerService.class);
            try {
                UploadConfigBean configBean = new UploadConfigBean.Builder()
                        .type(sp.getString("configType"))
                        .scale(sp.getString("configScale"))
                        .denoise(sp.getString("configDenoise"))
                        .fileName((new File(imgPath)).getName())
                        .fileSize((new File(imgPath)).length())
                        .imgWidth(dimen.x)
                        .imgHeight(dimen.y)
                        .build();
                Log.d(C.LOG_TAG, "config: " + (new Gson()).toJson(configBean));
                RequestBody confBody = RequestBody.create(MediaType.parse("text/plain"),
                        (new Gson()).toJson(configBean));
                RequestBody imgBody;
                Call<UploadBean> call;
                if (CheckImgFormat.is(CheckImgFormat.FORMAT_PNG, new File(imgPath), false)) {
                    imgBody = RequestBody.create(MediaType.parse("image/png"),
                            getImgBytes(imgUri));
                    call = service.processPng(confBody, imgBody, sp.getString("cookie"));
                } else {
                    imgBody = RequestBody.create(MediaType.parse("image/jpeg"),
                            getImgBytes(imgUri));
                    call = service.processJpg(confBody, imgBody, sp.getString("cookie"));
                }
                return call.execute().body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private QueryBean query(@NonNull String id) {
            String ids = "[\"" + id + "\"]";
            BigjpgServerService service = RetrofitHelper.getInstance()
                    .getService(BigjpgServerService.class);
            try {
                Call<JsonObject> call = service.query(ids);
                JsonObject jo = call.execute().body();
                return new QueryBean(jo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void delete(@NonNull String id) {
            String ids = "[\"" + id + "\"]";
            BigjpgServerService service = RetrofitHelper.getInstance()
                    .getService(BigjpgServerService.class);
            try {
                Call<JsonObject> call = service.delete(ids);
                JsonObject jo = call.execute().body();
                Log.d(C.LOG_TAG, "delete: " + jo.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private File download(@NonNull String url) {
            File outFile = new File(getCacheDir(), "tmp");

            BigjpgServerService service = RetrofitHelper.getInstance()
                    .getService(BigjpgServerService.class);
            try {
                Call<ResponseBody> call = service.downloadFile(url);
                // {"4aa84f2dfc734aea8d723bf546cb7f10": ["success", "htdocs/free_ok/b7b48b612e4e4d1cafdc73df0a84a3d8.png"]}
                ResponseBody responseBody = call.execute().body();
                boolean ok = RetrofitHelper.downloadFile(responseBody, outFile);
                if (ok) {
                    return outFile;
                }
                outFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private byte[] getImgBytes(@NonNull Uri imgUri) throws Exception {
            InputStream inputStream = getContentResolver().openInputStream(imgUri);

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            inputStream.close();

            return byteBuffer.toByteArray();
        }

        class Status {
            String text;
            int progress;
            boolean hintLogin;

            Status(String text, int progress) {
                this.text = text;
                this.progress = progress;
            }

            Status(@StringRes int strId, int progress) {
                this(getString(strId), progress);
            }

            Status(@StringRes int strId, int progress, boolean hintLogin) {
                this(getString(strId), progress);
                this.hintLogin = hintLogin;
            }
        }
    }
}
