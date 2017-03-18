package com.example.duanyi.myapplication;
import android.*;
import android.Manifest;
import android.app.Dialog;
import android.location.Location;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;

/**
 * Created by chrissie on 2/18/17.
 */

public class HorizontalViewActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private AuthorizationService mAuthorizationService;
    private AuthState mAuthState;
    private OkHttpClient mOkHttpClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int LOCATION_PERMISSION_RESULT = 17;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView mLatText;
    private TextView mLonText;
    private Location mLastLocation;
    private LocationListener mLocationListener;
    private TextView test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        setContentView(R.layout.activity_horizontal); //activity_api
        mAuthorizationService = new AuthorizationService(this);
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLatText = (TextView) findViewById(R.id.lat_output);
        mLonText = (TextView) findViewById(R.id.long_output);
        mLatText.setText("44.5");
        mLonText.setText("-123.2");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2);
        mLocationRequest.setFastestInterval(2);
        mLocationListener = new LocationListener () {
            @Override
            public void onLocationChanged(Location location){
                if(location != null){
                    mLonText.setText(String.valueOf(location.getLongitude()));
                    mLatText.setText(String.valueOf(location.getLatitude()));
                } else{
                    mLonText.setText("-123.2");
                }
            }

        };
        ((Button) findViewById(R.id.google_plus_get_post_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/calendar/v3/users/me/calendarList");
                                //reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBGOMhcH7esziKy22kToaAPncbcUkZZw3U").build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        //Log.i("respong.body", response.body().string());
                                        Log.i("herrrrrrererer:   " , r);
                                        try {
                                            JSONObject j = new JSONObject(r);

                                            JSONArray items = j.getJSONArray("items");
                                            List<Map<String, String>> posts = new ArrayList<Map<String, String>>();
                                            for (int i = 0; i < items.length(); i++) {
                                                HashMap<String, String> m = new HashMap<String, String>();
                                                m.put("summary", items.getJSONObject(i).getString("summary"));
                                                m.put("id", items.getJSONObject(i).getString("id"));
                                                m.put("location", items.getJSONObject(i).getString("location"));
                                                posts.add(m);
                                            }
                                            final SimpleAdapter postAdapter = new SimpleAdapter(
                                                    HorizontalViewActivity.this,
                                                    posts,
                                                    R.layout.google_plus_item,
                                                    new String[]{"summary","id","location"},
                                                    new int[]{R.id.google_plus_item_date_text, R.id.google_plus_item_text, R.id.google_plus_location_text});
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ListView) findViewById(R.id.google_post_list)).setAdapter(postAdapter);
                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }

                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ((Button) findViewById(R.id.google_plus_post_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if (e == null) {
                                mOkHttpClient = new OkHttpClient();//key = AIzaSyBFSvufl2Wxcl_587Lp8AaUk-jpV8CjAu8
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/me/activities/");//102828643594070495481
                                //reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyBFSvufl2Wxcl_587Lp8AaUk-jpV8CjAu8").build();
                                //String json = "{'object': { 'originalContent' : 'QAQ' }, 'access': {'domainRestricted' : true }}";
                                EditText edittext_lat = (EditText) findViewById(R.id.lat_output);
                                EditText edittext_long = (EditText) findViewById(R.id.long_output);
                                String text_lat = edittext_lat.getText().toString();
                                String text_long = edittext_long.getText().toString();
                                String loca = "My location is: ";
                                String lat = "Latitude: ";
                                String longt = "Longitude: ";
                                String comma = ", ";
                                //string Jason = "{ 'object': ' " + variableName + " '}"
                                String json = "{'object': { 'originalContent' : ' " + loca + "" + lat + "" + text_lat + "" + comma + "" + longt + "" + text_long + " ' }, 'access': {'domainRestricted' : true }}";
                                RequestBody body = RequestBody.create(JSON, json);
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .post(body)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();

                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ((Button) findViewById(R.id.sign_out)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.google.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);


            }
        });
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_RESULT);
            return;
        }
        UpdateLocation();
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_RESULT)
            if(grantResults.length > 0)
                UpdateLocation();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        mAuthState = getOrCreateAuthState();

        super.onStart();

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    //@Override
    //public void onStart() {
    //    mGoogleApiClient.connect();
    //    super.onStart();

    //}

    AuthState getOrCreateAuthState() {
        AuthState auth = null;
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authPreference.getString("stateJson", null);
        if (stateJson != null) {
            try {
                auth = AuthState.jsonDeserialize(stateJson);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        if (auth != null && auth.getAccessToken() != null) {
            return auth;
        } else {
            updateAuthState();
            return null;
        }
    }

    void updateAuthState() {

        Uri authEndpoint = new Uri.Builder().scheme("https").authority("accounts.google.com").path("/o/oauth2/v2/auth").build();
        Uri tokenEndpoint = new Uri.Builder().scheme("https").authority("www.googleapis.com").path("/oauth2/v4/token").build();
        Uri redirect = new Uri.Builder().scheme("com.example.duanyi.myapplication").path("foo").build();

        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint, null);
        AuthorizationRequest req = new AuthorizationRequest.Builder(config, "438853034939-2tgspqigjd1u8d5eh7nuqilmi0thbrk1.apps.googleusercontent.com", ResponseTypeValues.CODE, redirect)
                .setScope("https://www.googleapis.com/auth/calendar")
                //.setScopes("https://www.googleapis.com/auth/plus.me", "https://www.googleapis.com/auth/plus.stream.write", "https://www.googleapis.com/auth/plus.stream.read")
                .build();

        Intent authComplete = new Intent(this, AuthCompleteActivity.class);
        mAuthorizationService.performAuthorizationRequest(req, PendingIntent.getActivity(this, req.hashCode(), authComplete, 0));
    }
    private void UpdateLocation(){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            mLonText.setText(String.valueOf(mLastLocation.getLongitude()));
            mLatText.setText(String.valueOf(mLastLocation.getLatitude()));
        }else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
        }
    }
}

