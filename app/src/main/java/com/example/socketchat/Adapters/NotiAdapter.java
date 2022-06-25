package com.example.socketchat.Adapters;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socketchat.Constants;
import com.example.socketchat.R;
import com.example.socketchat.models.Notification;
import com.example.socketchat.models.User;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.ViewHolder> {
    Context context;
    List<Notification> list;

    public NotiAdapter(Context context, List<Notification> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.noti_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = list.get(position);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User u = dataSnapshot.getValue(User.class);
                    if (u.getId().equals(notification.getsId())) {
                        holder.name.setText(u.getUsername());
                        String img = u.getProfileImg();
                        byte[] bytes = android.util.Base64.decode(img, android.util.Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.avatar.setImageBitmap(bmp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        int type = notification.getType();
        if (type == Constants.LIKE_POST) {
            holder.content.setText("liked your post");
        } else if (type == Constants.COMMENT_POST) {
            holder.content.setText("commented your post");
        }
        holder.time.setText("just now");
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name, content, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar_noti);
            name = itemView.findViewById(R.id.name_noti);
            content = itemView.findViewById(R.id.content_noti);
            time = itemView.findViewById(R.id.time_noti);
        }
    }
}
