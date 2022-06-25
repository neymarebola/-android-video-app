package com.example.socketchat.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socketchat.Adapters.ContactAdapter;
import com.example.socketchat.MainActivity;
import com.example.socketchat.R;
import com.example.socketchat.Service.MyService;
import com.example.socketchat.UserAdapter;
import com.example.socketchat.models.Message;
import com.example.socketchat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MessageFragment extends Fragment {
    List<User> listUsers;
    RecyclerView usersRec;
    TextView logged_user;
    ImageView avatar;
    public static String loggedUserId = FirebaseAuth.getInstance().getUid();
    private List<Integer> tmp = new ArrayList<>();
    FirebaseDatabase mdatabase = FirebaseDatabase.getInstance();
    private Socket socket = MainActivity.mSocket;

    List<Message> list = new ArrayList<>();
    ContactAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatar = view.findViewById(R.id.avatar_msg_fragment);
        logged_user = view.findViewById(R.id.tvLoggedUser);
        usersRec = view.findViewById(R.id.listContact);

        mdatabase.getReference("Users").child(loggedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                logged_user.setText(u.getUsername());
                String img = u.getProfileImg();
                byte[] bytes = Base64.decode(img, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        sendUserDataToServer(loggedUserId);
        //showListUserFb();
        //getContext().startService(new Intent(getContext(), MyService.class));

        // hien thi danh sach nhung nguoi da gui tin nhan
        list = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        usersRec.setLayoutManager(manager);
        adapter = new ContactAdapter(getContext(), list);
        usersRec.setAdapter(adapter);
        showContact();
    }


    public void sendUserDataToServer(String loggedUserId) {
        socket.emit("client_send_user_data", loggedUserId);
    }

    // hien thi danh sach tat ca nguoi dung cung voi tin nhan cuoi cung tren firebase
    private void showContact() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("LastM");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message m = dataSnapshot.getValue(Message.class);
                    if (m.getContact_id().equals(loggedUserId) || m.getMy_id().equals(loggedUserId)) {
                        list.add(m);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showListUserFb() {
        mdatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User u = dataSnapshot.getValue(User.class);
                    if(!u.getId().equals(MainActivity.loggedUserId))
                        listUsers.add(u);
                }

                List<String> listId = new ArrayList<>();
                socket.emit("get_list_online_user_id", "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public boolean checkUserOnline(List<String> listId, String uid) {
        boolean res = false;
        for (String i : listId) {
            if (i.equals(uid)) {
                res = true;
                break;
            }
        }
        return res;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        return view;
    }
}
