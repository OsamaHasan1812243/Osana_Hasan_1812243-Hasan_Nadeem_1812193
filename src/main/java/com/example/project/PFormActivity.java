package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class PFormActivity extends AppCompatActivity {
    EditText inputEmail, inputName,inputPhone,CNIC;
    Button Save;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_form);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);


        inputName=findViewById(R.id.inputName);
        inputEmail=findViewById(R.id.inputEmail);
        CNIC=findViewById(R.id.CNIC);
        Save=findViewById(R.id.Save);
        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerforAuth();
            }
        });


    }


    private void PerforAuth() {

        final String email=inputEmail.getText().toString();
        final String name=inputName.getText().toString();
        final String cnic =CNIC.getText().toString();

        if(name.isEmpty() && email.isEmpty() && cnic.isEmpty()) {
            Toast.makeText(PFormActivity.this, "Enter Your Information", Toast.LENGTH_SHORT).show();

        }else  if(!email.matches(emailPattern)){
            inputEmail.setError("Enter Correct Email");
        }else if(cnic.isEmpty()) {
            Toast.makeText(PFormActivity.this, "Enter CNIC Number", Toast.LENGTH_SHORT).show();

        } else if(cnic.length()<=12 || cnic.length()>=14) {
            Toast.makeText(PFormActivity.this, "Please enter correct number", Toast.LENGTH_SHORT).show();
        }else {

            String phone = mUser.getPhoneNumber();
            String uid = mUser.getUid();
            //When user is registered store user info in firebase realtime database too
            //Using HashMap
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("email", email);
            hashMap.put("uid", uid);
            hashMap.put("name", name); // will add later
            hashMap.put("phone", phone);
            hashMap.put("onlineStatus","online");
            hashMap.put("typingTo","noOne");//will add later
            hashMap.put("image", ""); //will add later
            hashMap.put("cover", "");

            //firebase database instance
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            //path to store user data named "Users"
            DatabaseReference reference = database.getReference("User");
            //put data within hashmap in database
            reference.child(uid).setValue(hashMap);

            Toast.makeText(PFormActivity.this, "Information Saved", Toast.LENGTH_SHORT).show();
            sendUser();


        }

    }
    private void sendUser() {
        Intent intent= new Intent(PFormActivity.this,HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}


