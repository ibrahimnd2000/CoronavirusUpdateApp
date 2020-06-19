package com.itechteam.android.coronaupdate;

import android.app.SearchManager;
import android.content.Context;
import androidx.core.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private static Retrofit retrofit;
    private static Gson gson;
    private RecyclerView myRecyclerView;
    private RecyclerAdapter recyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private List<Summary.Countries> countriesList;
    private ProgressBar progressBar;
    private Toolbar androidToolbar;
    private MenuItem searchItem;
    private SwipeRefreshLayout pullToRefresh;
    private static final String TAG = "MainActivity";

    public static Retrofit getRetrofit() {
        gson = new GsonBuilder()
                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.covid19api.com/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public void refreshList() {
        Covid19Api covid19Api = getRetrofit().create(Covid19Api.class);
        Call<Summary> call = covid19Api.getSummary();
        call.enqueue(new Callback<Summary>() {
            @Override
            public void onResponse(Call<Summary> call, Response<Summary> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.code());
                }
                progressBar.setVisibility(View.GONE);
                searchItem.setVisible(true);
                countriesList = response.body().getCountriesList();
                recyclerAdapter = new RecyclerAdapter(getApplicationContext(), countriesList);
                myRecyclerView.setAdapter(recyclerAdapter);
                android.text.format.DateFormat df = new android.text.format.DateFormat();
                Date date = response.body().getDate();
                Toast.makeText(MainActivity.this, "Updated on: " + df.format("dd-MM-yyyy hh:mm:ss a", date), Toast.LENGTH_SHORT).show();
                pullToRefresh.setRefreshing(false);

            }

            @Override
            public void onFailure(Call<Summary> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Sorry, we couldn't fetch the data. Make sure you're connected to Internet.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(this);
        searchItem.setVisible(false);
        SearchManager searchManager =
                (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                pullToRefresh.setEnabled(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        pullToRefresh = findViewById(R.id.pullToRefresh);
        myRecyclerView = findViewById(R.id.my_recycler_view);
        androidToolbar = findViewById(R.id.android_toolbar);
        setSupportActionBar(androidToolbar);
        progressBar = findViewById(R.id.progressBar);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        myRecyclerView.setLayoutManager(linearLayoutManager);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
                recyclerAdapter.notifyDataSetChanged();
            }
        });
        refreshList();
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