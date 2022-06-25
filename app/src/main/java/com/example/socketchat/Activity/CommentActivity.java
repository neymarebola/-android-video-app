package com.example.socketchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.socketchat.Adapters.CommentAdapter;
import com.example.socketchat.Constants;
import com.example.socketchat.R;
import com.example.socketchat.models.Comment;
import com.example.socketchat.models.CommentWrapper;
import com.example.socketchat.models.Notification;
import com.example.socketchat.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    //
    EditText commentInput;
    ImageView sendComment;
    FirebaseDatabase database;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference ref;

    List<Comment> listC = new ArrayList<>();
    RecyclerView cmtRec;
    CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_comment);
        initView();

        String postId = getIntent().getStringExtra("p_id");
        database = FirebaseDatabase.getInstance();
        LinearLayoutManager manager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        cmtRec.setLayoutManager(manager);
        adapter = new CommentAdapter(this, listC);
        cmtRec.setAdapter(adapter);
        getListComments(postId);

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref = database.getReference("Posts").child(postId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        String cmt = commentInput.getText().toString();
                        String uid = auth.getCurrentUser().getUid();
                        long time = new Date().getTime();
                        post.getComment().setCount(post.getComment().getCount() + 1);
                        Comment comment = new Comment(uid, cmt, postId, time);
                        post.getComment().getList().add(comment);
                        saveComment(postId, post);

                        Notification n = new Notification();
                        n.setType(Constants.COMMENT_POST);
                        n.setsId(uid);
                        n.setrId(post.getUid());
                        sendNotification(n);
                        commentInput.setText("");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    void sendNotification(Notification n) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("Notices");
        ref.push().setValue(n);
    }

    void getListComments(String pid) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("Posts").child(pid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post p = snapshot.getValue(Post.class);
                listC.clear();
                listC.addAll(p.getComment().getList());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void saveComment(String pid, Post post) {
        DatabaseReference ref = database.getReference("Posts").child(pid);
        ref.setValue(post);

    }

    void initView() {
        commentInput = findViewById(R.id.edt_comment);
        sendComment = findViewById(R.id.btn_send_comment);
        cmtRec = findViewById(R.id.comment_rec);
    }
}