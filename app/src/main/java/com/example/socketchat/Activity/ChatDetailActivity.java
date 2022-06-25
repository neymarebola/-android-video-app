package com.example.socketchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.socketchat.Constants;
import com.example.socketchat.MainActivity;
import com.example.socketchat.MessageAdapter;
import com.example.socketchat.R;
import com.example.socketchat.Service.MyService;
import com.example.socketchat.models.Message;
import com.example.socketchat.models.User;
import com.facebook.react.modules.toast.ToastModule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Stopwatch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatDetailActivity extends AppCompatActivity {
    TextView contactName;
    RecyclerView messageRec;
    List<Message> listM;
    MessageAdapter messageAdapter;
    EditText message;
    public static ImageView videoCall;
    ImageView send, call, settings, gallery, camera, voice, avatar;
    //User contactUser;
    String contactId;
    ProgressDialog dialog;
    private Socket mSocket = MainActivity.mSocket;
    MediaRecorder mediaRecorder;
    MediaPlayer mp;
    LottieAnimationView animationView;

    String secretCode = "theduong";
    String path = "";

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    final int REQUEST_PERMISSION_CODE = 1000;

    public static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        initView();

        startService(new Intent(getBaseContext(), MyService.class));
        dialog = new ProgressDialog(this);
        dialog.setTitle("Video call");
        dialog.setMessage("Dang ket noi ...");
        dialog.setCancelable(false);

        Intent intent = getIntent();
        contactId = intent.getStringExtra("contact_uid");
        // get contact user
        DatabaseReference rf = database.getReference("Users").child(contactId);
        rf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                contactName.setText(u.getUsername());
                String img = u.getProfileImg();
                byte[] bytes = android.util.Base64.decode(img, android.util.Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        listM = new ArrayList<>();
        messageRec = findViewById(R.id.chat_detail_rec);
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        messageRec.setLayoutManager(manager);
        messageAdapter = new MessageAdapter(listM, this);
        messageRec.setAdapter(messageAdapter);

        //Toast.makeText(getBaseContext(), MainActivity.loggedUserId + " " + contactUser.getId(), Toast.LENGTH_SHORT).show();
        showMessageToChatFrame(MainActivity.loggedUserId, contactId);
        declineEvtListener();
        acceptEvtListener();

        message.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                mSocket.emit("client-send-stop-typing", contactId);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               mSocket.emit("client-send-typing", contactId);
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = message.getText().toString();
                Date date = new Date();
                Message m = new Message(MainActivity.loggedUserId, contactId, text, Constants.TEXT, 0, date.getTime());
                sendMsg(m);
                messageRec.scrollToPosition(messageAdapter.getItemCount() - 1);
                message.setText("");
                //  luu tin nhan cuoi cung
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference ref = db.getReference("LastM");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean ok = false;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message msg = dataSnapshot.getValue(Message.class);
                            String tmp = msg.getMy_id() + msg.getContact_id();
                            if (tmp.equals(m.getMy_id()+m.getContact_id())
                                || tmp.equals(m.getContact_id()+m.getMy_id())) {
                                ref.child(dataSnapshot.getKey()).setValue(m);
                                ok = true;
                                break;
                            }
                        }
                        if (ok == false) {
                            ref.child(m.getContact_id() + m.getMy_id()).setValue(m);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRecorderDialog(Gravity.CENTER);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // chuyen anh bitmap sang byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();
                // chuyen mang byte sang dang string , tao new message va add vao list view
                String imageStr = Base64.getEncoder().encodeToString(imageData);
                Date date = new Date();
                Message m = new Message(MainActivity.loggedUserId, contactId, imageStr, Constants.IMAGE, 0, date.getTime());
                sendMsg(m);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // dieu khien cac nut bam tren dialog
    boolean onMic = false;

    void openRecorderDialog(int gravity) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.voice_chat_dialog_layout);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        window.setGravity(gravity);
        window.setAttributes(windowAttributes);

        // khai bao cac thanh phan tren dialog
        TextView titleTime = dialog.findViewById(R.id.img_titleTime);
        ImageView cancelButton, sendButton, recordButton;
        Chronometer chronometer = dialog.findViewById(R.id.simpleChronometer);
        cancelButton = dialog.findViewById(R.id.img_cancel);
        sendButton = dialog.findViewById(R.id.img_ok);
        recordButton = dialog.findViewById(R.id.img_mic);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ng dung chua click vao nut ghi am
                if (!onMic) {
                    onMic = true;
                    recordButton.setImageResource(R.drawable.pause);
                    chronometer.start();
                    // bat dau ghi am
                    StartRecord();
                } else {
                    // ng dung da ghi am
                    StopRecord();
                    onMic = false;
                    recordButton.setImageResource(R.drawable.micro);
                    chronometer.stop();
                    cancelButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.VISIBLE);
                }
            }
        });

        // bam nut gui: chuyen file mp3 sang byte va gui len server
        sendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                byte[] amthanh = FileLocal_To_Byte(path);
                MainActivity.mSocket.emit("client-gui-amthanh", amthanh);// gui file am thanh len server

                Date date = new Date();
                String data = Base64.getEncoder().encodeToString(amthanh);
                Message m = new Message(MainActivity.loggedUserId, contactId, data, Constants.AUDIO, 0, date.getTime());
                sendMsg(m); // gui tin nhan am thanh len server va luu vao db
            }
        });

        dialog.show();
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

            Toast.makeText(getBaseContext(), "Recording...", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
    }

    void StopRecord() {
        mediaRecorder.stop();
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

    private void initView() {
        contactName = findViewById(R.id.tvContactName);
        messageRec = findViewById(R.id.chat_detail_rec);
        message = findViewById(R.id.edtMessage);
        send = findViewById(R.id.imgSend);
        videoCall = findViewById(R.id.img_video_call);
        call = findViewById(R.id.img_call);
        settings = findViewById(R.id.img_settings);
        camera = findViewById(R.id.ic_camera);
        gallery = findViewById(R.id.ic_gallery);
        voice = findViewById(R.id.ic_mic);
        avatar = findViewById(R.id.avatar_chat);
        //animationView = findViewById(R.id.typingStatus);
    }


    // hien thi tat ca tin nhan tu db
    void showMessageToChatFrame(String myId, String contactId) {
        database.getReference().child("Messages").orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listM.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message m = dataSnapshot.getValue(Message.class);
                    if ((m.getMy_id().equals(myId) && m.getContact_id().equals(contactId)) ||
                            (m.getMy_id().equals(contactId) && m.getContact_id().equals(myId))) {
                        listM.add(m);
                        messageAdapter.notifyDataSetChanged();
                    }
                }
                messageRec.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    List<String> list = new ArrayList<>();

    private void sendMsg(Message m) {
        // gui message cung thong tin cua nguoi kia len server
        JSONObject dataJson = new JSONObject();
        // thong tin gui len bao gom: id nguoi nhan, noi dung tin nhan
        try {
            dataJson.put("sender_id", m.getMy_id());
            dataJson.put("contact_id", m.getContact_id());
            dataJson.put("msg_text", m.getMsg_text());
            dataJson.put("msg_type", m.getType());
            dataJson.put("msg_date", m.getDate());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("client-send-msg", dataJson);
        Date date = new Date();
        Message myMessage = new Message(m.getMy_id(), m.getContact_id(), m.getMsg_text(), m.getType(), m.getStatus(), m.getDate());
        listM.add(myMessage);
        // hien thi tin nhan len recyclerView
        messageAdapter.notifyDataSetChanged();
    }

    private void receiveMsg() {
        MainActivity.mSocket.on("server-send-msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0]; // json chua thong tin cua message
                        try {
                            String msg = data.getString("text");
                            String my_id = data.getString("sender_id");
                            String receiver_id = data.getString("receiver_id");
                            int type = data.getInt("msg_type");
                            Message m = new Message(my_id, receiver_id, msg, type);
                            // neu id ng nhan la id cua minh va id ng gui = id cua contact user thi ms hien thi
                            if (m.getContact_id().equals(MainActivity.loggedUserId)) {
                                // add tin nhan to recyclerView
                                listM.add(m);
                                messageAdapter.notifyDataSetChanged();
                                messageRec.scrollToPosition(messageAdapter.getItemCount() - 1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("tag", data.toString());
                    }
                });
            }
        });
    }

    public void videoCallOnClick(View view) {
        // xu ly hanh dong khi click vao nut call
        String callerId = MainActivity.loggedUserId;
        String listenerId = contactId;
        JSONObject callerData = new JSONObject();
        try {
            callerData.put("callerId", callerId);
            callerData.put("listenerId", listenerId);
            callerData.put("secretCode", secretCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("client-send-call-data", callerData);
        dialog.show();
    }

    public void voiceCallOnClick(View view) {
        startActivity(new Intent(getBaseContext(), VoiceChatActivity.class));
    }

    void declineEvtListener() {
        mSocket.on("server-send-decline-call", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String callerId = data.getString("id_caller");
                            if (callerId.equals(MainActivity.loggedUserId)) {
                                dialog.dismiss();
                                Toast.makeText(getBaseContext(), "Nguoi nhan tu choi cuoc goi", Toast.LENGTH_SHORT).show();
                                // phat am thanh
                                playRejectsound();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void playRejectsound() {
        try {
            mp = MediaPlayer.create(ChatDetailActivity.this, R.raw.call_reject);
        } catch (IllegalArgumentException e) {

        } catch (SecurityException e) {

        } catch (IllegalStateException e) {

        }
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the" URI correctly!, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            //Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
        }
        mp.start();
    }

    void acceptEvtListener() {
        mSocket.on("server-send-accept-call", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String callerId = data.getString("id_caller");
                            if (callerId.equals(MainActivity.loggedUserId)) {
                                dialog.dismiss();
                                // vao jitsi meet
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
                                        .setRoom(String.valueOf(secretCode))
                                        .setWelcomePageEnabled(false).build();
                                JitsiMeetActivity.launch(getBaseContext(), options);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    int check = 0;

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        //onTypingEventListener();
        //onStopTypingEventListener();
        if (!MyService.unReadMessages.isEmpty()) {
            for(Message m : MyService.unReadMessages) {
                database.getReference("Messages").child(m.getMy_id() + m.getContact_id() + m.getDate())
                        .child("status").setValue(2);
            }
            MyService.unReadMessages.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
        //onStopTypingEventListener();
        //onTypingEventListener();
        if (!MyService.unReadMessages.isEmpty()) {
            for(Message m : MyService.unReadMessages) {
                database.getReference("Messages").child(m.getMy_id() + m.getContact_id() + m.getDate())
                        .child("status").setValue(2);
            }
            MyService.unReadMessages.clear();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }
}