package com.app.rush47.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.MatchDetailActivity;
import com.app.rush47.R;
import com.app.rush47.SelectPositionActivity;
import com.app.rush47.adapters.TournamentAdapter;
import com.app.rush47.models.Tournament;
import com.app.rush47.utils.NetworkErrorHelper;
import com.app.rush47.utils.UserLocalStore;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One page of the Ongoing/Upcoming/Completed ViewPager on MatchListActivity
 * (mirrors the original app's ongoing_home.xml / upcoming_home.xml /
 * result_home.xml - each is its own status-filtered fetch of
 * tournaments.php, with a shimmer skeleton while it loads).
 */
public class MatchStatusFragment extends Fragment {

    private static final String ARG_GAME_ID = "game_id";
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_TYPE = "type";
    private static final String ARG_STATUS = "status";
    private static final String ARG_EMPTY_TEXT_RES = "empty_text_res";

    private static final int REQUEST_JOIN = 301;

    private ShimmerFrameLayout shimmer;
    private SwipeRefreshLayout pullToRefresh;
    private RecyclerView recyclerView;
    private TextView emptyText;

    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;

    public static MatchStatusFragment newInstance(String gameId, String categoryId, String type,
                                                    String status, int emptyTextRes) {
        MatchStatusFragment fragment = new MatchStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GAME_ID, gameId);
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_STATUS, status);
        args.putInt(ARG_EMPTY_TEXT_RES, emptyTextRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_match_status, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestQueue = Volley.newRequestQueue(requireContext().getApplicationContext());
        userLocalStore = new UserLocalStore(requireContext());
        apiBase = getString(R.string.api);

        shimmer = view.findViewById(R.id.matchStatusShimmer);
        pullToRefresh = view.findViewById(R.id.matchStatusPullToRefresh);
        recyclerView = view.findViewById(R.id.matchStatusRecyclerView);
        emptyText = view.findViewById(R.id.matchStatusEmptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        emptyText.setText(getArguments() != null ? getArguments().getInt(ARG_EMPTY_TEXT_RES) : R.string.no_matches_found);
        pullToRefresh.setOnRefreshListener(this::fetchMatches);

        shimmer.startShimmer();
        fetchMatches();
    }

    public void fetchMatches() {
        if (getArguments() == null || requestQueue == null) return;

        String url = apiBase + "tournaments.php";
        JSONObject params = new JSONObject();
        try {
            String categoryId = getArguments().getString(ARG_CATEGORY_ID);
            String gameId = getArguments().getString(ARG_GAME_ID);
            if (!TextUtils.isEmpty(categoryId)) {
                params.put("category_id", categoryId);
            } else {
                params.put("game_id", gameId);
            }
            params.put("type", getArguments().getString(ARG_TYPE));
            params.put("status", getArguments().getString(ARG_STATUS));
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, params,
                this::handleResponse,
                error -> {
                    finishLoading();
                    if (isAdded()) {
                        Toast.makeText(requireContext(), NetworkErrorHelper.describe(error), Toast.LENGTH_SHORT).show();
                    }
                });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void handleResponse(JSONObject response) {
        List<Tournament> tournaments = new ArrayList<>();
        try {
            if (TextUtils.equals(response.getString("status"), "true")) {
                JSONArray arr = response.getJSONObject("message").optJSONArray("tournaments");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        tournaments.add(Tournament.fromJson(arr.getJSONObject(i)));
                    }
                }
            }
        } catch (JSONException ignored) {
        }
        renderMatches(tournaments);
    }

    private void renderMatches(List<Tournament> tournaments) {
        if (!isAdded()) return;
        finishLoading();
        emptyText.setVisibility(tournaments.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setAdapter(new TournamentAdapter(tournaments, true, new TournamentAdapter.OnTournamentClickListener() {
            @Override
            public void onCardClick(Tournament tournament) {
                Intent intent = new Intent(requireContext(), MatchDetailActivity.class);
                intent.putExtra(MatchDetailActivity.EXTRA_TOURNAMENT_ID, tournament.getTournamentId());
                startActivity(intent);
            }

            @Override
            public void onJoinClick(Tournament tournament) {
                Intent intent = new Intent(requireContext(), SelectPositionActivity.class);
                intent.putExtra(SelectPositionActivity.EXTRA_TOURNAMENT_ID, tournament.getTournamentId());
                intent.putExtra(SelectPositionActivity.EXTRA_TITLE, tournament.getTitle());
                intent.putExtra(SelectPositionActivity.EXTRA_ENTRY_FEE, tournament.getEntryFee());
                startActivityForResult(intent, REQUEST_JOIN);
            }
        }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_JOIN && resultCode == android.app.Activity.RESULT_OK) {
            fetchMatches();
        }
    }

    private void finishLoading() {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        pullToRefresh.setRefreshing(false);
    }
}
