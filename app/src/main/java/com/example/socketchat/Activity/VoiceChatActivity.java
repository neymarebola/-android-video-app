package com.example.socketchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.socketchat.MainActivity;
import com.example.socketchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class VoiceChatActivity extends AppCompatActivity {

    Button record, stopRecord, play, stopPlay, sendData;
    String path = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Socket socket;
    FirebaseFirestore fs = FirebaseFirestore.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    final int REQUEST_PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_voice_chat);
        initView();
        socket = MainActivity.mSocket;
        socket.on("server-gui-amthanh", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                    }
                });
            }
        });

        if (checkPermissionFromDevice())
            requestPermission();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartRecord();
            }
        });
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StopRecord();
            }
        });

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] amthanh = FileLocal_To_Byte(path);
                MainActivity.mSocket.emit("client-gui-amthanh", amthanh);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    String audio = Base64.getEncoder().encodeToString(amthanh);
                    db.getReference().child("Audio").push().setValue(audio).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getBaseContext(), "saved audio", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getReference().child("Audio").addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String data = dataSnapshot.getValue(String.class);
                            // chuyen string data thanh byte array
                            byte[] audio = Base64.getDecoder().decode(data);
                            // chuyen byte array thanh file audio
                            playMp3FromByte(audio);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    void StartRecord() {
        if (checkPermissionFromDevice()) {
            path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/"
                    + UUID.randomUUID().toString() + "_audio_record.3gp";
            setUpMediaRecorder();
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            play.setEnabled(false);
            stopPlay.setEnabled(false);
            Toast.makeText(getBaseContext(), "Recording...", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
    }

    void StopRecord() {
        mediaRecorder.stop();
        record.setEnabled(false);
        play.setEnabled(true);
        record.setEnabled(true);
        stopPlay.setEnabled(false);
        Toast.makeText(getBaseContext(), "Stop record...", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("WrongConstant")
    private void setUpMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(path);
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getBaseContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    public byte[] FileLocal_To_Byte(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    public void playMp3FromByte(byte[] mp3SoundByteArray) {
        try {
            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            FileInputStream fis = new FileInputStream(tempMp3);
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

    void initView() {
        record = findViewById(R.id.btn_start_record);
        stopRecord = findViewById(R.id.btn_stop_record);
        play = findViewById(R.id.btn_play);
        stopPlay = findViewById(R.id.btn_stop_play);
        sendData = findViewById(R.id.btn_send_server);
    }


}