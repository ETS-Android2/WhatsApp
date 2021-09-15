package com.example.whatsapp.loginsignup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LogInActivity extends AppCompatActivity {

    private Button LogInBtn,PhoneLogInBtn;
    EditText email,password;
    TextView forgotPassword,needNewAccount;
    RelativeLayout relativeLayout;
    FirebaseAuth mauth;
    DatabaseReference UsersRef;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        InitialiseFiels();
        mauth=FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        needNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendtoRegActivity();
            }
        });
        LogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogIn();
            }
        });
        PhoneLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendPhoneLogInActivity();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLinkonMail();
            }
        });
    }
    public void sendLinkonMail()
    {
        if(email.getText().toString().length()>8 && email.getText().toString().matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$"))
        {
            AlertDialog.Builder passwordreset= new AlertDialog.Builder(this);
            passwordreset.setTitle("Reset Password?");
            passwordreset.setMessage("Press YES to receive the reset link");
            passwordreset.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String resetemail = email.getText().toString();
                    mauth.sendPasswordResetEmail(resetemail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"Email reset link sent please check your mail",Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(e instanceof FirebaseNetworkException)
                            { Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();}
                            Toast.makeText(getApplicationContext(),"Email reset link not sent as no user exist by this email",Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });
            passwordreset.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            passwordreset.create().show();

        }
        else
        {email.setError("Enter a valid email");}

    }

    private void SendPhoneLogInActivity() {
        Intent mainIntent=new Intent(LogInActivity.this,PhoneLogInActivity.class);
        startActivity(mainIntent);
    }

    private void AllowUserToLogIn()
    {
        String userEmail=email.getText().toString();
        String userPassword=password.getText().toString();
        if(TextUtils.isEmpty(userEmail))
        {
            email.setError("Please Enter Email Id");

        }
        if(TextUtils.isEmpty(userPassword))
        {
            password.setError("Please set Password");
        }
        if(TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPassword))
        {
            email.setError("Please Enter Email Id");
            password.setError("Please set Password");


        }
        if(!TextUtils.isEmpty(userEmail) &&!TextUtils.isEmpty(userPassword)) {
            progressDialog.setTitle("Signing In")        ;
            progressDialog.setMessage("Please Wait ...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            mauth.signInWithEmailAndPassword(userEmail,userPassword)
           .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful())
                   {
                       String currentUserId = mauth.getCurrentUser().getUid();
                       final String[] deviceToken = new String[1];
                       FirebaseMessaging.getInstance().getToken()
                               .addOnCompleteListener(new OnCompleteListener<String>() {
                                   @Override
                                   public void onComplete(@NonNull Task<String> task) {
                                       deviceToken[0] = task.getResult();

                                   }
                               });

                       UsersRef.child(currentUserId).child("device_token")
                               .setValue(deviceToken[0])

                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task)
                                   {
                                       if (task.isSuccessful())
                                       {
                                           SendUserMainActivity();
                                           Toast.makeText(getApplicationContext(), "Logged In Successfully !!", Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });


                   }
                   else
                   {
                       String errorMessage=task.getException().getLocalizedMessage();
                       Snackbar.make(relativeLayout,"Error :"+errorMessage,Snackbar.LENGTH_SHORT).show();

                   }
                   progressDialog.cancel();
                   progressDialog.dismiss();
               }
           });
        }

    }

    private void InitialiseFiels() {
        LogInBtn=findViewById(R.id.login_button);
        PhoneLogInBtn=findViewById(R.id.phone_login_button);
        email=findViewById(R.id.login_email);
        password=findViewById(R.id.login_password);
        forgotPassword=findViewById(R.id.forgot_password);
        needNewAccount=findViewById(R.id.newAccount);
        relativeLayout=findViewById(R.id.logIn_relative_layout);
        progressDialog=new ProgressDialog(this);
    }



    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    public void SendUserMainActivity() {
        Intent mainIntent=new Intent(LogInActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
   public void SendtoRegActivity()
    {
        Intent regIntent=new Intent(LogInActivity.this, RegisterActivity.class);
        startActivity(regIntent);

    }
}