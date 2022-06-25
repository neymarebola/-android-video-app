package com.example.socketchat.Fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.example.socketchat.Adapters.NotiAdapter;
import com.example.socketchat.MyApplication;
import com.example.socketchat.R;
import com.example.socketchat.Service.MyService;
import com.example.socketchat.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    // hien thi nhung thong bao cua minh
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference ref;
    RecyclerView notiRec;
    List<Notification> list;
    NotiAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notiRec = view.findViewById(R.id.noti_rec);

        list = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        notiRec.setLayoutManager(manager);
        adapter = new NotiAdapter(getContext(), list);
        notiRec.setAdapter(adapter);

        // lang nghe hien thi cac thong bao cua minh
        ref = database.getReference("Notices");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification n = dataSnapshot.getValue(Notification.class);
                    if (n.getrId().equals(auth.getCurrentUser().getUid())) {
                        list.add(n);
                        //sendReactNotification(n.getsId());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendReactNotification(String senderName) {
        android.app.Notification notification = new NotificationCompat.Builder(getContext(), MyApplication.CHANNEL_ID)
                .setContentTitle("Messenger")
                .setContentText(senderName + " react about your post")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .build();
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, notification);
        }
    }

}
