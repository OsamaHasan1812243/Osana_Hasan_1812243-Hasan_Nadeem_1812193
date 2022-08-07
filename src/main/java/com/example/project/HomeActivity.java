package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.fragment.app.FragmentTransaction;

import com.example.project.Fragments.ChatListFragment;
import com.example.project.Fragments.HomeFragment;
import com.example.project.Fragments.ProfileFragment;
import com.example.project.Fragments.UserFragment;
import com.example.project.Notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    BottomNavigationView navigationView;

    String mUID, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

        firebaseAuth=FirebaseAuth.getInstance();
        navigationView=findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();
        checkUserStatus();
        updateToken();


    }

    private void updateToken() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    protected void onResume(){
        checkUserStatus();
        super.onResume();
    }




    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.home:
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();

                            return true;
                        case R.id.profile:
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.user:
                            UserFragment fragment3 = new UserFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                        case R.id.chat:
                            ChatListFragment fragment4 = new ChatListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;
                        case R.id.logout:
                            firebaseAuth.signOut();
                            signOutUser();
                            return true;

                    }
                    return false;
                }

                private void signOutUser() {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            };

        private void checkUserStatus(){
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                mUID = user.getUid();

                SharedPreferences sp =getSharedPreferences("SP_USER", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("Current_USERID", mUID);
                editor.apply();

            }/*else{
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }*/
        }


}