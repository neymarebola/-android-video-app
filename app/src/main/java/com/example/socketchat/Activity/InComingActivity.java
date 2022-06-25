package com.example.socketchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socketchat.MainActivity;
import com.example.socketchat.R;
import com.example.socketchat.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.socket.client.Socket;

public class InComingActivity extends AppCompatActivity {
    TextView callerName;
    ImageView decline, accept, avatar;
    Socket socket;
    MediaPlayer mp;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_in_coming);
        initView();
        socket = MainActivity.mSocket;
        playRingStone();

        Intent intent = getIntent();
        String code = intent.getStringExtra("secretCode");
        String callerId = intent.getStringExtra("callerId");
        database.getReference().child("Users").child(callerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                callerName.setText(u.getUsername());
                String img = u.getProfileImg();
                byte[] bytes = Base64.decode(img, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // gui thong bao len server ng nhan tu choi cuoc goi
                socket.emit("client-decline-call", callerId);
                mp.stop();
                finish();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.stop();
                socket.emit("client-accept-call", callerId);
                URL server;
                try {
                    server = new URL("https://meet.jit.si");
                    JitsiMeetConferenceOptions defaultOptions =
                            new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(server)
                                    .setWelcomePageEnabled(false)
                                    .build();
                    JitsiMeet.setDefaultConferenceOptions(defaultOptions);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                        .setRoom(String.valueOf(code))
                        .setWelcomePageEnabled(false).build();
                JitsiMeetActivity.launch(getBaseContext(), options);
                finish();
            }
        });
    }

    void initView() {
        callerName = findViewById(R.id.txtCallerName);
        decline = findViewById(R.id.declineBtn);
        accept = findViewById(R.id.acceptBtn);
        avatar = findViewById(R.id.avatar_incoming);
    }

    void playRingStone() {
        try {
            mp = MediaPlayer.create(InComingActivity.this, R.raw.sound);
        } catch (IllegalArgumentException e) {

        } catch (SecurityException e) {

        } catch (IllegalStateException e) {

        }
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
        } catch (IOException e) {
        }
        mp.start();
    }
}