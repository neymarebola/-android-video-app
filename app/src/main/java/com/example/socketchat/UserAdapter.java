package com.example.socketchat;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socketchat.Activity.ChatDetailActivity;
import com.example.socketchat.Service.MyService;
import com.example.socketchat.models.Message;
import com.example.socketchat.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    List<User> listUsers;
    Context context;

    public UserAdapter(List<User> listUsernames, Context context) {
        this.listUsers = listUsernames;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = listUsers.get(position);
        holder.username.setText(user.getUsername());

        Message lastM = new Message();
        if(!MyService.unReadMessages.isEmpty()) {
            lastM = MyService.unReadMessages.get(MyService.unReadMessages.size() - 1);
        }
        if(lastM != null && user.getId().equals(lastM.getMy_id()) && lastM.getType() == Constants.TEXT) {
            holder.newEstMsg.setVisibility(View.VISIBLE);
            holder.newEstMsg.setText(lastM.getMsg_text());
        } else if(lastM != null && user.getId().equals(lastM.getMy_id()) && lastM.getType() == Constants.AUDIO) {
            holder.newEstMsg.setVisibility(View.VISIBLE);
            holder.newEstMsg.setText("audio");
        } else if(lastM != null && user.getId().equals(lastM.getMy_id()) && lastM.getType() == Constants.IMAGE) {
            holder.newEstMsg.setVisibility(View.VISIBLE);
            holder.newEstMsg.setText("image");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("contact_user", user);
                context.startActivity(intent);
            }
        });
        holder.countUnReadMsg.setText(String.valueOf(MyService.unReadMessages.size()));
    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView status;
        TextView newEstMsg, countUnReadMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvUsername);
            status = itemView.findViewById(R.id.tvStatus);
            newEstMsg = itemView.findViewById(R.id.tvNewestMsg);
            countUnReadMsg = itemView.findViewById(R.id.tvCountUnReadMsg);
        }
    }
}
