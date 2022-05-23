package com.example.antmediawebrtctest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.webrtc.SurfaceViewRenderer;

import io.antmedia.webrtcandroidframework.IWebRTCClient;
import io.antmedia.webrtcandroidframework.IWebRTCListener;
import io.antmedia.webrtcandroidframework.WebRTCClient;
import io.antmedia.webrtcandroidframework.apprtc.CallFragment;
import io.antmedia.webrtcandroidframework.apprtc.UnhandledExceptionHandler;

public class MainActivity extends AppCompatActivity {
    public static final String SERVER_ADDRESS = "rtmp://165.232.166.106/WebRTCApp";
    private String webRTCMode = IWebRTCClient.MODE_PUBLISH;

    private CallFragment callFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
}