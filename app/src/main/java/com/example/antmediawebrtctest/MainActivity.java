package com.example.antmediawebrtctest;

import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED;
import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_DATA_CHANNEL_ENABLED;
import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_VIDEO_BITRATE;
import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_VIDEO_FPS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.webrtc.DataChannel;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Objects;

import de.tavendo.autobahn.WebSocket;
import io.antmedia.webrtcandroidframework.IDataChannelObserver;
import io.antmedia.webrtcandroidframework.IWebRTCClient;
import io.antmedia.webrtcandroidframework.IWebRTCListener;
import io.antmedia.webrtcandroidframework.StreamInfo;
import io.antmedia.webrtcandroidframework.WebRTCClient;
import io.antmedia.webrtcandroidframework.apprtc.CallActivity;
import io.antmedia.webrtcandroidframework.apprtc.CallFragment;
import io.antmedia.webrtcandroidframework.apprtc.UnhandledExceptionHandler;

public class MainActivity extends AppCompatActivity implements IWebRTCListener, IDataChannelObserver {


    /**
     * Change this address with your Ant Media Server address
     */
    public static final String SERVER_ADDRESS = "159.223.79.46:5080";
    private String streamUrl = "http://159.223.79.46:5080/WebRTCApp/play.html?name=";

    /**
     * Mode can Publish, Play or P2P
     */
    private String webRTCMode = IWebRTCClient.MODE_PUBLISH;

    private boolean enableDataChannel = true;


    public static final String SERVER_URL = "ws://" + SERVER_ADDRESS + "/WebRTCApp/websocket";

    private WebRTCClient webRTCClient;

    private Button startStreamingButton;
    private String operationName = "";
    private String streamId;

    private SurfaceViewRenderer cameraViewRenderer;
    private SurfaceViewRenderer pipViewRenderer;
    private ImageView flipCamera, muteMic;
    private RoundedImageView shopImage;
    private TextView shopName;


    // variables for handling reconnection attempts after disconnected
    final int RECONNECTION_PERIOD_MLS = 100;
    private boolean stoppedStream = false;
    Handler reconnectionHandler = new Handler();
    Runnable reconnectionRunnable = new Runnable() {
        @Override
        public void run() {
            if (!webRTCClient.isStreaming()) {
                attempt2Reconnect();
                // call the handler again in case startStreaming is not successful
                reconnectionHandler.postDelayed(this, RECONNECTION_PERIOD_MLS);
            }
        }
    };

    private void attempt2Reconnect() {
        Log.w(getClass().getSimpleName(), "Attempt2Reconnect called");
        if (!webRTCClient.isStreaming()) {
            webRTCClient.startStream();
            if (Objects.equals(webRTCMode, IWebRTCClient.MODE_JOIN)) {
                pipViewRenderer.setZOrderOnTop(true);
            }
        }
    }

