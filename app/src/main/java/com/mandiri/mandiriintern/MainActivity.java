package com.mandiri.mandiriintern;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String BASE_URL = "https://newsapi.org/v2/";
    private static final String API_KEY = "b5fe3ccde68845e7999fb225c526b1d2"; // Keep API key in MainActivity as requested
    private static final int PAGE_SIZE = 10;  // Number of news items per page
    private int currentPage = 1;  // Track current page number
    private boolean isLoading = false;  // Track if we're currently loading data

    private RecyclerView recyclerView;
    private RecyclerView breakingNewsRecyclerView;
    private List<Article> articleList = new ArrayList<>();
    private List<Article> breakingNewsList = new ArrayList<>();
    private NewsRecyclerAdapter adapter;
    private BreakingNewsAdapter breakingNewsAdapter;
    private LinearProgressIndicator progressIndicator;
    private NewsApiService apiService;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7;
    private SearchView searchView;
    private ImageView imgLogo, imgProfile;
    private TextView txProfile;
    private Button btnViewMore;
    private String currentCategory = null;
    private Button currentSelectedButton = null;

    private void initializeApiService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("X-Api-Key", API_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(NewsApiService.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupRecyclerViews();
        initializeApiService();
        setupSearchView();
        fetchBreakingNews();
        fetchHeadlines();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.news_recycler_view);
        breakingNewsRecyclerView = findViewById(R.id.breaking_news_recycler_view);
        progressIndicator = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);
        btnViewMore = findViewById(R.id.btn_view_more);
        btnViewMore.setOnClickListener(v -> loadMoreNews());

        // Logo and Profile
        imgLogo = findViewById(R.id.img_logo);
        imgProfile = findViewById(R.id.img_profile);
        txProfile = findViewById(R.id.tx_profile);

        // Initialize buttons with correct IDs from XML
        btn1 = findViewById(R.id.btn_1);  // GENERAL
        btn2 = findViewById(R.id.btn_2);  // BUSINESS
        btn3 = findViewById(R.id.btn_3);  // SPORTS
        btn4 = findViewById(R.id.btn_4);  // TECHNOLOGY
        btn5 = findViewById(R.id.btn_5);  // HEALTH
        btn6 = findViewById(R.id.btn_6);  // ENTERTAINMENT
        btn7 = findViewById(R.id.btn_7);  // SCIENCE

        // Set click listeners for all buttons
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);

        // Set the default selected button
        currentSelectedButton = btn1;
        currentSelectedButton.setBackgroundColor(getResources().getColor(R.color.my_secondary));
    }

    private void setupRecyclerViews() {
        // Setup main news RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsRecyclerAdapter(articleList);
        recyclerView.setAdapter(adapter);

        // Setup breaking news RecyclerView
        breakingNewsAdapter = new BreakingNewsAdapter(breakingNewsList);
        breakingNewsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        breakingNewsRecyclerView.setAdapter(breakingNewsAdapter);

        // Initially hide the View More button
        btnViewMore.setVisibility(View.GONE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading) {  // Only check if we're not currently loading
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        // Show View More button only when reaching the end of the list
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                                firstVisibleItemPosition >= 0 &&
                                totalItemCount >= PAGE_SIZE) {
                            btnViewMore.setVisibility(View.VISIBLE);
                        } else {
                            btnViewMore.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    // Update loadMoreNews to use current category
    private void loadMoreNews() {
        if (isLoading) return;

        isLoading = true;
        btnViewMore.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.VISIBLE);

        currentPage++;

        try {
            Call<NewsResponse> call = apiService.getTopHeadlines("us", currentCategory, null, currentPage, PAGE_SIZE);

            call.enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    progressIndicator.setVisibility(View.INVISIBLE);
                    isLoading = false;

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> newArticles = response.body().getArticles();
                            if (newArticles != null && !newArticles.isEmpty()) {
                                int oldSize = articleList.size();
                                articleList.addAll(newArticles);
                                adapter.notifyItemRangeInserted(oldSize, newArticles.size());

                                // Show View More button again if we successfully loaded more news
                                btnViewMore.setVisibility(View.VISIBLE);
                            } else {
                                currentPage--;
                                Toast.makeText(MainActivity.this, "No more news available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            currentPage--;
                            Toast.makeText(MainActivity.this, "Failed to load more news", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        currentPage--;
                        Log.e("ERROR", "Error processing response", e);
                        Toast.makeText(MainActivity.this, "Error loading news", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<NewsResponse> call, Throwable t) {
                    progressIndicator.setVisibility(View.INVISIBLE);
                    isLoading = false;
                    currentPage--;
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "Network error", t);
                }
            });
        } catch (Exception e) {
            isLoading = false;
            progressIndicator.setVisibility(View.INVISIBLE);
            Log.e("ERROR", "Error making API call", e);
            Toast.makeText(MainActivity.this, "Error loading news", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearchView() {
        // Store original size
        final int originalWidth = searchView.getLayoutParams().width;

        searchView.setIconified(true);

        searchView.setOnSearchClickListener(v -> {
            // Expand to fill available space
            searchView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
        });

        searchView.setOnCloseListener(() -> {
            // Return to original size
            searchView.setLayoutParams(new LinearLayout.LayoutParams(
                    originalWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAllNews(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    // handle searching all news
    private void searchAllNews(String query) {
        progressIndicator.setVisibility(View.VISIBLE);

        Call<NewsResponse> call = apiService.searchAllNews(query, "en");

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    articleList.clear();
                    List<Article> articles = response.body().getArticles();
                    if (articles != null && !articles.isEmpty()) {
                        articleList.addAll(articles);
                        adapter.notifyDataSetChanged();
                        // Add this line to scroll to top
                        recyclerView.smoothScrollToPosition(0);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "No results found for: " + query,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API_ERROR", "Response Code: " + response.code());
                    Toast.makeText(MainActivity.this,
                            "Failed to fetch news",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                Log.e("API_ERROR", "Error: ", t);
                Toast.makeText(MainActivity.this,
                        "Network error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHeadlines() {
        progressIndicator.setVisibility(View.VISIBLE);
        Call<NewsResponse> call = apiService.getTopHeadlines("us", null, null, 1, PAGE_SIZE);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    articleList.clear();
                    List<Article> articles = response.body().getArticles();
                    if (articles != null && !articles.isEmpty()) {
                        articleList.addAll(articles);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "No headlines available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API_ERROR", "Response Code: " + response.code());
                    Toast.makeText(MainActivity.this, "Failed to fetch headlines", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                Log.e("API_ERROR", "Error: ", t);
                Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBreakingNews() {
        progressIndicator.setVisibility(View.VISIBLE);
        Call<NewsResponse> call = apiService.getTopHeadlines("us", null, null, 1, 5); // Fetch top 5 breaking news

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    breakingNewsList.clear();
                    List<Article> articles = response.body().getArticles();
                    if (articles != null && !articles.isEmpty()) {
                        breakingNewsList.addAll(articles);
                        breakingNewsAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e("API_ERROR", "Breaking news response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                Log.e("API_ERROR", "Breaking news error: ", t);
                Toast.makeText(MainActivity.this, "Failed to load breaking news", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        // Reset previous button color
        if (currentSelectedButton != null) {
            currentSelectedButton.setBackgroundColor(getResources().getColor(R.color.my_primary));
        }

        // Set new button color and update currentSelectedButton
        v.setBackgroundColor(getResources().getColor(R.color.my_secondary));
        currentSelectedButton = (Button) v;

        int id = v.getId();

        // Set the current category based on which button was clicked
        if (id == R.id.btn_1) currentCategory = "general";
        else if (id == R.id.btn_2) currentCategory = "business";
        else if (id == R.id.btn_3) currentCategory = "sports";
        else if (id == R.id.btn_4) currentCategory = "technology";
        else if (id == R.id.btn_5) currentCategory = "health";
        else if (id == R.id.btn_6) currentCategory = "entertainment";
        else if (id == R.id.btn_7) currentCategory = "science";
        else currentCategory = null; // Default case

        // Reset to first page and fetch news for the selected category
        currentPage = 1;
        getNews(currentCategory, null);
        recyclerView.smoothScrollToPosition(0);
    }

    private void getNews(String category, String query) {
        progressIndicator.setVisibility(View.VISIBLE);
        Call<NewsResponse> call = apiService.getTopHeadlines("us", category, query, 1, PAGE_SIZE);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    articleList.clear();
                    List<Article> articles = response.body().getArticles();
                    if (articles != null && !articles.isEmpty()) {
                        articleList.addAll(articles);
                        adapter.notifyDataSetChanged();
                        Log.d("API_DEBUG", "Found " + articles.size() + " articles");
                    } else {
                        String message = query != null
                                ? "No news found for: " + query
                                : "No news found for category: " + category;
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.d("API_DEBUG", "No articles found");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Failed to fetch news", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", "Request failed", t);
            }
        });
    }
}