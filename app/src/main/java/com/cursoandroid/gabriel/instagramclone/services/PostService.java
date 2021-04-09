package com.cursoandroid.gabriel.instagramclone.services;

import com.cursoandroid.gabriel.instagramclone.model.Post;
import com.cursoandroid.gabriel.instagramclone.search.PostSearch;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostService {

    @POST(value = "/posts")
    Call<Void> createPost(@Body Post post);

    @GET(value = "/posts/all")
    Call<PostSearch> searchPost(@Query("ids") Long x);
}
