package com.app.rush47.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.app.rush47.MyRewardedActivity;
import com.app.rush47.R;
import com.app.rush47.ReferAndEarnActivity;
import com.app.rush47.TopPlayerActivity;
import com.app.rush47.WatchAndEarnActivity;
import com.app.rush47.utils.UserLocalStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Earn tab - recreated from the old app's earn_home.xml exactly:
 * header (title, share icon, coin-balance chip) + a vertical list of
 * banners loaded from earn_banners.php, same as the old app's
 * "banerll" LinearLayout that it filled in at runtime.
 *
 * Each banner's slider_link_type/slider_link (from the `slider`
 * table) decides what tapping it does:
 *   - "app"  + slider_link "refer_and_earn" / "watch_and_earn" /
 *              "top_player" / "my_rewarded"  -> opens that screen
 *   - "web"  -> opens slider_link in the browser
 *   - anything else / empty                  -> not clickable
 */
public class EarnFragment extends Fragment {

    private TextView balanceText;
    private LinearLayout bannerContainer;
    private RequestQueue requestQueue;
    private UserLocalStore userLocalStore;
    private String apiBase;
    private String referralCode = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_earn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestQueue = Volley.newRequestQueue(requireContext().getApplicationContext());
        userLocalStore = new UserLocalStore(requireContext());
        apiBase = getString(R.string.api);

        balanceText = view.findViewById(R.id.balinearn);
        bannerContainer = view.findViewById(R.id.banerll);

        view.findViewById(R.id.share).setOnClickListener(v -> shareReferralCode());

        fetchCoinBalance();
        loadBanners();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCoinBalance(); // wallet balance may have changed (e.g. after Watch & Earn)
    }

    private void fetchCoinBalance() {
        if (userLocalStore.getLoggedInUser() == null) return;
        String url = apiBase + "earn.php";

        JSONObject params = new JSONObject();
        try {
            params.put("member_id", userLocalStore.getLoggedInUser().getMemberid());
            params.put("api_token", userLocalStore.getLoggedInUser().getToken());
        } catch (JSONException ignored) { }

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                response -> {
                    try {
                        if (TextUtils.equals(response.getString("status"), "true")) {
                            JSONObject data = response.getJSONObject("message");
                            balanceText.setText(data.optString("wallet_balance", "0"));
                            referralCode = data.optString("referral_code", "");
                        }
                    } catch (JSONException ignored) { }
                },
                error -> { });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void loadBanners() {
        String url = apiBase + "earn_banners.php";

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, new JSONObject(),
                response -> {
                    try {
                        JSONArray banners = response.getJSONObject("message").getJSONArray("banners");
                        bannerContainer.removeAllViews();
                        for (int i = 0; i < banners.length(); i++) {
                            addBannerView(banners.getJSONObject(i));
                        }
                    } catch (JSONException ignored) { }
                },
                error -> { });

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void addBannerView(JSONObject banner) {
        if (getContext() == null) return;

        View bannerView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_earn_banner, bannerContainer, false);
        ImageView imageView = bannerView.findViewById(R.id.banner_image);

        String imageUrl = banner.optString("slider_image", "");
        Glide.with(this).load(imageUrl).into(imageView);

        String linkType = banner.optString("slider_link_type", "");
        String link = banner.optString("slider_link", "");

        if (TextUtils.equals(linkType, "app")) {
            bannerView.setOnClickListener(v -> openAppScreen(link));
        } else if (TextUtils.equals(linkType, "web") && !TextUtils.isEmpty(link)) {
            bannerView.setOnClickListener(v ->
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))));
        }

        bannerContainer.addView(bannerView);
    }

    private void openAppScreen(String link) {
        if (getContext() == null) return;
        Class<?> target;
        switch (link) {
            case "refer_and_earn":
                target = ReferAndEarnActivity.class;
                break;
            case "watch_and_earn":
                target = WatchAndEarnActivity.class;
                break;
            case "top_player":
                target = TopPlayerActivity.class;
                break;
            case "my_rewarded":
                target = MyRewardedActivity.class;
                break;
            default:
                return; // unknown app link - not clickable
        }
        startActivity(new Intent(getContext(), target));
    }

    private void shareReferralCode() {
        if (TextUtils.isEmpty(referralCode)) {
            Toast.makeText(requireContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Join Rush47 and use my referral code " + referralCode + " to get a signup bonus!");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.refer_and_earn)));
    }
}
