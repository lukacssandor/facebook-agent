package com.example.facebookagent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class PageActivity extends AppCompatActivity {

    TextView name, birthday, email, hometown, friendsCount;
    ImageView avatar;
    AccessTokenTracker accessTokenTracker;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        getSupportActionBar().hide();

        Intent intent = getIntent();

        name = findViewById(R.id.id_name);
        birthday = findViewById(R.id.id_birth);
        email = findViewById(R.id.id_email);
        hometown = findViewById(R.id.id_hometown);
        friendsCount = findViewById(R.id.id_friend_count);

        avatar = findViewById(R.id.id_avatar);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.d(MainActivity.TAG, oldAccessToken.toString());
                    Log.d(MainActivity.TAG, "onLogout catched");
                    disconnectFromFacebook(oldAccessToken);
                }
            }
        };

        try {
            URL avatar_picture_url = new URL("https://graph.facebook.com/" +
                    intent.getStringExtra(MainActivity.LOGIT_RESULT_ID) +
                    "/picture?width=250&height=250");

            Picasso.get()
                    .load(avatar_picture_url.toString())
                    .transform(new CropCircleTransformation())
                    .resize(250, 250)
                    .into(avatar);

            String nameStr = intent.getStringExtra(MainActivity.LOGIN_RESULT_NAME);
            SpannableStringBuilder nameSSB = new SpannableStringBuilder(nameStr);
            nameSSB.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, nameStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            name.setText(nameSSB);

            String emailStamp = "Email: ";
            String emailStr = emailStamp + intent.getStringExtra(MainActivity.LOGIN_RESULT_EMAIL);
            SpannableStringBuilder emailSSB = new SpannableStringBuilder(emailStr);
            emailSSB.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, emailStamp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            email.setText(emailSSB);

            String birthdayStamp = "Birthday: ";
            String birthdayStr = birthdayStamp + intent.getStringExtra(MainActivity.LOGIN_RESULT_BIRTH);
            SpannableStringBuilder birthdaySSB = new SpannableStringBuilder(birthdayStr);
            birthdaySSB.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, birthdayStamp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            birthday.setText(birthdaySSB);

            String hometownStamp = "Hometown: ";
            String hometownStr = hometownStamp + intent.getStringExtra(MainActivity.LOGIN_RESULT_HOMETOWN);
            SpannableStringBuilder hometownSSB = new SpannableStringBuilder(hometownStr);
            hometownSSB.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, hometownStamp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            hometown.setText(hometownSSB);

            String friendsCountStamp = "Friends: ";
            String friendsCountStr = friendsCountStamp + intent.getStringExtra(MainActivity.LOGIN_RESULT_FRIENDS);
            SpannableStringBuilder friendsCountSSB = new SpannableStringBuilder(friendsCountStr);
            friendsCountSSB.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, friendsCountStamp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            friendsCount.setText(friendsCountSSB);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        accessTokenTracker.startTracking();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.d(MainActivity.TAG, "back button pressed");
            disconnectFromFacebook(AccessToken.getCurrentAccessToken());
        }
        return super.onKeyDown(keyCode, event);
    }

    private void disconnectFromFacebook(AccessToken accessToken) {

        if (accessToken == null) {
            Log.d(MainActivity.TAG, "no access token");
            return;
        }

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Logging out...");
        mDialog.show();
        findViewById(R.id.logout_button).setVisibility(View.GONE);

        new GraphRequest(accessToken, "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                Log.d(MainActivity.TAG, "logged out");
                LoginManager.getInstance().logOut();
                accessTokenTracker.stopTracking();
                mDialog.dismiss();
                finish();
            }
        }).executeAsync();
    }
}
