package com.example.socketchat.Service;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.FaceDetector;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.socketchat.Activity.InComingActivity;
import com.example.socketchat.Fragments.MessageFragment;
import com.example.socketchat.MainActivity;
import com.example.socketchat.MyApplication;
import com.example.socketchat.R;
import com.example.socketchat.models.Message;
import com.example.socketchat.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MyService extends Service {
    private Socket mSocket;
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference ref = database.getReference();
    public static List<Message> unReadMessages = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mSocket = MainActivity.mSocket;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSocket.on("server-send-msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // hien thi thong bao noi dung tin nhan gui ve
                        JSONObject data = (JSONObject) args[0]; // json chua thong tin cua message
                        try {
                            String msg = data.getString("text");
                            String my_id = data.getString("sender_id");
                            String receiver_id = data.getString("receiver_id");
                            int type = data.getInt("msg_type");
                            long date = data.getLong("msg_date");
                            Message m = new Message(my_id, receiver_id, msg, type);
                            m.setDate(date);
                            // neu id ng nhan la id cua minh va id ng gui = id cua contact user thi ms hien thi
                            if (m.getContact_id().equals(MainActivity.loggedUserId)) {
                                sendNotification("DemoApp");
                                m.setStatus(1);
                                saveMessagesToDb(m);// da nhan duoc tin nhan
                                unReadMessages.add(m);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        receiveCallData();

        return START_NOT_STICKY;
    }

    void saveMessagesToDb(Message m) {
        database = FirebaseDatabase.getInstance();
        String mId = m.getMy_id() + m.getContact_id() + m.getDate();
        database.getReference().child("Messages").child(mId).setValue(m);
    }

    void saveLastMsg(Message msg) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference();
        ref.child("LastM").child(msg.getContact_id() + msg.getMy_id()).setValue(msg);
    }

    public static void savePost(Post p) {
        ref.child("Posts").child(p.getUid()).setValue(p);
    }

    private void receiveCallData() {
        MainActivity.mSocket.on("server-send-call-data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            JSONObject callData = data.getJSONObject("calling");
                            String listenerId = callData.getString("listenerId");
                            String callerId = callData.getString("callerId");
                            //String callerName = callData.getString("callerName");
                            String secretCode = callData.getString("secretCode");
                            if (listenerId.equals(MainActivity.loggedUserId)) {
                                // hien thi thong bao
                                Intent intent = new Intent(getBaseContext(), InComingActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //intent.putExtra("callerName", callerName);
                                intent.putExtra("secretCode", secretCode);
                                intent.putExtra("callerId", callerId);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void sendNotification(String senderName) {
        Notification notification = new NotificationCompat.Builder(getBaseContext(), MyApplication.CHANNEL_ID)
                .setContentTitle("Messenger")
                .setContentText("Tin nhắn đến từ " + senderName)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
