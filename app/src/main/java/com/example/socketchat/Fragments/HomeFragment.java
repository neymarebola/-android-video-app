package com.example.socketchat.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.socketchat.Activity.SearchActivity;
import com.example.socketchat.Adapters.PostAdapter;
import com.example.socketchat.MainActivity;
import com.example.socketchat.R;
import com.example.socketchat.models.Post;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;

public class HomeFragment extends Fragment {
    //  hien thi tat ca cac bai post tu firebase
    RecyclerView postRec;
    PostAdapter adapter;
    ArrayList<Post> list;
    FirebaseDatabase db;
    DatabaseReference ref;
    ImageView search;
    ShimmerFrameLayout shimmerFrameLayout;
    Socket socket = MainActivity.mSocket;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postRec = view.findViewById(R.id.post_rec);
        search = view.findViewById(R.id.profile_search);
        shimmerFrameLayout = view.findViewById(R.id.shimmer);
        shimmerFrameLayout.startShimmer();

        socket.connect();
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        postRec.setLayoutManager(manager);
        list = new ArrayList<>();
        adapter = new PostAdapter(getContext(), list);
        postRec.setAdapter(adapter);
        showListPosts();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SearchActivity.class));
            }
        });
    }

    void showListPosts() {
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post p = dataSnapshot.getValue(Post.class);
                    list.add(p);
                }
                adapter.notifyDataSetChanged();
                shimmerFrameLayout.stopShimmer();
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shimmerFrameLayout.setVisibility(View.GONE);
                    }
                }, 1000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
