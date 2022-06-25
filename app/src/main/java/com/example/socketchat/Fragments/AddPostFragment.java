package com.example.socketchat.Fragments;

import static android.app.Activity.RESULT_OK;

import static com.facebook.react.bridge.ReadableType.Map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.socketchat.MainActivity;
import com.example.socketchat.R;
import com.example.socketchat.Service.MyService;
import com.example.socketchat.models.CommentWrapper;
import com.example.socketchat.models.Like;
import com.example.socketchat.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddPostFragment extends Fragment {
    // mo thu vien chon anh, set anh cho imageView
    ImageView selectImage, selectedImage, deleteImage;
    Button postButton;
    EditText contentEdt;
    Uri uriImg;
    int REQUEST_CODE = 1;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference ref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getContext().startService(new Intent(getContext(), MyService.class));
        selectImage = view.findViewById(R.id.ic_select_img);
        selectedImage = view.findViewById(R.id.iv_selected_img);
        deleteImage = view.findViewById(R.id.iv_delete_img);
        postButton = view.findViewById(R.id.btn_post);
        contentEdt = view.findViewById(R.id.edt_content);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // chon hinh anh tu thu vien
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String uid = auth.getCurrentUser().getUid();
                String content = contentEdt.getText().toString();
                Date currentTime = Calendar.getInstance().getTime();
                long postedTime = currentTime.getTime();
                String pid = uid + postedTime;
                // chuyen hinh anh sang string
                Bitmap bitmap = ((BitmapDrawable) selectedImage.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageInByte = baos.toByteArray();
                String imgUrl = Base64.getEncoder().encodeToString(imageInByte);

                Like like = new Like();
                like.setCount(0);
                CommentWrapper cw = new CommentWrapper();
                cw.setCount(0);
                Post p = new Post(pid, uid, content, imgUrl, postedTime, like, cw);
                // luu post len firebase
                savePost(p);
            }
        });
    }

    void savePost(Post post) {
        ref = database.getReference().child("Posts").child(post.getPid());
        ref.setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HomeFragment fragment = new HomeFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.commit();
                    MainActivity.bottomNav.setSelectedItemId(R.id.item_home);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data.getData() != null) {
            uriImg = data.getData();
            selectedImage.setImageURI(uriImg);
            deleteImage.setVisibility(View.VISIBLE);
        }
    }
}
