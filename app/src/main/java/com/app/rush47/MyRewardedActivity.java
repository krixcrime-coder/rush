package com.app.rush47;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.rush47.utils.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * My Rewards - recreated from the old app's activity_my_rewarded.xml
 * (same ids throughout: backfrommyreff, refnumber, earnings, reftv,
 * refll). Data comes from my_rewards.php, which reads the
 * `watch_earn` table (the only reward source currently wired up).
 */
public class MyRewardedActivity extends BaseActivity {

    private TextView refNumber, earnings, reftv;
    private LinearLayout refList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rewarded);

        ImageView back = findViewById(R.id.backfrommyreff);
        refNumber = findViewById(R.id.refnumber);
        earnings = findViewById(R.id.earnings);
        reftv = findViewById(R.id.reftv);
        refList = findViewById(R.id.refll);

        back.setOnClickListener(v -> finish());

        loadRewards();
    }

    private void loadRewards() {
        String apiBase = getString(R.string.api);
        String url = apiBase + "my_rewards.php";
        UserLocalStore userLocalStore = new UserLocalStore(getApplicationContext());

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
        } catch (Exception ignored) { }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                response -> {
                    try {
                        if (!response.getString("status").equals("true")) return;
                        JSONObject data = response.getJSONObject("message");
                        refNumber.setText(data.optString("total_rewards", "0"));
                        earnings.setText("₹" + data.optString("total_earnings", "0.00"));

                        JSONArray rows = data.getJSONArray("rewards");
                        refList.removeAllViews();
                        if (rows.length() == 0) {
                            reftv.setVisibility(View.VISIBLE);
                        } else {
                            reftv.setVisibility(View.GONE);
                            for (int i = 0; i < rows.length(); i++) {
                                addRow(rows.getJSONObject(i));
                            }
                        }
                    } catch (Exception ignored) { }
                },
                error -> reftv.setVisibility(View.VISIBLE));
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void addRow(JSONObject row) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_reward_row, refList, false);
        ((TextView) item.findViewById(R.id.row_date)).setText(row.optString("date", "-"));
        ((TextView) item.findViewById(R.id.row_rewards)).setText(row.optString("rewards", "0"));
        ((TextView) item.findViewById(R.id.row_earnings)).setText("₹" + row.optString("earnings", "0.00"));
        refList.addView(item);
    }
}
