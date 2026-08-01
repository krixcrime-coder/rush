package com.app.rush47;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.app.rush47.utils.LocaleHelper;

/**
 * Every activity in the app extends this instead of AppCompatActivity
 * directly. This is the actual fix for language switching not working:
 * LocaleHelper.setLocale() was saving the choice correctly, but no
 * activity was ever wrapping its context with LocaleHelper.wrap(), so
 * the saved language was never applied. Now every screen picks it up.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
