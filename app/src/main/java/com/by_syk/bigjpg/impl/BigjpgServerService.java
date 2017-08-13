package com.by_syk.bigjpg.impl;

import com.by_syk.bigjpg.bean.LoginBean;
import com.by_syk.bigjpg.bean.UploadBean;
import com.by_syk.bigjpg.bean.UserBean;
import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by By_syk on 2017-06-08.
 */

public interface BigjpgServerService {
    @FormUrlEncoded
    @POST("login")
    Call<LoginBean> login(@Field("username") String user,
                          @Field("password") String pwd);

    @GET("user")
    Call<UserBean> getUserInfo(@Header("Cookie") String cookie);

    @POST("free")
    @Multipart
    Call<UploadBean> processJpg(@Part("conf") RequestBody conf,
                                @Part("file\"; filename=\"test.jpg") RequestBody img,
                                @Header("Cookie") String cookie);

    @POST("free")
    @Multipart
    Call<UploadBean> processPng(@Part("conf") RequestBody conf,
                                @Part("file\"; filename=\"test.png") RequestBody img,
                                @Header("Cookie") String cookie);

    @GET("free")
    Call<JsonObject> query(@Query("fids") String ids);

    @DELETE("free")
    Call<JsonObject> delete(@Query("fids") String ids);

    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}
