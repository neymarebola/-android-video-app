package com.example.socketchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socketchat.Activity.ChatDetailActivity;
import com.example.socketchat.Activity.VoiceChatActivity;
import com.example.socketchat.models.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_AUDIO_LEFT = 2;
    public static final int MSG_TYPE_AUDIO_RIGHT = 3;
    public static final int MSG_TYPE_IMAGE_LEFT = 4;
    public static final int MSG_TYPE_IMAGE_RIGHT = 5;

    List<Message> messageList;
    Context context;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MSG_TYPE_AUDIO_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.voice_chat_item_right, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MSG_TYPE_AUDIO_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.voice_chat_item_left, parent, false);
            return new ViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.image_chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.image_chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Message m = messageList.get(position);
        if (messageList.get(position).getType() == Constants.TEXT) {
            holder.showMessage.setText(m.getMsg_text());
            if (m.getStatus() == 0 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da gui");
            } else if (m.getStatus() == 1 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da nhan");
            } else if (m.getStatus() == 2 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da xem");
            }
        } else if (messageList.get(position).getType() == Constants.AUDIO) {
            if (m.getStatus() == 0 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da gui");
            } else if (m.getStatus() == 1 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da nhan");
            } else if (m.getStatus() == 2 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da xem");
            }
            holder.playButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    String data = messageList.get(position).getMsg_text();
                    byte[] audio = Base64.getDecoder().decode(data);
                    playMp3FromByte(audio);
                }
            });
        } else if (messageList.get(position).getType() == Constants.IMAGE) {
            byte[] hinhanh = Base64.getDecoder().decode(messageList.get(position).getMsg_text());
            Bitmap bitmap = BitmapFactory.decodeByteArray(hinhanh, 0,
                    hinhanh.length);
            holder.sentImage.setImageBitmap(bitmap);
            if (m.getStatus() == 0 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da gui");
            } else if (m.getStatus() == 1 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da nhan");
            } else if (m.getStatus() == 2 && m.getMy_id().equals(MainActivity.loggedUserId)) {
                holder.statusMsg.setText("da xem");
            }
        }
    }

    public void playMp3FromByte(byte[] mp3SoundByteArray) {
        try {
            File tempMp3 = File.createTempFile("kurchina", "mp3", context.getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            FileInputStream fis = new FileInputStream(tempMp3);
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //ImageView profileImage;
        TextView showMessage;
        ImageView playButton;
        TextView time;
        ImageView sentImage;
        TextView statusMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.chat_msg);
            playButton = itemView.findViewById(R.id.playIcon);
            time = itemView.findViewById(R.id.timeIcon);
            sentImage = itemView.findViewById(R.id.image_chat_item);
            statusMsg = itemView.findViewById(R.id.tvStatusMsg);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getMy_id().equals(MainActivity.loggedUserId)) {
            if (messageList.get(position).getType() == Constants.TEXT) {
                return MSG_TYPE_RIGHT;
            } else if (messageList.get(position).getType() == Constants.AUDIO)
                return MSG_TYPE_AUDIO_RIGHT;
            else if (messageList.get(position).getType() == Constants.IMAGE)
                return MSG_TYPE_IMAGE_RIGHT;
        } else if (messageList.get(position).getContact_id().equals(MainActivity.loggedUserId)) {
            if (messageList.get(position).getType() == Constants.TEXT) {
                return MSG_TYPE_LEFT;
            } else if (messageList.get(position).getType() == Constants.AUDIO)
                return MSG_TYPE_AUDIO_LEFT;
            else if (messageList.get(position).getType() == Constants.IMAGE)
                return MSG_TYPE_IMAGE_LEFT;
        }
        return -1;
    }
}
