package com.cursoandroid.gabriel.instagramclone.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cursoandroid.gabriel.instagramclone.R;
import com.cursoandroid.gabriel.instagramclone.adapter.AdapterGrid;
import com.cursoandroid.gabriel.instagramclone.helper.Configurators;
import com.cursoandroid.gabriel.instagramclone.helper.Dialog;
import com.cursoandroid.gabriel.instagramclone.helper.downloaders.ImageDownloader;
import com.cursoandroid.gabriel.instagramclone.model.Post;
import com.cursoandroid.gabriel.instagramclone.model.UserProfile;
import com.cursoandroid.gabriel.instagramclone.services.UserServices;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONObject;

import java.util.ArrayList;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PerfilAmigoActivity extends AppCompatActivity {

    private TextView textButtonAcaoPerfil;
    private CircleImageView imagemPerfil;
    private ImageButton buttonAcaoPerfil;
    private GridView gridViewPerfil;
    private ProgressBar progressBar;

    private UserProfile friendUser;
    private UserProfile currentUser;

    private Retrofit retrofit;
    private UserServices userServices;

    private AdapterGrid adapterGrid;

    private TextView textPublicacoes, textSeguidores, textSeguindo;

    private List<Post> postagens = new ArrayList<>();

    private AlertDialog dialog;

    private boolean following;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //Configurações iniciais
        inicializarComponentes();
        configRetrofit();

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperado dados. Aguarde...")
                .setCancelable(false)
                .build();
        dialog.show();

        Toolbar toolbar = findViewById(R.id.toolbarAlternativa);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            retrieveUser(bundle.getLong("user"));
        }

        carregarFotosPostagem();
        inicializarImageLoader();

        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFollow();
            }
        });

        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Post postagem = postagens.get(position);
                Intent i = new Intent(getApplicationContext(), VisualizarPostagemActivity.class);
                i.putExtra("postagem", postagem);
                startActivity(i);
            }
        });
    }

    private void configRetrofit() {
        retrofit = Configurators.retrofitConfigurator(this);
        userServices = retrofit.create(UserServices.class);
    }

    public void inicializarComponentes(){
        textButtonAcaoPerfil = findViewById(R.id.textBotaoAcaoPerfil);
        imagemPerfil = findViewById(R.id.imagePerfil);
        textPublicacoes = findViewById(R.id.textPublicacoes);
        textSeguidores = findViewById(R.id.textSeguidores);
        textSeguindo = findViewById(R.id.textSeguindo);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        textButtonAcaoPerfil.setText("Carregando...");
        gridViewPerfil = findViewById(R.id.gridViewPerfil);
        progressBar = findViewById(R.id.progressBarEditarPerfil);
    }

    public void inicializarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()).build();
        ImageLoader.getInstance().init(config);
    }

    public void carregarFotosPostagem(){
    }

    public void recuperarDadosUsuariosLogado(){
        Call<UserProfile> call = userServices.getCurrentUser();
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if(response.isSuccessful()){
                    currentUser = response.body();
                    if(followVerification()) textButtonAcaoPerfil.setText("Seguindo");
                    else textButtonAcaoPerfil.setText("Seguir");
                }
                else {
                    try {
                        JSONObject json = new JSONObject(response.errorBody().string());
                        Dialog.dialogError(PerfilAmigoActivity.this, json.getString("message"), json.getString("details"));
                    } catch (Exception e) {
                        Toast.makeText(PerfilAmigoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(PerfilAmigoActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean followVerification(){
        return currentUser.getFollowing().contains(friendUser.getId());
    }

    private void saveFollow(){
        if(following){
            Call<Void> call = userServices.removeFollow(friendUser.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(response.isSuccessful()) {
                        following = false;
                        textButtonAcaoPerfil.setText("Seguir");
                        Long number = (Long.parseLong( textSeguidores.getText().toString() ) - 1L);
                        textSeguidores.setText(String.valueOf(number));
                    }
                    else {
                        try {
                            JSONObject json = new JSONObject(response.errorBody().string());
                            Dialog.dialogError(PerfilAmigoActivity.this, json.getString("message"), json.getString("details"));
                        } catch (Exception e) {
                            Toast.makeText(PerfilAmigoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(PerfilAmigoActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            Call<Void> call = userServices.newFollow(friendUser.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(response.isSuccessful()){
                        following = true;
                        textButtonAcaoPerfil.setText("Seguindo");
                        Long number = (Long.parseLong( textSeguidores.getText().toString() ) + 1L);
                        textSeguidores.setText(String.valueOf(number));
                    }
                    else {
                        try {
                            JSONObject json = new JSONObject(response.errorBody().string());
                            Dialog.dialogError(PerfilAmigoActivity.this, json.getString("message"), json.getString("details"));
                        } catch (Exception e) {
                            Toast.makeText(PerfilAmigoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(PerfilAmigoActivity.this, "Failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void retrieveUser(Long id){
        Call<UserProfile> call = userServices.getUserById(id);
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if(response.isSuccessful()) {
                    friendUser = response.body();
                    loadUserData();
                    recuperarDadosUsuariosLogado();
                }
                else{
                    try {
                        JSONObject json = new JSONObject(response.errorBody().string());
                        Dialog.dialogError(PerfilAmigoActivity.this, json.getString("message"), json.getString("details"));
                    } catch (Exception e) {
                        Toast.makeText(PerfilAmigoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Toast.makeText(PerfilAmigoActivity.this, "Failure:" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

            private void loadUserData() {
                getSupportActionBar().setTitle(friendUser.getUsername());
                List<Long> followers = friendUser.getFollowers();
                List<Long> following = friendUser.getFollowing();
                Long postsNumber = friendUser.getPostsNumber();
                if(followers != null) textSeguidores.setText(String.valueOf(followers.size()));
                if(following != null) textSeguindo.setText(String.valueOf(following.size()));
                if(postsNumber != null) textSeguindo.setText(String.valueOf(postsNumber));

                String url = friendUser.getImageUrl();
                if(url != null && !url.equals("")){
                    new ImageDownloader(imagemPerfil, progressBar).execute(url);
                }else{
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}