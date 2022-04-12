package com.proyekakhir.crudphoto.server;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Network {

    @Multipart
    @POST("api/v1/user")
    Call<ResponseCrud> insert(
            @Part("username") RequestBody username,
            @Part("password") RequestBody pasword,
            @Part("level") RequestBody level,
            @Part MultipartBody.Part photo
    );
}
