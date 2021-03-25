package com.cursoandroid.gabriel.instagramclone.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cursoandroid.gabriel.instagramclone.R;
import com.cursoandroid.gabriel.instagramclone.helper.Converters;
import com.cursoandroid.gabriel.instagramclone.helper.ImageDownloader;
import com.cursoandroid.gabriel.instagramclone.model.Post;
import com.cursoandroid.gabriel.instagramclone.model.UserProfile;
import com.cursoandroid.gabriel.instagramclone.services.UserServices;
import com.like.LikeButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {

    private List<Post> listPost;
    private Retrofit retrofit;
    private UserServices userServices;
    private Context context;

    public AdapterFeed(List<Post> listPost, Retrofit retrofit, Context context) {
        this.listPost = listPost;
        this.retrofit = retrofit;
        userServices = retrofit.create(UserServices.class);
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_feed, parent, false);
        return new AdapterFeed.MyViewHolder(itemLista);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Post post = listPost.get(position);
        new ImageDownloader(holder.postImage).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, post.getImageUrl());
        holder.description.setText(post.getDescription());

        Call<UserProfile> callUser = userServices.getUserProfileById(post.getIdUser());
        callUser.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if(response.isSuccessful() && response.body() != null){
                    holder.name.setText(response.body().getUsername());
                    if(response.body().getProfileImage_path_name() != null) {
                        new ImageDownloader(holder.profilePhoto).execute(response.body().getProfileImage_path_name());
                    }
                }else{
                    Toast.makeText(context, Converters.converterErrorBodyToString(response), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(context, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPost.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profilePhoto;
        TextView name, description, likeNumber;
        ImageView postImage;
        ImageButton comentButton;
        LikeButton likeButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePhoto = itemView.findViewById(R.id.imagePerfilPostagem);
            name = itemView.findViewById(R.id.textPerfilPostagem);
            description = itemView.findViewById(R.id.textDescricaoPostagem);
            likeNumber = itemView.findViewById(R.id.textQtdCurtidasPostagem);
            postImage = itemView.findViewById(R.id.imagePostagemSelecionada);
            likeButton = itemView.findViewById(R.id.likeButtonFeed);
            comentButton = itemView.findViewById(R.id.imageButtonComentarioFeed);
        }
    }
}