package com.example.whatsapp.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.loginsignup.LogInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    Button update;
    EditText status , username;
    CircleImageView userProfileImg;
    FirebaseAuth mauth;
    RelativeLayout relativeLayout;
    DatabaseReference RootRef;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Initialize();
        username.setVisibility(View.GONE);
        mauth= FirebaseAuth.getInstance();
        currentUser=mauth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });
        RetriveData();
    }

    private void RetriveData() {

        RootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists())  && snapshot.hasChild("name")&& snapshot.hasChild("image"))
                {
                    String rusername= snapshot.child("name").getValue().toString();
                    String rstatus=snapshot.child("status").getValue().toString();
                    String rptofileImage=snapshot.child("image").getValue().toString();
                    username.setText(rusername);
                    status.setText(rstatus);

                }
                else if((snapshot.exists())  && snapshot.hasChild("name") )
                {
                    String rusername= Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String rstatus= Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    username.setText(rusername);
                    status.setText(rstatus);
                }
                else
                {
                    username.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Please set and update your profile.....", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void UpdateSettings() {
        String setUsername=username.getText().toString();
        String setStatus=status.getText().toString();
        if(TextUtils.isEmpty(setUsername))
        {
            Snackbar.make(relativeLayout, "PLease Enter Your Username ...", Snackbar.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(setStatus))
        {
            Snackbar.make(relativeLayout,"Please Enter  Your Status ...",Snackbar.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus) && TextUtils.isEmpty(setUsername))
        {
            Snackbar.make(relativeLayout,"Please Enter Status and Username ...",Snackbar.LENGTH_SHORT).show();

        }
        if(!TextUtils.isEmpty(setStatus) &&!TextUtils.isEmpty(setUsername))
        {
            HashMap<String,String> profileMap= new HashMap<>();
            profileMap.put("uid",currentUser);
            profileMap.put("name",setUsername);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUser).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        SendUserMainActivity();
                    }
                    else
                    {
                        String errorMessage= Objects.requireNonNull(task.getException()).getLocalizedMessage();
                        Snackbar.make(relativeLayout,"Error :"+errorMessage,Snackbar.LENGTH_SHORT).show();

                    }
                }
            });


        }
    }

    private void Initialize() {
        update=findViewById(R.id.update_Settings_button);
        status=findViewById(R.id.set_user_status);
        username=findViewById(R.id.set_user_name);
        userProfileImg=findViewById(R.id.profile_image);
        relativeLayout=findViewById(R.id.settings_relative);
    }

    public void SendUserMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
      SendUserMainActivity();
    }
}