package com.example.socketchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socketchat.Adapters.PostAdapter;
import com.example.socketchat.MainActivity;
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
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference ref;
    String uid;
    ImageView avatar;
    TextView name, slFollow, slFollower;
    RecyclerView rec;
    PostAdapter adapter;
    ArrayList<Post> list;
    CardView follow, message;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_profile);
        initView();

        database = FirebaseDatabase.getInstance();
        uid = getIntent().getStringExtra("uid");

        if (uid.equals(auth.getCurrentUser().getUid())) {
            follow.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
        }

        // lay ve cac bai post cua minh
        list = new ArrayList<>();
        adapter = new PostAdapter(this, list);
        rec.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rec.setLayoutManager(manager);

        database.getReference("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                name.setText(u.getUsername());
                slFollow.setText(u.getFollow() + "");
                slFollower.setText(u.getFollower() + "");
                String img = u.getProfileImg();
                byte[] bytes = Base64.decode(img, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref = database.getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
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

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference ref = db.getReference("Users").child(uid);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User u = snapshot.getValue(User.class);
                        Intent i = new Intent(getBaseContext(), ChatDetailActivity.class);
                        i.putExtra("contact_uid", u.getId());
                        startActivity(i);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // so follow cua mk tang len 1
                DatabaseReference ref = database.getReference("Users").child(auth.getCurrentUser().getUid());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User u = snapshot.getValue(User.class);
                        u.setFollow(u.getFollow() + 1);
                        ref.setValue(u);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                // tang so follower cua nguoi kia len 1
                DatabaseReference ref2 = database.getReference("Users").child(uid);
                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User u = snapshot.getValue(User.class);
                        u.setFollower(u.getFollower() + 1);
                        ref2.setValue(u);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                follow.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_slide, menu);

        return super.onCreateOptionsMenu(menu);
    }

    void initView() {
        avatar = findViewById(R.id.profile_avatar2);
        name = findViewById(R.id.profile_name);
        rec = findViewById(R.id.profile_rec);
        follow = findViewById(R.id.card_follow);
        message = findViewById(R.id.card_message);
        slFollow = findViewById(R.id.tv_follow);
        slFollower = findViewById(R.id.tv_follower);
    }
}