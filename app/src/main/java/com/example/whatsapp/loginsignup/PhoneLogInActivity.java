
package com.example.whatsapp.loginsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLogInActivity extends AppCompatActivity {
    Button Verify,SendVerificationCode;
    EditText InputPhoneNumber,InputVerification;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    String mVerificationId;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    PhoneAuthProvider.ForceResendingToken   mResendToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_log_in);
        Initialise();
        mAuth=FirebaseAuth.getInstance();
        Verify.setVisibility(View.INVISIBLE);
        InputVerification.setVisibility(View.INVISIBLE);
        InputPhoneNumber.setVisibility(View.VISIBLE);
        SendVerificationCode.setVisibility(View.VISIBLE);
        progressDialog=new ProgressDialog(this);
        SendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = InputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(getApplicationContext(), "Phone number is required...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("Please wait , while we are authenticating your phone...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber("+91"+phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(PhoneLogInActivity.this)
                                    .setCallbacks(callbacks)
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                    mAuth.setLanguageCode("en");
                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Invalid Phone Number , please enter correct phone number"+e.toString(), Toast.LENGTH_SHORT).show();
                SendVerificationCode.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE );
                Verify.setVisibility(View.INVISIBLE);
                InputVerification.setVisibility(View.INVISIBLE);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(getApplicationContext(), "Invalid Request : "+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(getApplicationContext(), "Your sms limit has been expired", Toast.LENGTH_SHORT).show();
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId=verificationId;
                mResendToken=token;
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Code Sent", Toast.LENGTH_SHORT).show();
                SendVerificationCode.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                Verify.setVisibility(View.VISIBLE);
                InputVerification.setVisibility(View.VISIBLE);
            }
        };


        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationCode.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE );
                String verificationCode=InputVerification.getText().toString();
                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(getApplicationContext(), "Please write a verification code ...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("Please wait , while we are verifying verification code...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

    }

    private void Initialise() {
        Verify=findViewById(R.id.verify_btn);
        SendVerificationCode=findViewById(R.id.send_ver_code);
        InputPhoneNumber=findViewById(R.id.phone_number_input);
        InputVerification=findViewById(R.id.verification_code_input);

    }
    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                                 progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Congratulations , you are logged in successfully..", Toast.LENGTH_SHORT).show();
                            SendToMainActivity();
                        } else {

                           String message=task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), "Error is  : "+message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendToMainActivity() {
        Intent mainIntent=new Intent(PhoneLogInActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}