    /*private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 22);
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(/*WindowManager.LayoutParams.FLAG_FULLSCREEN |*/  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);


        //requestPermission();

        String shop_name = getIntent().getStringExtra("name");
        String shop_image = getIntent().getStringExtra("logo");
        String store_id = getIntent().getStringExtra("store");

        shopName = findViewById(R.id.shop_name);
        shopImage = findViewById(R.id.roundedImageView);
        flipCamera = findViewById(R.id.flip_camera);
        muteMic = findViewById(R.id.mute_mic);
        cameraViewRenderer = findViewById(R.id.camera_view_renderer);
        pipViewRenderer = findViewById(R.id.pip_view_renderer);
        startStreamingButton = findViewById(R.id.start_streaming_button);

        // Check for mandatory permissions.
        for (String permission : CallActivity.MANDATORY_PERMISSIONS) {
            if (this.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission " + permission + " is not granted", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        this.getIntent().putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        this.getIntent().putExtra(EXTRA_VIDEO_FPS, 30);
        this.getIntent().putExtra(EXTRA_VIDEO_BITRATE, 1500);
        this.getIntent().putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        this.getIntent().putExtra(EXTRA_DATA_CHANNEL_ENABLED, enableDataChannel);

        if (shop_image != null) {
            shop_image = "https://static.durbar.live/" + shop_image;
            Picasso.get().load(shop_image).into(shopImage);
        }
        if (shop_name != null) {
            shopName.setText(shop_name);
        }

        webRTCClient = new WebRTCClient(this, this);
        if (shop_name != null)
            streamId = shop_name.replaceAll(" ", "-").toLowerCase() + "_" + store_id;
        streamUrl += streamId;
        String tokenId = "";
        webRTCClient.setVideoRenderers(pipViewRenderer, cameraViewRenderer);

        // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);
        webRTCClient.init(SERVER_URL, streamId, webRTCMode, tokenId, this.getIntent());
        webRTCClient.setDataChannelObserver(this);


        startStreamingButton.setOnClickListener(view -> startStreaming());
        flipCamera.setOnClickListener(view -> {
            webRTCClient.switchCamera();
        });
        muteMic.setOnClickListener(view -> {
            if (muteMic.isSelected()) {
                muteMic.setSelected(false);
                muteMic.setBackgroundColor(0);
                muteMic.clearColorFilter();
            } else {
                muteMic.setSelected(true);
                muteMic.setBackgroundColor(getResources().getColor(R.color.pink));
            }
        });

    }

    private void startStreaming() {
        Log.v("url", streamUrl);

        if (webRTCClient.isStreaming()) {
            startStreamingButton.setText("Go Live");
            webRTCClient.stopStream();
            stoppedStream = true;
        } else {
            startStreamingButton.setText("End");
            webRTCClient.startStream();
            stoppedStream = false;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        webRTCClient.stopStream();
    }


    @Override
    public void onPlayStarted(String streamId) {
        Log.w(getClass().getSimpleName(), "onPlayStarted");
        Toast.makeText(this, "Play started", Toast.LENGTH_LONG).show();
        webRTCClient.switchVideoScaling(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        webRTCClient.getStreamInfoList();
    }

    @Override
    public void onPublishStarted(String streamId) {
        Log.w(getClass().getSimpleName(), "onPublishStarted");
        Toast.makeText(this, "Publish started", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPublishFinished(String streamId) {
        Log.w(getClass().getSimpleName(), "onPublishFinished");
        Toast.makeText(this, "Publish finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPlayFinished(String streamId) {
        Log.w(getClass().getSimpleName(), "onPlayFinished");
        Toast.makeText(this, "Play finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public void noStreamExistsToPlay(String streamId) {
        Log.w(getClass().getSimpleName(), "noStreamExistsToPlay");
        Toast.makeText(this, "No stream exist to play", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void streamIdInUse(String streamId) {
        Log.w(getClass().getSimpleName(), "streamIdInUse");
        Toast.makeText(this, "Stream id is already in use.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(String description, String streamId) {
        Toast.makeText(this, "Error: " + description, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onDisconnected(String streamId) {
        Log.w(getClass().getSimpleName(), "disconnected");
        Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();

        startStreamingButton.setText("Start " + operationName);
        // handle reconnection attempt
        if (!stoppedStream) {
            Toast.makeText(this, "Disconnected Attempting to reconnect", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!reconnectionHandler.hasCallbacks(reconnectionRunnable)) {
                    reconnectionHandler.postDelayed(reconnectionRunnable, RECONNECTION_PERIOD_MLS);
                }
            } else {
                reconnectionHandler.postDelayed(reconnectionRunnable, RECONNECTION_PERIOD_MLS);
            }
        } else {
            Toast.makeText(this, "Stopped the stream", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBufferedAmountChange(long previousAmount, String dataChannelLabel) {

    }

    @Override
    public void onStateChange(DataChannel.State state, String dataChannelLabel) {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, String dataChannelLabel) {

    }

    @Override
    public void onMessageSent(DataChannel.Buffer buffer, boolean successful) {

    }


    @Override
    public void onSignalChannelClosed(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification code, String streamId) {

    }

    @Override
    public void onIceConnected(String streamId) {

    }

    @Override
    public void onIceDisconnected(String streamId) {

    }

    @Override
    public void onTrackList(String[] tracks) {

    }

    @Override
    public void onBitrateMeasurement(String streamId, int targetBitrate, int videoBitrate, int audioBitrate) {

    }

    @Override
    public void onStreamInfoList(String streamId, ArrayList<StreamInfo> streamInfoList) {

    }
}