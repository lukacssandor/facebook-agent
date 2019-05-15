package com.example.facebookagent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final String LOGIT_RESULT_ID = "com.example.facebookagent.LOGIN_RESULT_ID";
    public static final String LOGIN_RESULT_NAME = "com.example.facebookagent.LOGIN_RESULT_NAME";
    public static final String LOGIN_RESULT_EMAIL = "com.example.facebookagent.LOGIN_RESULT_EMAIL";
    public static final String LOGIN_RESULT_BIRTH = "com.example.facebookagent.LOGIN_RESULT_BIRTH";
    public static final String LOGIN_RESULT_HOMETOWN = "com.example.facebookagent.LOGIN_RESULT_HOMETOWN";
    public static final String LOGIN_RESULT_FRIENDS = "com.example.facebookagent.LOGIN_RESULT_FRIENDS";
    public static final String TAG = "fb_log";

    CallbackManager callbackManager;
    ProgressDialog mDialog;
    Intent intent;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        callbackManager = CallbackManager.Factory.create();

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.facebookagent",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        final LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends, user_hometown"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setMessage("Waiting for data...");
                mDialog.show();

                AccessToken accesstoken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(accesstoken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();

                        try {
                            Log.d(TAG, object.toString());
                            sendResult(object);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, birthday, friends, hometown");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "onError", error);
            }
        });
    }

    private void sendResult(JSONObject object) throws JSONException {
        intent = new Intent(this, PageActivity.class);
        Log.d(TAG, object.getString("id"));
        intent.putExtra(LOGIT_RESULT_ID, object.getString("id"));
        intent.putExtra(LOGIN_RESULT_NAME, object.getString("name"));
        intent.putExtra(LOGIN_RESULT_EMAIL, object.getString("email"));
        intent.putExtra(LOGIN_RESULT_BIRTH, object.getString("birthday"));
        intent.putExtra(LOGIN_RESULT_HOMETOWN, object.getJSONObject("hometown").getString("name"));
        intent.putExtra(LOGIN_RESULT_FRIENDS, object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
        startActivity(intent);
    }
}
