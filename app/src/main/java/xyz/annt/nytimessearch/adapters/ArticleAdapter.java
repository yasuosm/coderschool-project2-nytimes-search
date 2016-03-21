package xyz.annt.nytimessearch.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.annt.nytimessearch.R;
import xyz.annt.nytimessearch.models.Article;
import xyz.annt.nytimessearch.models.Image;
import xyz.annt.nytimessearch.utils.DynamicHeightImageView;

/**
 * Created by annt on 3/19/16.
 */
public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Article> articles;
    private Context context;
    private final int ARTICLE_DEFAULT = 0, ARTICLE_NO_THUMB = 1;

    // Define listener member variable
    private static OnItemClickListener listener;

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ViewHolderDefault extends RecyclerView.ViewHolder {
        @Bind(R.id.ivThumbnail) DynamicHeightImageView ivThumbnail;
        @Bind(R.id.tvHeadline) TextView tvHeadline;

        public ViewHolderDefault(final View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onItemClick(view, getLayoutPosition());
                    }
                }
            });
        }
    }

    class ViewHolderNoThumb extends RecyclerView.ViewHolder {
        @Bind(R.id.tvHeadline) TextView tvHeadline;

        public ViewHolderNoThumb(final View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onItemClick(view, getLayoutPosition());
                    }
                }
            });
        }
    }

    public ArticleAdapter(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder holder;

        // Inflate the custom layout
        switch (viewType) {
            case ARTICLE_NO_THUMB:
                View viewNoThumb = inflater.inflate(R.layout.item_article_no_thumb, parent, false);
                holder = new ViewHolderNoThumb(viewNoThumb);
                break;
            default:
                View viewDefault = inflater.inflate(R.layout.item_article_default, parent, false);
                holder = new ViewHolderDefault(viewDefault);
        }

        // Return a new holder instance
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ARTICLE_NO_THUMB:
                onBindViewHolderNoThumb((ViewHolderNoThumb) holder, position);
                break;
            default:
                onBindViewHolderDefault((ViewHolderDefault) holder, position);
        }
    }

    public void onBindViewHolderNoThumb(ViewHolderNoThumb holder, int position) {
        Article article = articles.get(position);

        holder.tvHeadline.setText(article.getHeadline());
    }

    public void onBindViewHolderDefault(ViewHolderDefault holder, int position) {
        Article article = articles.get(position);

        Image thumbnail = article.getThumbnail();
        holder.ivThumbnail.setImageResource(0);
        // Set the height ratio before loading in image into Picasso
        holder.ivThumbnail.setHeightRatio(((double) thumbnail.getHeight()) / thumbnail.getWidth());
        // Load the image into the view using Picasso
        Glide.with(context).load(thumbnail.getUrl()).placeholder(R.drawable.ic_image_black).into(holder.ivThumbnail);

        holder.tvHeadline.setText(article.getHeadline());
    }

    @Override
    public int getItemViewType(int position) {
        Article article = articles.get(position);

        if (null == article.getThumbnail()) {
            return ARTICLE_NO_THUMB;
        }

        return ARTICLE_DEFAULT;
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }
}
