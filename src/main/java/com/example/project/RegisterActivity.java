package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {
    TextView alreadyHaveAccount;

    EditText inputEmail, inputName,inputPhone,CNIC,inputPassword,inputConfirmPassword;
    Button btnRegister;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

        alreadyHaveAccount=findViewById(R.id.alreadyHaveAccount);

        inputEmail=findViewById(R.id.inputEmail);
        inputName=findViewById(R.id.inputName);
        inputPhone=findViewById(R.id.inputPhone);
        CNIC=findViewById(R.id.CNIC);
        inputPassword=findViewById(R.id.inputPassword);
        inputConfirmPassword=findViewById(R.id.inputConfirmPassword);
        btnRegister=findViewById(R.id.btnRegister);
        progressDialog=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerforAuth();
            }
        });
    }

    private void PerforAuth() {

       final String email=inputEmail.getText().toString();
        final String phone=inputPhone.getText().toString();
        final String name=inputName.getText().toString();
        final String cnic =CNIC.getText().toString();
        String  password = inputPassword.getText().toString();
        String confirmPasword = inputConfirmPassword.getText().toString();



        if(!email.matches(emailPattern)){
            inputEmail.setError("Enter Correct Email");
        } else if(name.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();

        }else if(phone.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();

        } else if(phone.length()<=10 || phone.length()>=12) {
            Toast.makeText(RegisterActivity.this, "Please enter correct number", Toast.LENGTH_SHORT).show();
        }else if(cnic.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Enter CNIC Number", Toast.LENGTH_SHORT).show();

        } else if(cnic.length()<=12 || phone.length()>=14) {
            Toast.makeText(RegisterActivity.this, "Please enter correct number", Toast.LENGTH_SHORT).show();
        }else if(password.isEmpty() || password.length()<6){
            inputPassword.setError("Enter proper Password ");
        }else if(!password.equals(confirmPasword)){
            inputConfirmPassword.setError("Password not matched");
        }else{
            progressDialog.setMessage("Please wait while Registration.....");
            progressDialog.setTitle("Registration");
            //boolean flase = false;
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
               if(task.isSuccessful()){
                   progressDialog.dismiss();
                   FirebaseUser user = mAuth.getCurrentUser();

                   FirebaseDatabase database = FirebaseDatabase.getInstance();
                   //path to store user data named "Users"
                   DatabaseReference reference = database.getReference("User");
                   //Get user email and uid from auth

                   String uid = user.getUid();

                   //When user is registered store user info in firebase realtime database too
                   //Using HashMap
                   HashMap<Object, String> hashMap = new HashMap<>();
                   hashMap.put("email", email);
                   hashMap.put("uid", uid);
                   hashMap.put("name", name); // will add later
                   hashMap.put("onlineStatus","online");
                   hashMap.put("typingTo","noOne");
                   hashMap.put("phone", phone); //will add later
                   hashMap.put("cnic",cnic);
                   hashMap.put("image", "");
                   hashMap.put("cover", "");//will add later

                   //firebase database instance

                   //put data within hashmap in database
                   reference.child(uid).setValue(hashMap);


                   sendUserToNextActivity();
                   Toast.makeText(RegisterActivity.this, "Registraton Successful", Toast.LENGTH_SHORT).show();


               }else{
                   progressDialog.dismiss();
                   Toast.makeText(RegisterActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
               }
                }
            });
        }
    }

    private void sendUserToNextActivity() {
        Intent intent= new Intent(RegisterActivity.this,HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
    }
}