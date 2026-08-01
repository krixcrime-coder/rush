package com.app.rush47;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.utils.NetworkErrorHelper;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Match Result - winner + full ranked table for a completed match.
 * Recreated from the old app's Match Result screen: banner, match
 * summary cards, a "Winner" card (rank 1 only), then the full
 * "Match Result" table with everyone's rank/kills/winning.
 *
 * Launch with EXTRA_MATCH_ID and (optionally) EXTRA_BANNER_URL /
 * EXTRA_MATCH_NAME so the header can show instantly while the
 * results load.
 */
public class MatchResultActivity extends BaseActivity {

    public static final String EXTRA_MATCH_ID = "MATCH_ID";
    public static final String EXTRA_BANNER_URL = "BANNER_URL";
    public static final String EXTRA_MATCH_NAME = "MATCH_NAME";

    private ProgressBar loadingSpinner;
    private NestedScrollView scrollView;
    private ImageView bannerImage;
    private TextView matchName, organisedOn, winningPrize, perKill, entryFee,
            winnerRank, winnerName, winnerKills, winnerWinning, resultsNotOutYet;
    private View winnerSection;
    private LinearLayout resultTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_result);

        String matchId = getIntent().getStringExtra(EXTRA_MATCH_ID);
        String bannerUrl = getIntent().getStringExtra(EXTRA_BANNER_URL);
        String initialName = getIntent().getStringExtra(EXTRA_MATCH_NAME);

        findViewById(R.id.backFromMatchResult).setOnClickListener(v -> finish());

        loadingSpinner = findViewById(R.id.resultLoadingSpinner);
        scrollView = findViewById(R.id.resultScrollView);
        bannerImage = findViewById(R.id.resultBannerImage);
        matchName = findViewById(R.id.resultMatchName);
        organisedOn = findViewById(R.id.resultOrganisedOn);
        winningPrize = findViewById(R.id.resultWinningPrize);
        perKill = findViewById(R.id.resultPerKill);
        entryFee = findViewById(R.id.resultEntryFee);
        winnerSection = findViewById(R.id.winnerSection);
        winnerRank = findViewById(R.id.winnerRank);
        winnerName = findViewById(R.id.winnerName);
        winnerKills = findViewById(R.id.winnerKills);
        winnerWinning = findViewById(R.id.winnerWinning);
        resultTable = findViewById(R.id.resultTable);
        resultsNotOutYet = findViewById(R.id.resultsNotOutYet);

        if (!TextUtils.isEmpty(initialName)) matchName.setText(initialName);
        if (!TextUtils.isEmpty(bannerUrl)) {
            Glide.with(this).load(bannerUrl).placeholder(R.drawable.battlemanialogo).into(bannerImage);
        }

        fetchResult(matchId);
    }

    private void fetchResult(String matchId) {
        String apiBase = getString(R.string.api);
        String url = apiBase + "match_result.php";
        JSONObject params = new JSONObject();
        try {
            params.put("m_id", matchId);
        } catch (Exception ignored) { }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                this::render,
                error -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast(NetworkErrorHelper.describe(error));
                });
        request.setShouldCache(false);
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void render(JSONObject response) {
        loadingSpinner.setVisibility(View.GONE);
        try {
            if (!TextUtils.equals(response.getString("status"), "true")) {
                Toast(response.optString("message", "Could not load result."));
                return;
            }
            scrollView.setVisibility(View.VISIBLE);
            JSONObject data = response.getJSONObject("message");

            matchName.setText(data.optString("match_name", ""));
            organisedOn.setText("Organised on " + data.optString("match_time", "-"));
            winningPrize.setText("Winning Prize : \u20B9" + data.optString("win_prize", "0"));
            perKill.setText("Per Kill : \u20B9" + data.optString("per_kill", "0"));
            entryFee.setText("Entry Fee : \u20B9" + data.optString("entry_fee", "0"));

            JSONObject winner = data.optJSONObject("winner");
            if (winner != null) {
                winnerSection.setVisibility(View.VISIBLE);
                winnerRank.setText(winner.optString("rank", "1"));
                winnerName.setText(winner.optString("user_name", ""));
                winnerKills.setText(winner.optString("killed", "0"));
                winnerWinning.setText(winner.optString("total_win", "0"));
            }

            JSONArray results = data.optJSONArray("results");
            resultTable.removeAllViews();
            if (results == null || results.length() == 0) {
                resultsNotOutYet.setVisibility(View.VISIBLE);
            } else {
                resultsNotOutYet.setVisibility(View.GONE);
                for (int i = 0; i < results.length(); i++) {
                    addResultRow(results.getJSONObject(i));
                }
            }
        } catch (Exception e) {
            Toast("Something went wrong showing this result.");
        }
    }

    private void addResultRow(JSONObject row) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_match_result_row, resultTable, false);
        ((TextView) item.findViewById(R.id.rowRank)).setText(row.optString("rank", "-"));
        ((TextView) item.findViewById(R.id.rowPlayerName)).setText(row.optString("user_name", ""));
        ((TextView) item.findViewById(R.id.rowKills)).setText(row.optString("killed", "0"));
        ((TextView) item.findViewById(R.id.rowWinning)).setText(row.optString("total_win", "0"));
        resultTable.addView(item);
    }

    private void Toast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
