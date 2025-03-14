package com.mandiri.mandiriintern;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class BreakingNewsAdapter extends RecyclerView.Adapter<BreakingNewsAdapter.ViewHolder> {
    private List<Article> breakingNews;
    private Context context;

    public BreakingNewsAdapter(List<Article> breakingNews) {
        this.breakingNews = breakingNews != null ? breakingNews : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.breaking_news_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = breakingNews.get(position);

        // Load image using Glide
        Glide.with(context)
                .load(article.getUrlToImage())
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_error)
                .centerCrop()
                .into(holder.newsImage);

        holder.titleText.setText(article.getTitle());
        holder.sourceText.setText(article.getSource().getName());

        // Add click listener
        holder.itemView.setOnClickListener(v -> {
            // Add click animation
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        // Open article URL
                        String url = article.getUrl();
                        if (url != null && !url.isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            context.startActivity(intent);
                        }
                    })
                    .start();
        });
    }

    @Override
    public int getItemCount() {
        return breakingNews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView titleText, sourceText;

        ViewHolder(View view) {
            super(view);
            newsImage = view.findViewById(R.id.breaking_news_image);
            titleText = view.findViewById(R.id.breaking_news_title);
            sourceText = view.findViewById(R.id.breaking_news_source);
        }
    }
}