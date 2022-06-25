package com.example.socketchat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socketchat.Activity.CommentActivity;
import com.example.socketchat.Activity.ProfileActivity;
import com.example.socketchat.Constants;
import com.example.socketchat.R;
import com.example.socketchat.models.Notification;
import com.example.socketchat.models.Post;
import com.example.socketchat.models.User;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Post> list;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;

    public PostAdapter(Context context, ArrayList<Post> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = list.get(position);
        String time = TimeAgo.using(post.getPostedTime());
        holder.status.setText(post.getContent());
        holder.postedTime.setText(time);
        holder.like.setText(post.getLike().getCount() + "");
        holder.comment.setText(post.getComment().getCount() + "");

        // set username cho tung bai viet
        String uid = auth.getCurrentUser().getUid();
        ref = database.getReference("Users").child(post.getUid()).child("username");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                holder.username.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // lay ra link anh va chuyen sang byte array
        byte[] imgBytes = Base64.decode(post.getUrlImg(), Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
        holder.postedImg.setImageBitmap(bmp);

        // kiem tra ng dung da like bai viet hay ch
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref2 = db.getReference("Posts").child(post.getPid()).child("like").child("listUid");
        if (ref2 != null) {
            ref2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String uid = dataSnapshot.getValue(String.class);
                        if (auth.getCurrentUser().getUid().equals(uid)) {
                            holder.iconLike.setImageResource(R.drawable.favorite_fill);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        // an vao icon like
        holder.iconLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(post.getPid())
                        .child("like");
                // bai viet chua dc like
                if (holder.iconLike.getDrawable().getConstantState() == context.getResources().getDrawable(R.drawable.favorite).getConstantState()) {
                    post.getLike().setCount(post.getLike().getCount() + 1);
                    post.getLike().getListUid().add(auth.getCurrentUser().getUid());
                    holder.iconLike.setImageResource(R.drawable.favorite_fill);
                    ref.setValue(post.getLike());

                    // luu thong bao len db
                    FirebaseDatabase db1 = FirebaseDatabase.getInstance();
                    DatabaseReference newRef = db1.getReference("Notices");
                    if(newRef != null) {
                        Notification n = new Notification();
                        n.setType(Constants.LIKE_POST);
                        n.setsId(uid);
                        n.setrId(post.getUid());
                        newRef.push().setValue(n);
                    }
                } else {
                    post.getLike().setCount(post.getLike().getCount() - 1);
                    post.getLike().getListUid().remove(auth.getCurrentUser().getUid());
                    holder.iconLike.setImageResource(R.drawable.favorite);
                    ref.setValue(post.getLike());
                }

            }
        });

        holder.iconComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // chuyen id cua bai post sang man hinh comment activity
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("p_id", post.getPid());
                context.startActivity(intent);
            }
        });

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent u = new Intent(context, ProfileActivity.class);
                u.putExtra("uid", post.getUid());
                context.startActivity(u);
            }
        });

        // lay string hinh anh tu uid
        FirebaseDatabase db2 = FirebaseDatabase.getInstance();
        DatabaseReference ref3 = db2.getReference("Users").child(post.getUid());
        ref3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                String img = u.getProfileImg();
                byte[] bytes = Base64.decode(img, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.avatar.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); // end
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar, postedImg;
        TextView username, postedTime, status, like, comment;
        ImageView iconLike, iconComment;
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            postedImg = itemView.findViewById(R.id.postedImg);
            username = itemView.findViewById(R.id.tvUsername);
            postedTime = itemView.findViewById(R.id.PostedTime);
            status = itemView.findViewById(R.id.tvStatus);
            like = itemView.findViewById(R.id.tvLike);
            comment = itemView.findViewById(R.id.tvComment);
            iconLike = itemView.findViewById(R.id.ic_like);
            iconComment = itemView.findViewById(R.id.ic_comment);
        }

    }
}
