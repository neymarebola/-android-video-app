package com.example.socketchat.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socketchat.Activity.ProfileActivity;
import com.example.socketchat.Activity.SignInActivity;
import com.example.socketchat.Adapters.PostAdapter;
import com.example.socketchat.R;
import com.example.socketchat.models.Post;
import com.example.socketchat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccountFragment extends Fragment {
    // luu 1 list cac string len firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    ImageView avatar, more;
    TextView name, follow, follower;
    RecyclerView profileRec;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    ArrayList<Post> list = new ArrayList<>();
    PostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatar = view.findViewById(R.id.account_avatar);
        name = view.findViewById(R.id.account_name);
        more = view.findViewById(R.id.ic_more2);
        follow = view.findViewById(R.id.tv_follow_acc);
        follower = view.findViewById(R.id.tv_follower_acc);
        profileRec = view.findViewById(R.id.account_rec);

        database.getReference("Users").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                name.setText(u.getUsername());
                follow.setText(u.getFollow() + "");
                follower.setText(u.getFollower() + "");
                String img = u.getProfileImg();
                byte[] bytes = Base64.decode(img, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adapter = new PostAdapter(getContext(), list);
        profileRec.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        profileRec.setLayoutManager(manager);
        ref = database.getReference("Post");
        showListPosts(auth.getCurrentUser().getUid());
        //getMyListPosts(auth.getCurrentUser().getUid());

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "click", Toast.LENGTH_SHORT).show();
                PopupMenu pm = new PopupMenu(getContext(), more);
                pm.getMenuInflater().inflate(R.menu.menu_slide, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.item_logout:
                                auth.signOut();
                                startActivity(new Intent(getContext(), SignInActivity.class));
                                return true;
                        }

                        return true;
                    }
                });
                pm.show();
            }
        });
    }
    void showListPosts(String uid) {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post p = dataSnapshot.getValue(Post.class);
                    if(p.getUid().equals(uid)) {
                        list.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
