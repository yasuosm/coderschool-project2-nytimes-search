package xyz.annt.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import xyz.annt.nytimessearch.R;
import xyz.annt.nytimessearch.adapters.ArticleAdapter;
import xyz.annt.nytimessearch.fragments.ArticleSearchFragment;
import xyz.annt.nytimessearch.listeners.EndlessRecyclerViewScrollListener;
import xyz.annt.nytimessearch.models.Article;
import xyz.annt.nytimessearch.network.ArticleClient;

public class ArticlesActivity extends AppCompatActivity
        implements ArticleSearchFragment.ArticleSearchFragmentListener {
    private static final String TAG = "ArticlesActivity";
    private StaggeredGridLayoutManager layoutManager;
    private ArrayList<Article> articles;
    private ArticleAdapter articleAdapter;
    private ArticleClient client;

    // Search settings
    private String searchQuery = "";
    private long searchBeginTimestamp = 0;
    private String searchSort = "Newest";
    private ArrayList<String> searchNewsDesk;

    @Bind(R.id.rvArticles) RecyclerView rvArticles;
    @Bind(R.id.tvMessage) TextView tvMessage;
    @Bind(R.id.tvErrorMessage) TextView tvErrorMessage;
    @Bind(R.id.pbLoading) ProgressBar pbLoading;

    @BindString(R.string.network_is_not_available) String stringNoNetwork;
    @BindString(R.string.an_error_has_occurred) String stringErrorOccurred;
    @BindString(R.string.no_articles_found) String stringNotFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        articles = new ArrayList<>();
        articleAdapter = new ArticleAdapter(articles);
        client = new ArticleClient();

        layoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        rvArticles.setAdapter(articleAdapter);
        rvArticles.setLayoutManager(layoutManager);

        setListeners();
    }

    public void setListeners() {
        rvArticles.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMore(page);
            }
        });

        articleAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Article article = articles.get(position);
                Intent intent = new Intent(ArticlesActivity.this, ArticleActivity.class);
                intent.putExtra("article", article);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_articles, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform query
                searchQuery = query;
                searchArticles();

                // Avoid issues with keyboard
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSearchFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showSearchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ArticleSearchFragment fragment = new ArticleSearchFragment();

        Bundle args = new Bundle();
        args.putLong("searchBeginTimestamp", searchBeginTimestamp);
        args.putString("searchSort", searchSort);
        args.putStringArrayList("searchNewsDesk", searchNewsDesk);
        fragment.setArguments(args);

        fragment.show(fragmentManager, "fragment_article_search");
    }

    public void searchArticles() {
        articles.clear();
        articleAdapter.notifyDataSetChanged();

        resetViewsState();

        if (!isNetworkAvailable()) {
            tvErrorMessage.setText(stringNoNetwork);
            tvErrorMessage.setVisibility(View.VISIBLE);
            return;
        }

        RequestParams searchParams = getSearchParams();

        pbLoading.setVisibility(View.VISIBLE);
        client.searchArticles(searchParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, response.toString());
                pbLoading.setVisibility(View.INVISIBLE);

                JSONObject responseJSON = response.optJSONObject("response");
                if (null != responseJSON) {
                    JSONArray docsJSONArray = responseJSON.optJSONArray("docs");
                    if (null != docsJSONArray && 0 < docsJSONArray.length()) {
                        articles.addAll(Article.fromJSONArray(docsJSONArray));
                        articleAdapter.notifyDataSetChanged();
                        return;
                    } else {
                        tvMessage.setText(stringNotFound);
                        tvMessage.setVisibility(View.VISIBLE);
                        return;
                    }
                }

                tvErrorMessage.setText(stringErrorOccurred);
                tvErrorMessage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.w(TAG, errorResponse.toString());
                pbLoading.setVisibility(View.INVISIBLE);

                tvErrorMessage.setText(stringErrorOccurred);
                tvErrorMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    public void loadMore(int page) {
        RequestParams searchParams = getSearchParams();
        searchParams.put("page", page);

        articleAdapter.notifyDataSetChanged();
        client.searchArticles(searchParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, response.toString());
                JSONObject responseJSON = response.optJSONObject("response");
                if (null != responseJSON) {
                    JSONArray docsJSONArray = responseJSON.optJSONArray("docs");
                    if (null != docsJSONArray) {
                        articles.addAll(Article.fromJSONArray(docsJSONArray));
                        int curSize = articleAdapter.getItemCount();
                        articleAdapter.notifyItemRangeInserted(curSize, articles.size() - 1);
                    }
                }
                // Ignore errors
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.w(TAG, errorResponse.toString());
                // Ignore errors
            }
        });
    }

    private void resetViewsState() {
        tvMessage.setText("");
        tvMessage.setVisibility(View.INVISIBLE);
        tvErrorMessage.setText("");
        tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private RequestParams getSearchParams() {
        RequestParams params = new RequestParams();

        params.put("q", searchQuery);

        if (0 < searchBeginTimestamp) {
            params.put("begin_date", getDateString(searchBeginTimestamp, "yyyyMMdd"));
        }

        params.put("sort", searchSort.toLowerCase());

        if (null != searchNewsDesk && 0 < searchNewsDesk.size()) {
            String fq = "news_desk:(";
            for (int i = 0; i < searchNewsDesk.size(); i++) {
                if (i > 0) {
                    fq += " ";
                }
                fq += "\"" + searchNewsDesk.get(i) + "\"";
            }
            fq += ")";
            params.put("fq", fq);
        }

        return params;
    }

    private String getDateString(long timeStamp, String format){
        if (timeStamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = (new Date(timeStamp));
            return sdf.format(date);
        }

        return "";
    }

    @Override
    public void onSaveSettings(long searchBeginTimestamp, String searchSort, ArrayList<String> searchNewsDesk) {
        this.searchBeginTimestamp = searchBeginTimestamp;
        this.searchSort = searchSort;
        this.searchNewsDesk = searchNewsDesk;

        // Trigger search new results
        if (!"".equals(searchQuery)) {
            searchArticles();
        }
    }
}
