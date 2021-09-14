package com.example.whatsapp.Settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.transition.CircularPropagation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    Button update;
    ImageView DEMO;
    EditText status, username;
     CircleImageView userProfileImg;
    FirebaseAuth mauth;
    String image;
    RelativeLayout relativeLayout;
    DatabaseReference RootRef;
    public static final int Galley_Code = 1;
    public StorageReference UserProfileImageRef;
    String currentUser;
    StorageReference storageReference;
    String downloadUrl;
    ProgressDialog progressDialog;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Initialize();
        username.setVisibility(View.INVISIBLE);
        mauth = FirebaseAuth.getInstance();
        currentUser = mauth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images/" + currentUser + ".jpg");
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });
        userProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image*/");
                startActivityForResult(galleryIntent, Galley_Code);
            }
        });
        RetriveData();
    }

    public void RetriveData() {
        RootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && snapshot.hasChild("name") && snapshot.hasChild("image")) {
                    String rusername = snapshot.child("name").getValue().toString();
                    String rstatus = snapshot.child("status").getValue().toString();
                    String rprofile = snapshot.child("image").getValue().toString();
                    username.setText(rusername);
                    status.setText(rstatus);
                    image=rprofile;
                    GetImage();

                } else if ((snapshot.exists()) && snapshot.hasChild("name")) {
                    String rusername = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String rstatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    username.setText(rusername);
                    status.setText(rstatus);

                } else {
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
        String setUsername = username.getText().toString();
        String setStatus = status.getText().toString();
        if (TextUtils.isEmpty(setUsername)) {
            Snackbar.make(relativeLayout, "PLease Enter Your Username ...", Snackbar.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(setStatus)) {
            Snackbar.make(relativeLayout, "Please Enter  Your Status ...", Snackbar.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus) && TextUtils.isEmpty(setUsername)) {
            Snackbar.make(relativeLayout, "Please Enter Status and Username ...", Snackbar.LENGTH_SHORT).show();

        }
        if (!TextUtils.isEmpty(setStatus) && !TextUtils.isEmpty(setUsername)) {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUser);
            profileMap.put("name", setUsername);
            profileMap.put("status", setStatus);
            if(image!=null)
            {
                profileMap.put("image",image);
            }
            RootRef.child("Users").child(currentUser).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        SendUserMainActivity();
                    } else {
                        String errorMessage = Objects.requireNonNull(task.getException()).getLocalizedMessage();
                        Snackbar.make(relativeLayout, "Error :" + errorMessage, Snackbar.LENGTH_SHORT).show();
                        GetImage();
                    }
                }
            });


        }
    }

    private void Initialize() {
        update = findViewById(R.id.update_Settings_button);
        status = findViewById(R.id.set_user_status);
        username = findViewById(R.id.set_user_name);
        DEMO=findViewById(R.id.demoprof);
        userProfileImg = findViewById(R.id.profile_image);
        relativeLayout = findViewById(R.id.settings_relative);
        progressDialog = new ProgressDialog(this);
        toolbar=findViewById(R.id.settings_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    public void SendUserMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Galley_Code && resultCode == RESULT_OK && data != null) {
            CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Set Profile Image");
                progressDialog.setMessage("Please wait , your profile image is updating ..");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImageRef.child(currentUser + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this, "Profile Image Updated..", Toast.LENGTH_SHORT).show();

                        downloadUrl = currentUser;

                        RootRef.child("Users").child(currentUser).child("image")
                                .setValue(downloadUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            GetImage();
                                            Toast.makeText(getApplicationContext(), "Image Saved in Database , Successfully", Toast.LENGTH_SHORT).show();

                                        } else {
                                            String message = task.getException().toString();
                                            Toast.makeText(getApplicationContext(), "Error : " + message, Toast.LENGTH_SHORT).show();
                                        }
                                        progressDialog.dismiss();
                                        progressDialog.cancel();
                                    }
                                });

                    }
                });
            }

        }
    }


    public void GetImage( ) {
        storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Profile Images/" + currentUser + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(userProfileImg);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }
}