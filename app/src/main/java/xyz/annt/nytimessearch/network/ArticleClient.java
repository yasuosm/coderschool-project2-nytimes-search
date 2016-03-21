package xyz.annt.nytimessearch.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by annt on 3/18/16.
 */
public class ArticleClient {
    private static final String ARTICLE_SEARCH_API_URI = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
    private static final String ARTICLE_SEARCH_API_KEY = "cd397f92c9e19271f233825e99f2a9fb:7:74742640";
    private AsyncHttpClient client;

    public ArticleClient() {
        this.client = new AsyncHttpClient();
    }

    public void searchArticles(RequestParams params, JsonHttpResponseHandler handler) {
        String url = ARTICLE_SEARCH_API_URI;
        params.put("api-key", ARTICLE_SEARCH_API_KEY);

        client.get(url, params, handler);
    }
}
