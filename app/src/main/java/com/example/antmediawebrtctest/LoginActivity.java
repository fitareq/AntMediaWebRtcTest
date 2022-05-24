package com.example.antmediawebrtctest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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

        requestQueue = Volley.newRequestQueue(this);
        phone = findViewById(R.id.login_phone);
        password = findViewById(R.id.login_password);
        login = findViewById(R.id.login);
        login.setOnClickListener(view -> vendorLogin());
    }

    private void vendorLogin(){
        String _phone = phone.getText().toString();
        String _password = password.getText().toString();
        String url = "http://159.223.58.172/api/v1/vendor-login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Gson gson = new Gson();
            Vendor vendor = gson.fromJson(response, Vendor.class);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("name",vendor.getStore().get(0).getName() );
            intent.putExtra("logo", vendor.getStore().get(0).getLogo());
        }, error -> {
            Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_LONG).show();
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email",_phone);
                params.put("password",_password);

                return params;
            }
        };
        requestQueue.add(stringRequest);

    }
}