package com.example.socketchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.example.socketchat.Fragments.AccountFragment;
import com.example.socketchat.Fragments.AddPostFragment;
import com.example.socketchat.Fragments.HomeFragment;
import com.example.socketchat.Fragments.MessageFragment;
import com.example.socketchat.Fragments.NotificationFragment;
import com.example.socketchat.Service.MyService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.net.URISyntaxException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {
    public static Socket mSocket;
    public static String loggedUserId = FirebaseAuth.getInstance().getUid();
    public static BottomNavigationView bottomNav;


    {
        try {
            mSocket = IO.socket("http://192.168.1.16:3000"); // localhost
            //mSocket = IO.socket("https://vutheduong.herokuapp.com/");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        //mSocket.connect();

        bottomNav = findViewById(R.id.bottom_nav);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.item_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.item_add:
                        selectedFragment = new AddPostFragment();
                        break;
                    case R.id.item_message:
                        selectedFragment = new MessageFragment();
                        break;
                    case R.id.item_profile:
                        selectedFragment = new AccountFragment();
                        break;
                    case R.id.item_notification:
                        selectedFragment = new NotificationFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });

    }

    public void sendUserDataToServer(String loggedUserId) {
        mSocket.emit("client_send_user_data", loggedUserId);
    }

    public boolean checkUserOnline(List<String> listId, String uid) {
        boolean res = false;
        for (String i : listId) {
            if (i.equals(uid)) {
                res = true;
                break;
            }
        }
        return res;
    }
}