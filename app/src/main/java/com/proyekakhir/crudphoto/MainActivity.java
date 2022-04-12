package com.proyekakhir.crudphoto;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.proyekakhir.crudphoto.server.Client;
import com.proyekakhir.crudphoto.server.Network;
import com.proyekakhir.crudphoto.server.ResponseCrud;
import com.proyekakhir.crudphoto.util.PathUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button,upload;
    EditText username,password,level;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.previewImage);
        upload = findViewById(R.id.btnUpload);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        level = findViewById(R.id.level);
        button = findViewById(R.id.btnImage);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("TAG", "onClick: " );
                checkPermission();
            }
        });

        upload.setOnClickListener(view -> {
            Client client =new Client();
            Network network = client.getAPI();

            RequestBody usernameBody = convertStringToRequestBody(username.getText().toString());
            RequestBody passwordBody = convertStringToRequestBody(password.getText().toString());
            RequestBody levelBody = convertStringToRequestBody(level.getText().toString());
            MultipartBody.Part imageBody = convertUriToMultipartBody(iv,"photo");

            network.insert(usernameBody,passwordBody,levelBody,imageBody).enqueue(new Callback<ResponseCrud>() {
                @Override
                public void onResponse(Call<ResponseCrud> call, Response<ResponseCrud> response) {
                    if(response.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Success upload", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "gagal 1", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseCrud> call, Throwable t) {
                    Log.e("TAG", "onFailure: "+t.getMessage().toString() );
                    Toast.makeText(MainActivity.this, "gagal 2", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    String[] menu = {"Camera","Gallery"};
    private void  showMenuCamera(){
        new MaterialAlertDialogBuilder(this).setTitle("Pilih image")
                .setItems(menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            dispatchTakePictureIntent();
                        }else{
                            openGallery();
                        }
                    }
                }).show();
    }

    private MultipartBody.Part convertUriToMultipartBody(Uri uri,String name){
        File file= null;
        try {
            file = new File(Objects.requireNonNull(PathUtil.getPath(this, uri)));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);


        return MultipartBody.Part.createFormData(name, file.getName(), requestFile);
    }

    private RequestBody convertStringToRequestBody(String name){
        return RequestBody.create(MediaType.parse("multipart/form-data"), name);
    }

    private void checkPermission(){
        Log.e("TAG", "checkPermission: "+ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) );
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            showMenuCamera();
        }else{
            // You can directly ask for the permission.
            String[] data = {Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissionLauncher.launch(data);
            Log.d("", "");
        }
    }

    private ActivityResultLauncher<Intent> resultCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result->{
        if(result.getResultCode() == Activity.RESULT_OK){
            if(iv != null){
                try {
                    Log.e("TAG", "data: "+ PathUtil.getPath(this,iv));
                    imageView.setImageURI(iv);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private ActivityResultLauncher<Intent> resultGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result->{
        if(result.getResultCode() == Activity.RESULT_OK){
            Log.e("TAG", "1: " );
            Log.e("TAG", "2: " );
            try {
                Log.e("TAG", "data: "+ result.getData().getData());
                Log.e("TAG", "data: "+ PathUtil.getPath(this,result.getData().getData()));
                iv = result.getData().getData();
                imageView.setImageURI(iv);
            } catch (URISyntaxException e) {
                Log.e("TAG", "3: " );
                e.printStackTrace();
            }
        }
    });
    boolean status =  true;
    @SuppressLint("NewApi")
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {

                isGranted.entrySet().forEach(b->{
                    status =status && b.getValue();
                });
                if(status){
                    showMenuCamera();
                }else{
                    Log.e("TAG", "Permission error: " );
                }

            });


    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        resultGallery.launch(intent);
    }

    private void dispatchTakePictureIntent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"imageCamera");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"fromCamera");
        iv =getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, iv);
        resultCamera.launch(intent);
    }






}