package com.itechteam.android.coronaupdate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;

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
    private static Retrofit retrofit;
    private ImageView flagImageView;

    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;

    private TextView countryNameValueTextView;
    private TextView newConfirmedValueTextView;
    private TextView totalConfirmedValueTextView;
    private TextView newDeathsValueTextView;
    private TextView totalDeathsValueTextView;
    private TextView newRecoveredValueTextView;
    private TextView totalRecoveredValueTextView;
    private TextView dateUpdatedValueTextView;

    private MenuItem shareMenuItem;

    private Summary.Countries selectedCountry;
    private String flagUrl;
    private View rootView;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        shareMenuItem = menu.findItem(R.id.action_share);
        shareMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                verifyStoragePermissions(DetailActivity.this);
            } else {
                Date d = new Date();
                String fileName = DateFormat.format("MMMM d, yyyy hh:mm:ss", d.getTime()) + ".png";

                store(getScreenshot(rootView), fileName);

            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getRequestBuilder() {
        requestBuilder = Glide.with(getApplicationContext())
                .using(Glide.buildStreamModelLoader(Uri.class, getApplicationContext()), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                .placeholder(R.drawable.placeholder_loading_image)
                .decoder(new SvgDecoder())
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<>());
    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public Bitmap getScreenshot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void store(Bitmap bm, String fileName) {
        final File dir = new File(getExternalFilesDir(null) + "/Screenshots");
        final File image = new File(dir.getAbsolutePath(), fileName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            FileOutputStream fOut = new FileOutputStream(image);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        shareImage(image);
    }

    public void shareImage(File file) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_SEND);
        Intent chooser = Intent.createChooser(intent, "Share Screenshot to");
        intent.setType("image/*");
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

        } else {
            Uri path = Uri.fromFile(file);
            intent.putExtra(Intent.EXTRA_STREAM, path);
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, "Coronavirus Update");
        intent.putExtra(Intent.EXTRA_TEXT, "Current cases in " + selectedCountry.getCountry() + " are: " + formatter.format(selectedCountry.getTotalConfirmed()));
        try {
            startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No Sharing App Available", Toast.LENGTH_SHORT).show();
        }
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://restcountries.eu/rest/v2/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public void setTextValues() {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String date = "";

        date += DateFormat.getLongDateFormat(getApplicationContext()).format(selectedCountry.getDate());
        countryNameValueTextView.setText(selectedCountry.getCountry());
        newConfirmedValueTextView.setText(formatter.format(selectedCountry.getNewConfirmed()));
        totalConfirmedValueTextView.setText(formatter.format(selectedCountry.getTotalConfirmed()));
        newDeathsValueTextView.setText(formatter.format(selectedCountry.getNewDeaths()));
        totalDeathsValueTextView.setText(formatter.format(selectedCountry.getTotalDeaths()));
        newRecoveredValueTextView.setText(formatter.format(selectedCountry.getNewRecovered()));
        totalRecoveredValueTextView.setText(formatter.format(selectedCountry.getTotalRecovered()));
        dateUpdatedValueTextView.setText(date);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        // References to all of the views
        Toolbar androidToolbar = findViewById(R.id.android_toolbar);
        countryNameValueTextView = findViewById(R.id.countryNameValueTextView);
        newConfirmedValueTextView = findViewById(R.id.newConfirmedValueTextView);
        totalConfirmedValueTextView = findViewById(R.id.totalConfirmedValueTextView);
        newDeathsValueTextView = findViewById(R.id.newDeathsValueTextView);
        totalDeathsValueTextView = findViewById(R.id.totalDeathsValueTextView);
        newRecoveredValueTextView = findViewById(R.id.newRecoveredValueTextView);
        totalRecoveredValueTextView = findViewById(R.id.totalRecoveredValueTextView);
        dateUpdatedValueTextView = findViewById(R.id.dateUpdatedValueTextView);
        flagImageView = findViewById(R.id.flagImageView);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        setSupportActionBar(androidToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        getRequestBuilder();

        Intent intent = getIntent();
        selectedCountry = (Summary.Countries) intent.getSerializableExtra("selectedCountry");
        androidToolbar.setTitle((selectedCountry != null ? selectedCountry.getCountry() : null) + " Details");

        setTextValues();

        RestCountriesApi restCountriesApi = getRetrofit().create(RestCountriesApi.class);
        Call<CountriesFlag> call = restCountriesApi.getCountryFlags("https://restcountries.eu/rest/v2/alpha/" + selectedCountry.getCountryCode() + "?fields=flag");
        call.enqueue(new Callback<CountriesFlag>() {
            @Override
            public void onResponse(Call<CountriesFlag> call, Response<CountriesFlag> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DetailActivity.this, "Sorry, country flag not found: " + response.code(), Toast.LENGTH_SHORT).show();
                    flagImageView.setImageResource(R.drawable.placeholder_flag_error_image);
                } else {
                    flagUrl = response.body().getFlag();
                    Uri uri = Uri.parse(flagUrl);
                    requestBuilder
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .load(uri)
                            .into(flagImageView);
                }
                shareMenuItem.setVisible(true);
            }

            @Override
            public void onFailure(Call<CountriesFlag> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Error encountered: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
