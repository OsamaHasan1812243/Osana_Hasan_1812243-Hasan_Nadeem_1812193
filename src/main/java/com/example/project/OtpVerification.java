package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class OtpVerification extends AppCompatActivity {
    EditText InputNumber;
    Button btnOTP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        InputNumber=findViewById(R.id.InputNumber);
        btnOTP= findViewById(R.id.btnOTP);
       final ProgressBar progressBar =findViewById(R.id.Progressbar_sending_otp);

        btnOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!InputNumber.getText().toString().trim().isEmpty()){
                    if(InputNumber.getText().toString().trim().length()==10){
                        progressBar.setVisibility(View.VISIBLE);
                        btnOTP.setVisibility(View.INVISIBLE);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+92" + InputNumber.getText().toString(),
                                60,
                                TimeUnit.SECONDS,
                                OtpVerification.this,
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        progressBar.setVisibility(View.GONE);
                                        btnOTP.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        progressBar.setVisibility(View.GONE);
                                        btnOTP.setVisibility(View.INVISIBLE);
                                        Toast.makeText(OtpVerification.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String backendotp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        Intent intent = new Intent(getApplicationContext(),OtpRecieve.class);
                                        intent.putExtra("Mobile",InputNumber.getText().toString());
                                        intent.putExtra("backendotp",backendotp);
                                        startActivity(intent);
                                    }
                                }
                        );


                    }else{
                        Toast.makeText(OtpVerification.this, "Please enter correct number", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(OtpVerification.this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}