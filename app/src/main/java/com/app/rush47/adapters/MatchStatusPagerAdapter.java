package com.app.rush47.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.rush47.R;
import com.app.rush47.fragments.MatchStatusFragment;

/**
 * Backs the Ongoing / Upcoming / Completed tabs on MatchListActivity -
 * three MatchStatusFragment pages, one per status, all scoped to the
 * same game/category/type the activity was opened with.
 */
public class MatchStatusPagerAdapter extends FragmentPagerAdapter {

    private final String gameId;
    private final String categoryId;
    private final String type;

    public MatchStatusPagerAdapter(@NonNull FragmentManager fm, String gameId, String categoryId, String type) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.gameId = gameId;
        this.categoryId = categoryId;
        this.type = type;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return MatchStatusFragment.newInstance(gameId, categoryId, type, "upcoming", R.string.no_upcoming_match_found);
            case 2:
                return MatchStatusFragment.newInstance(gameId, categoryId, type, "completed", R.string.no_completed_match_found);
            case 0:
            default:
                return MatchStatusFragment.newInstance(gameId, categoryId, type, "ongoing", R.string.no_live_match_found);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
