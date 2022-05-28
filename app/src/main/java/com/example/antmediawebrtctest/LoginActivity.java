package com.example.antmediawebrtctest;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    private TextInputEditText phone, password;
    private Button login;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestPermission();
        requestQueue = Volley.newRequestQueue(this);
        phone = findViewById(R.id.login_phone);
        password = findViewById(R.id.login_password);
        login = findViewById(R.id.login);
        login.setOnClickListener(view -> {
            login.setEnabled(false);
            vendorLogin();
        });
    }

    private void vendorLogin() {

        String _phone = phone.getText().toString();
        String _password = password.getText().toString();
        String url = "http://159.223.58.172/api/v1/vendor-login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Log.v("api", "response: " + response);
            Gson gson = new Gson();
            Vendor vendor = gson.fromJson(response, Vendor.class);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("name", vendor.getStore().get(0).getName());
            intent.putExtra("logo", vendor.getStore().get(0).getLogo());

            intent.putExtra("store", String.valueOf(vendor.getStore().get(0).getId()));
            startActivity(intent);
        }, error -> {
            Log.v("api", "error: " + error);
            Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_LONG).show();
            login.setEnabled(true);


        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", _phone);
                params.put("password", _password);

                return params;
            }
        };
        requestQueue.add(stringRequest);

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,

                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH_CONNECT}, 22);
    }
}