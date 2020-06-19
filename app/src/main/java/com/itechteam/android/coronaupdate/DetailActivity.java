package com.itechteam.android.coronaupdate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity {
    private static Gson gson;
    private static Retrofit retrofit;
    private ImageView flagImageView;

    private GenericRequestBuilder requestBuilder;

    private TextView countryNameValueTextView;
    private TextView newConfirmedValueTextView;
    private TextView totalConfirmedValueTextView;
    private TextView newDeathsValueTextView;
    private TextView totalDeathsValueTextView;
    private TextView newRecoveredValueTextView;
    private TextView totalRecoveredValueTextView;

    private Button shareButton;
    private Summary.Countries selectedCountry;
    private String flagUrl;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private View rootView;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void getRequestBuilder() {
        requestBuilder = Glide.with(getApplicationContext())
                .using(Glide.buildStreamModelLoader(Uri.class, getApplicationContext()), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>());
    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public Bitmap getScreenshot(View view) {
        shareButton.setVisibility(View.GONE);
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static void store(Bitmap bm, String fileName) {
        final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shareImage(File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (Exception e) {
            Toast.makeText(DetailActivity.this, "No Sharing App Available", Toast.LENGTH_SHORT).show();
        }
    }

    public static Retrofit getRetrofit() {
        gson = new GsonBuilder()
                .create();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://restcountries.eu/rest/v2/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public void setTextValues() {
        countryNameValueTextView.setText(selectedCountry.getCountry());
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        newConfirmedValueTextView.setText(formatter.format(selectedCountry.getNewConfirmed()));
        totalConfirmedValueTextView.setText(formatter.format(selectedCountry.getTotalConfirmed()));
        newDeathsValueTextView.setText(formatter.format(selectedCountry.getNewDeaths()));
        totalDeathsValueTextView.setText(formatter.format(selectedCountry.getTotalDeaths()));
        newRecoveredValueTextView.setText(formatter.format(selectedCountry.getNewRecovered()));
        totalRecoveredValueTextView.setText(formatter.format(selectedCountry.getTotalRecovered()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);
        Toolbar androidToolbar = findViewById(R.id.android_toolbar);
        setSupportActionBar(androidToolbar);
        getRequestBuilder();

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        countryNameValueTextView = findViewById(R.id.countryNameValueTextView);
        newConfirmedValueTextView = findViewById(R.id.newConfirmedValueTextView);
        totalConfirmedValueTextView = findViewById(R.id.totalConfirmedValueTextView);
        newDeathsValueTextView = findViewById(R.id.newDeathsValueTextView);
        totalDeathsValueTextView = findViewById(R.id.totalDeathsValueTextView);
        newRecoveredValueTextView = findViewById(R.id.newRecoveredValueTextView);
        totalRecoveredValueTextView = findViewById(R.id.totalRecoveredValueTextView);

        Intent intent = getIntent();
        selectedCountry = (Summary.Countries) intent.getSerializableExtra("selectedCountry");

        androidToolbar.setTitle(selectedCountry.getCountry() + " Details");
        setSupportActionBar(androidToolbar);

        this.imageLoader.init(ImageLoaderConfiguration.createDefault(getBaseContext()));
        flagImageView = findViewById(R.id.flagImageView);
        shareButton = findViewById(R.id.shareButton);
        setTextValues();
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(permission != PackageManager.PERMISSION_GRANTED) {
                    verifyStoragePermissions(DetailActivity.this);
                } else {
                    Date d = new Date();
                    String fileName = DateFormat.format("MMMM d, yyyy hh:mm:ss", d.getTime()) + ".png";
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots", fileName);
                    store(getScreenshot(rootView), fileName);
                    shareImage(file);
                    shareButton.setVisibility(View.VISIBLE);
                }
            }
        });
        RestCountriesApi restCountriesApi = getRetrofit().create(RestCountriesApi.class);
        Call<List<CountriesFlag>> call = restCountriesApi.getCountryFlags("https://restcountries.eu/rest/v2/name/" + selectedCountry.getCountry().toLowerCase() + "?fields=flag");
        call.enqueue(new Callback<List<CountriesFlag>>() {
            @Override
            public void onResponse(Call<List<CountriesFlag>> call, Response<List<CountriesFlag>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DetailActivity.this, "Sorry, country flag not found: " + response.code(), Toast.LENGTH_SHORT).show();
                } else {
                    flagUrl = response.body().get(0).getFlag();
                    Uri uri = Uri.parse(flagUrl);
                    requestBuilder
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .load(uri)
                            .into(flagImageView);
                }
            }

            @Override
            public void onFailure(Call<List<CountriesFlag>> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Error encountered: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
