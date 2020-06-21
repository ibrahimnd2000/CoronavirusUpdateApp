package com.itechteam.android.coronaupdate;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Date;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private RecyclerView myRecyclerView;
    private RecyclerAdapter recyclerAdapter;
    private List<Summary.Countries> countriesList;
    private ProgressBar progressBar;
    private MenuItem searchItem;
    private SwipeRefreshLayout pullToRefresh;
    private AdView adView;
    private Toast internetErrorToast;
    private static final String TAG = "MainActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(this);
        searchItem.setVisible(false);
        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                pullToRefresh.setEnabled(false);
                return true;
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void refreshList() {
        Covid19Api covid19Api = RetrofitClient.getApiService();

        Call<Summary> call = covid19Api.getSummary();
        call.enqueue(new Callback<Summary>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Summary> call, Response<Summary> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.code());
                }
                progressBar.setVisibility(View.GONE);
                if (response.body() != null) {
                    countriesList = response.body().getCountriesList();
                }
                recyclerAdapter = new RecyclerAdapter(getApplicationContext(), countriesList);
                myRecyclerView.setAdapter(recyclerAdapter);
                Date date = null;
                if (response.body() != null) {
                    date = response.body().getDate();
                }
                Toast.makeText(MainActivity.this, "Updated on: " + DateFormat.format("dd-MM-yyyy hh:mm:ss a", date), Toast.LENGTH_SHORT).show();
                pullToRefresh.setRefreshing(false);
                searchItem.setVisible(true);
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Summary> call, Throwable t) {
                if (!isNetworkAvailable()) {
                    internetErrorToast.show();
                }
                call.clone().enqueue(this);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        pullToRefresh.setEnabled(true);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        recyclerAdapter.filter(s);
        return true;
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar androidToolbar = findViewById(R.id.android_toolbar);
        setSupportActionBar(androidToolbar);

        // References to views
        pullToRefresh = findViewById(R.id.pullToRefresh);
        myRecyclerView = findViewById(R.id.my_recycler_view);
        adView = findViewById(R.id.adView);
        progressBar = findViewById(R.id.progressBar);


        internetErrorToast = Toast.makeText(MainActivity.this, "Make sure you are connected to Internet!", Toast.LENGTH_SHORT);


        MobileAds.initialize(this, initializationStatus -> {

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);



        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        myRecyclerView.setLayoutManager(linearLayoutManager);

        refreshList();

        pullToRefresh.setOnRefreshListener(this::refreshList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adView.destroy();
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        pullToRefresh.setEnabled(true);
        return true;
    }
}