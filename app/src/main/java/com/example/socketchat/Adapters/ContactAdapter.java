package com.example.socketchat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socketchat.Activity.ChatDetailActivity;
import com.example.socketchat.R;
import com.example.socketchat.models.Message;
import com.example.socketchat.models.User;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    Context context;
    List<Message> list = new ArrayList<>();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;

    public ContactAdapter(Context context, List<Message> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = list.get(position);
        String myId = auth.getCurrentUser().getUid();
        String contactId;
        if(message.getMy_id() == myId) {
            contactId = message.getContact_id();
        } else {
            contactId = message.getMy_id();
        }
        // lay ten theo id
        ref = database.getReference("Users").child(contactId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                holder.name.setText(u.getUsername());
                String img = u.getProfileImg();
                byte[] bytes = android.util.Base64.decode(img, android.util.Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.avatar.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        if (message.getMy_id().equals(auth.getCurrentUser().getUid())) {
            holder.message.setText("You: " + message.getMsg_text());
        } else holder.message.setText(message.getMsg_text());
        String tgian = TimeAgo.using(message.getDate());
        holder.time.setText(tgian);
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ChatDetailActivity.class);
                i.putExtra("contact_uid", contactId);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name, message, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.contact_avatar);
            name = itemView.findViewById(R.id.contact_name);
            message = itemView.findViewById(R.id.contact_msg);
            time = itemView.findViewById(R.id.contact_time);
        }
    }
}
