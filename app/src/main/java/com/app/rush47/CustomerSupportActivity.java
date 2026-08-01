package com.app.rush47;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Customer Support - a static contact-info page, same layout the old
 * app used (address / phone+call+whatsapp / email / instagram / street
 * / response time), each row separated by a thin divider. All values
 * come from customer_support.php so they're editable from the admin
 * panel without an app update.
 */
public class CustomerSupportActivity extends BaseActivity {

    private String phone = "";
    private String whatsapp = "";
    private String email = "";
    private String instagramHandle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);

        ImageView back = findViewById(R.id.backfromsupport);
        back.setOnClickListener(v -> finish());

        TextView addressText = findViewById(R.id.support_address);
        TextView phoneText = findViewById(R.id.support_phone);
        TextView emailText = findViewById(R.id.support_email);
        TextView instagramText = findViewById(R.id.support_instagram);
        TextView streetText = findViewById(R.id.support_street);
        TextView timeText = findViewById(R.id.support_time);

        ImageView callIcon = findViewById(R.id.support_call_icon);
        ImageView whatsappIcon = findViewById(R.id.support_whatsapp_icon);
        ImageView emailIcon = findViewById(R.id.support_email_icon);
        ImageView instagramIcon = findViewById(R.id.support_instagram_icon);

        callIcon.setOnClickListener(v -> {
            if (!phone.isEmpty()) startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
        });
        whatsappIcon.setOnClickListener(v -> {
            if (!whatsapp.isEmpty()) {
                String number = whatsapp.replaceAll("[^0-9]", "");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + number)));
            }
        });
        emailIcon.setOnClickListener(v -> {
            if (!email.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                startActivity(intent);
            }
        });
        instagramIcon.setOnClickListener(v -> {
            if (!instagramHandle.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://instagram.com/" + instagramHandle)));
            }
        });

        loadSupportInfo(addressText, phoneText, emailText, instagramText, streetText, timeText);
    }

    private void loadSupportInfo(TextView addressText, TextView phoneText, TextView emailText,
                                  TextView instagramText, TextView streetText, TextView timeText) {
        String apiBase = getString(R.string.api);
        String url = apiBase + "customer_support.php";

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, new JSONObject(),
                response -> {
                    try {
                        JSONObject data = response.getJSONObject("message");
                        addressText.setText("Address : " + data.optString("address", "-"));

                        phone = data.optString("phone", "");
                        whatsapp = data.optString("whatsapp", phone);
                        phoneText.setText("Phone : " + phone);

                        email = data.optString("email", "");
                        emailText.setText("Email : " + email);

                        instagramHandle = data.optString("instagram", "");
                        instagramText.setText("Instagram : " + instagramHandle);

                        streetText.setText("Street : " + data.optString("street", "-"));
                        timeText.setText("Time : " + data.optString("response_time", "-"));
                    } catch (Exception ignored) { }
                },
                error -> { });
        Volley.newRequestQueue(getApplicationContext()).add(request);
    }
}
