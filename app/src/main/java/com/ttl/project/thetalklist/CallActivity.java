package com.ttl.project.thetalklist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.opentok.android.OpentokError;
import com.opentok.android.PublisherKit;
import com.opentok.android.Stream;

import static android.content.Context.MODE_PRIVATE;

public class CallActivity extends AppCompatActivity implements PublisherKit.PublisherListener {

    private static final String LOG_TAG = New_videocall_activity.class.getSimpleName();
    private static final String TAG = "CallActivity";
    ImageView ans, reject;
    FrameLayout frameCameraPreview;
    int wasActive;
    RequestQueue queue111;
    Camera mCamera;
    TextView incomingCall_CallerName;
//    CameraView cameraView;
    ImageView call_activity_image;
    BroadcastReceiver callEnd;
    private Vibrator vib;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        SharedPreferences p = getSharedPreferences("videocallrole", MODE_PRIVATE);
        SharedPreferences.Editor ed = p.edit();
        ed.putString("videocallrole", "subscriber").apply();

        frameCameraPreview = (FrameLayout) findViewById(R.id.frameCameraPreview);
        wasActive = 0;

        if (wasActive == 1)
            onBackPressed();


        callEnd = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        registerReceiver(callEnd, new IntentFilter("callEnd"));

        incomingCall_CallerName = (TextView) findViewById(R.id.incomingCall_CallerName);
        call_activity_image = (ImageView) findViewById(R.id.call_activity_image);
        final SharedPreferences preferences = getApplicationContext().getSharedPreferences("videoCallTutorDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        incomingCall_CallerName.setText(preferences.getString("callSenderName", ""));


        if (!preferences.getString("image", "").equals("")) {
            Glide.with(getApplicationContext()).load("https://www.thetalklist.com/uploads/images/" + preferences.getString("image", ""))
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(getApplicationContext()))
                    .placeholder(R.drawable.process)
                    .error(R.drawable.black_person)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(call_activity_image);
        } else {
            Glide.with(getApplicationContext()).load("https://www.thetalklist.com/images/header.jpg")
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(getApplicationContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(call_activity_image);
        }


        mp = MediaPlayer.create(this, R.raw.incoming);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioManager m_amAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.STREAM_MUSIC);
        m_amAudioManager.setSpeakerphoneOn(true);
        mp.setLooping(true);
        m_amAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, m_amAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        if (m_amAudioManager.isWiredHeadsetOn()) {
            Toast.makeText(this, "Headset plugged in", Toast.LENGTH_SHORT).show();
            m_amAudioManager.setMode(AudioManager.STREAM_MUSIC);
            m_amAudioManager.setSpeakerphoneOn(false);
            m_amAudioManager.setWiredHeadsetOn(true);
        } else {
            m_amAudioManager.setWiredHeadsetOn(false);
            m_amAudioManager.setSpeakerphoneOn(true);
            m_amAudioManager.setMode(AudioManager.STREAM_MUSIC);
        }
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp.start();

        vib = (Vibrator) this.getSystemService(getApplication().VIBRATOR_SERVICE);
        long pattern[] = {200, 200, 200, 200, 200, 200, 200, 200, 200};
        try {

            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        try {

           /* cameraView = new CameraView(getApplicationContext(), mCamera);
            frameCameraPreview.addView(cameraView);*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT
                || audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {

            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {

            } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {

                vib.vibrate(pattern, 4);
            }

        } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            vib.vibrate(pattern, 4);
            mp.start();
        }


        ans = (ImageView) findViewById(R.id.ansbutton);
        reject = (ImageView) findViewById(R.id.rejectbutton);

        ans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Stop Ringing: ");
                wasActive = 1;

                vib.cancel();
                Intent i = new Intent("com.example.saubhagyam.thetalklist");
                i.putExtra("from", "callActivity");

                TTL ttl = new TTL();
                ttl.Callfrom = "callActivity";

                finish();

                New_videocall_activity videoCall = new New_videocall_activity();
                i.setClass(getApplicationContext(), videoCall.getClass());
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                getApplication().startActivity(i);
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

                Intent i = new Intent();
                i.setAction("callEnd");
                sendBroadcast(i);


                queue111 = Volley.newRequestQueue(getApplicationContext());
                SharedPreferences preferences = getSharedPreferences("videoCallTutorDetails", MODE_PRIVATE);
                SharedPreferences pref = getSharedPreferences("loginStatus", MODE_PRIVATE);


                String URL = "https://www.thetalklist.com/api/firebase_rejectcall?sender_id=" + pref.getInt("id", 0) + "&receiver_id=" + preferences.getInt("tutorId", 0) + "&cid=" + preferences.getInt("classId", 0);
                Log.e("firebase reject Call", URL);
                Log.e(TAG, "Reject Call" + URL);
                StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                queue111.add(sr);


            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp.isPlaying())
            mp.stop();
        if (vib != null)
            vib.cancel();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasActive == 1)
            onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mp.stop();
        vib.cancel();
        if (mCamera != null)
            mCamera.release();

        finish();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(callEnd);
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher Stream Created");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher Stream Destroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }

    private void logOpenTokError(OpentokError opentokError) {
        Log.e(LOG_TAG, "Error Domain: " + opentokError.getErrorDomain().name());
        Log.e(LOG_TAG, "Error Code: " + opentokError.getErrorCode().name());
    }
}