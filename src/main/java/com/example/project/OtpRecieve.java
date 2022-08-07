package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class OtpRecieve extends AppCompatActivity {

    EditText inputotp1,inputotp2,inputotp3,inputotp4,inputotp5,inputotp6;
    String getotpbackend;
    FirebaseAuth mAuth ;
    FirebaseUser mUser;
    Button btnverify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_recieve);

        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();

        btnverify=findViewById(R.id.btnverify);

        inputotp1=findViewById(R.id.inputotp1);
        inputotp2=findViewById(R.id.inputotp2);
        inputotp3=findViewById(R.id.inputotp3);
        inputotp4=findViewById(R.id.inputotp4);
        inputotp5=findViewById(R.id.inputotp5);
        inputotp6=findViewById(R.id.inputotp6);

        TextView textView =findViewById(R.id.shownumber);
        textView.setText(String.format(
                "+92%s",getIntent().getStringExtra("Mobile")

        ));

        getotpbackend=getIntent().getStringExtra("backendotp");
        final ProgressBar progressBarrecieveotp = findViewById(R.id.Progressbar_verify_otp);

        btnverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!inputotp1.getText().toString().trim().isEmpty() &&
                        !inputotp2.getText().toString().trim().isEmpty() &&
                        !inputotp3.getText().toString().trim().isEmpty() &&
                        !inputotp4.getText().toString().trim().isEmpty() &&
                        !inputotp5.getText().toString().trim().isEmpty() &&
                        !inputotp6.getText().toString().trim().isEmpty()){
                   String entercodeotp = inputotp1.getText().toString()+
                           inputotp2.getText().toString()+
                           inputotp3.getText().toString()+
                           inputotp4.getText().toString()+
                           inputotp5.getText().toString()+
                           inputotp6.getText().toString();
                   if(getotpbackend!=null){
                       progressBarrecieveotp.setVisibility(View.VISIBLE);
                       btnverify.setVisibility(View.INVISIBLE);

                       PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                               getotpbackend, entercodeotp

                       );
                       FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                   

                                   @Override
                                   public void onComplete(@NonNull Task<AuthResult> task) {
                                       progressBarrecieveotp.setVisibility(View.GONE);
                                       btnverify.setVisibility(View.VISIBLE);

                                       if(task.isSuccessful()){

                                           FirebaseUser user = mAuth.getCurrentUser();
                                           //Get user email and uid from auth
                                           if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                               String phone = user.getPhoneNumber();
                                               String uid = user.getUid();
                                               //When user is registered store user info in firebase realtime database too
                                               //Using HashMap
                                               HashMap<Object, String> hashMap = new HashMap<>();
                                               hashMap.put("email", "");
                                               hashMap.put("uid", uid);
                                               hashMap.put("name", ""); // will add later
                                               hashMap.put("onlineStatus","online");
                                               hashMap.put("typingTo","noOne");
                                               hashMap.put("phone", phone); //will add later
                                               hashMap.put("image", ""); //will add later
                                               hashMap.put("cover", "");

                                               //firebase database instance
                                               FirebaseDatabase database = FirebaseDatabase.getInstance();
                                               //path to store user data named "Users"
                                               DatabaseReference reference = database.getReference("User");
                                               //put data within hashmap in database
                                               reference.child(uid).setValue(hashMap);

                                           }


                                           Intent intent=new Intent(getApplicationContext(), PFormActivity.class);

                                           intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK|intent.FLAG_ACTIVITY_CLEAR_TASK);
                                           startActivity(intent);
                                       }else{
                                           Toast.makeText(OtpRecieve.this, "Enter the correct OTP", Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });

                   }else{
                       Toast.makeText(OtpRecieve.this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
                   }
                    // Toast.makeText(OtpRecieve.this, "OTP VERIFIED", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(OtpRecieve.this, "Please enter all numbers", Toast.LENGTH_SHORT).show();
                }


            }

        });


        numberotpmove();

        findViewById(R.id.resendotp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+92" + getIntent().getStringExtra("Mobile"),
                        60,
                        TimeUnit.SECONDS,
                        OtpRecieve.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {


                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                                Toast.makeText(OtpRecieve.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String newbackendotp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                                getotpbackend=newbackendotp;
                                Toast.makeText(OtpRecieve.this, "OTP sended Successfully", Toast.LENGTH_SHORT).show();

                            }
                        }
                );
            }
        });

    }

    private void numberotpmove() {

        inputotp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(!s.toString().trim().isEmpty()){
                inputotp2.requestFocus();
            }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputotp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputotp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputotp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputotp4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputotp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputotp5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputotp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().isEmpty()){
                    inputotp6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}