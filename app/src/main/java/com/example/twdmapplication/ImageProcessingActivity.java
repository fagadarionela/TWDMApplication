package com.example.twdmapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImageProcessingActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonChooseImage;
    private Button buttonProcessImage;
    private Button buttonProcessDefaultImage;
    private ImageView imageViewOriginal;
    private ImageView imageViewProcessed;

    private static final String IPV4_ADDRESS = "192.168.1.10";
    private static final String PORT_NUMBER = "5000";

    private Bitmap originalBitmap;
    private Bitmap grayscaleBitmap;

    private static final int READ_EXTERNAL_STORAGE_CODE = 1;
    private static final int SELECT_PICTURE_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processing);

        //request storage in order to access the storage to upload the image
        requestStoragePermission();

        //Initializing views
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonProcessImage = findViewById(R.id.buttonProcessImage);
        buttonProcessDefaultImage = findViewById(R.id.buttonProcessDefaultImage);
        imageViewOriginal = findViewById(R.id.imageViewOriginal);
        imageViewProcessed = findViewById(R.id.imageViewProcessed);

        //Setting clicklistener
        buttonChooseImage.setOnClickListener(this);
        buttonProcessImage.setOnClickListener(this);
        buttonProcessDefaultImage.setOnClickListener(this);
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_CODE);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageViewOriginal.setImageBitmap(originalBitmap);
                imageViewProcessed.setImageResource(android.R.color.transparent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v == buttonChooseImage) {
            showFileChooser();
        }
        if (v == buttonProcessImage) {
            if (originalBitmap != null) {
                connectServer(originalBitmap);
            }
            else{
                Toast.makeText(this, "Choose an image first", Toast.LENGTH_LONG).show();
            }
        }
        if (v == buttonProcessDefaultImage) {
            InputStream inputStream;
            try {
                inputStream = getAssets().open("image.jpg");
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                originalBitmap = BitmapFactory.decodeStream(bufferedInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageViewOriginal.setImageBitmap(originalBitmap);
            imageViewProcessed.setImageResource(android.R.color.transparent);
        }
    }

    public void connectServer(Bitmap bitmap) {

        if (bitmap == null) { // This means no image is provided and thus nothing to upload.
            System.out.println("No Bitmap provided. Try Again.");
            return;
        }

        System.out.println("Sending the Files. Please Wait ...");

        String postUrl = "http://" + IPV4_ADDRESS + ":" + PORT_NUMBER + "/";

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch (Exception e) {
            System.out.println("Please Make Sure the Selected File is an Image.");
            return;
        }
        byte[] byteArray = stream.toByteArray();

        multipartBodyBuilder.addFormDataPart("image", "provided_image.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));

        RequestBody postBodyImage = multipartBodyBuilder.build();

        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder().addHeader("Connection", "close").build();
                        return chain.proceed(request);
                    }
                })
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                runOnUiThread(() -> System.out.println("Failed to Connect to Server. Please Try Again."));
            }

            @Override
            public void onResponse(Call call, final Response response) {
                assert response.body() != null;
                InputStream inputStream = response.body().byteStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                grayscaleBitmap = BitmapFactory.decodeStream(bufferedInputStream);
                runOnUiThread(() -> imageViewProcessed.setImageBitmap(grayscaleBitmap));
            }
        });
    }
}
