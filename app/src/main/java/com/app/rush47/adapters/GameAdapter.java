package com.app.rush47.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.rush47.R;
import com.app.rush47.models.Game;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Grid item for the Play tab's game list - rebuilt to match the old
 * app's newallgamedata.xml exactly: a 90dp centerCrop image with a
 * name strip below it, no match-count badge on top of the icon (the
 * old design never had one there), and the app's logo_space image
 * shown while the real icon is loading/if it fails - same as the old
 * app's GameAdapter, which used it as the Picasso placeholder.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    /** Implement this in the fragment/activity to react to a game tap. */
    public interface OnGameClickListener {
        void onGameClick(Game game);
    }

    private final List<Game> games;
    private final OnGameClickListener listener;

    public GameAdapter(List<Game> games, OnGameClickListener listener) {
        this.games = games;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_card, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.gameName.setText(game.getName());

        Glide.with(holder.itemView.getContext())
                .load(game.getBannerUrl())
                .placeholder(R.drawable.logo_space)
                .error(R.drawable.logo_space)
                .into(holder.gameImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGameClick(game);
            }
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameImage;
        TextView gameName;

        GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameImage = itemView.findViewById(R.id.gameivnew);
            gameName = itemView.findViewById(R.id.game_name);
        }
    }
}
