package xyz.annt.nytimessearch.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by annt on 3/19/16.
 */
public class Article implements Parcelable {
    String webUrl;
    Image thumbnail;
    String headline;

    public Article() {}

    public Article(String webUrl, Image thumbnail, String headline) {
        this.webUrl = webUrl;
        this.thumbnail = thumbnail;
        this.headline = headline;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public static Article fromJSONObject(JSONObject object) {
        Article article = new Article();

        article.setWebUrl(object.optString("web_url"));

        JSONObject headlineJSON = object.optJSONObject("headline");
        if (null != headlineJSON) {
            article.setHeadline(headlineJSON.optString("main"));
        }

        JSONArray multimediaJSONArray = object.optJSONArray("multimedia");
        if (null != multimediaJSONArray) {
            for (int i = 0; i < multimediaJSONArray.length(); i++) {
                JSONObject multimediaJSON = multimediaJSONArray.optJSONObject(i);
                if (null != multimediaJSON) {
                    String subtype = multimediaJSON.optString("subtype");
                    if (null != subtype && subtype.equals("thumbnail")) {
                        Image thumbnail = new Image();
                        thumbnail.setUrl("http://nytimes.com/" + multimediaJSON.optString("url"));
                        thumbnail.setWidth(multimediaJSON.optInt("width"));
                        thumbnail.setHeight(multimediaJSON.optInt("height"));
                        article.setThumbnail(thumbnail);
                        break;
                    }
                }
            }
        }

        return article;
    }

    public static ArrayList<Article> fromJSONArray(JSONArray array) {
        ArrayList<Article> articles = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (null != object) {
                Article article = Article.fromJSONObject(object);
                articles.add(article);
            }
        }

        return articles;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.webUrl);
        dest.writeParcelable(this.thumbnail, flags);
        dest.writeString(this.headline);
    }

    protected Article(Parcel in) {
        this.webUrl = in.readString();
        this.thumbnail = in.readParcelable(Image.class.getClassLoader());
        this.headline = in.readString();
    }

    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel source) {
            return new Article(source);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };
}
