package com.example.whatsapp.loginsignup;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    EditText regEmail,regPassword;
    TextView alreadyHaveAccount;
    Button createAccountBtn;
    RelativeLayout relativeLayout;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    DatabaseReference RootRef ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Initialisation();
        auth = FirebaseAuth.getInstance();
       RootRef = FirebaseDatabase.getInstance().getReference();
        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserLogInActivity();
            }
        });
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    public void createNewAccount() {
        String userEmail=regEmail.getText().toString();
        String userPassword=regPassword.getText().toString();
        if(TextUtils.isEmpty(userEmail))
        {
             Snackbar.make(relativeLayout, "PLease Enter Your Email ...", Snackbar.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(userPassword))
        {
            Snackbar.make(relativeLayout,"Please Enter Password ...",Snackbar.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPassword))
        {
             Snackbar.make(relativeLayout,"Please Enter Email and Password ...",Snackbar.LENGTH_SHORT).show();

        }
        if(!TextUtils.isEmpty(userEmail) &&!TextUtils.isEmpty(userPassword))
        {
            progressDialog.setTitle("Creating New Account")        ;
            progressDialog.setMessage("PLease Wait , While we are creating new account for you ...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            final String[] deviceToken = new String[1];
            auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override

                public void onComplete(@NonNull Task<AuthResult> task) {
                         if(task.isSuccessful())
                         {
                             final String[] deviceToken = new String[1];
                             FirebaseMessaging.getInstance().getToken()
                                     .addOnCompleteListener(new OnCompleteListener<String>() {
                                         @Override
                                         public void onComplete(@NonNull Task<String> task) {
                                             deviceToken[0] = task.getResult();

                                         }
                                     });

                             String currentUserID= auth.getCurrentUser().getUid();
                             RootRef.child("Users").child(currentUserID).setValue("");
                             RootRef.child("Users").child(currentUserID).child("device_token")
                                     .setValue(deviceToken[0]);

                             SendUserMainActivity();
                             Toast.makeText(getApplicationContext() , "Account Created Successfully !" + currentUserID, Toast.LENGTH_SHORT).show();                   }
                         else
                         {
                                String errorMessage= Objects.requireNonNull(task.getException()).getLocalizedMessage();
                                Snackbar.make(relativeLayout,"Error :"+errorMessage,Snackbar.LENGTH_SHORT).show();

                         }
                         progressDialog.cancel();
                         progressDialog.dismiss();
                }
            }) ;
        }
    }

    public void SendUserMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    public  void Initialisation() {
        regEmail=findViewById(R.id.reg_email);
        regPassword=findViewById(R.id.reg_password);
        alreadyHaveAccount=findViewById(R.id.already_have_Account);
        createAccountBtn=findViewById(R.id.reg_button);
        relativeLayout=findViewById(R.id.regRelativeLayout);
        progressDialog=new ProgressDialog(this);
    }
    public  void SendUserLogInActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this, LogInActivity.class);
        startActivity(mainIntent);
    }
}