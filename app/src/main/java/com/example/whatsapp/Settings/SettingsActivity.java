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
import android.provider.Settings;
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
import com.example.whatsapp.loginsignup.LogInActivity;
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
                if(!username.getText().toString().isEmpty() && !status.getText().toString().isEmpty()) {
                sendUserToMainActivity();
                }
            }
        });
        userProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!username.getText().toString().isEmpty() && !status.getText().toString().isEmpty()) {
                    UpdateSettings();
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(
                                    intent,
                                    "Select Image from here..."),
                            Galley_Code);
                }
                else
                {
                    username.setError("Please Enter username First");
                    status.setError("Please Enter status First");
                }
            }
        });
        RetriveData();
    }

    public void RetriveData() {
        RootRef.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && snapshot.hasChild("image")) {
                    String rprofile = snapshot.child("image").getValue().toString();
                    image=rprofile;
                    GetImage();
                    String rusername = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String rstatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    username.setText(rusername);
                    status.setText(rstatus);

                }
                else if ((snapshot.exists()) && snapshot.hasChild("name")) {
                    String rusername = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String rstatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    username.setText(rusername);
                    status.setText(rstatus);

                } else {
                    username.setVisibility(View.VISIBLE);
                    Snackbar.make(relativeLayout, "Please set and update your profile.....", Snackbar.LENGTH_SHORT).show();
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
            username.setError("Please username First");

        }
        if (TextUtils.isEmpty(setStatus)) {
            status.setError("Please Enter status First");
        }
        if (TextUtils.isEmpty(setStatus) && TextUtils.isEmpty(setUsername)) {
            username.setError("Please Enter username");
            status.setError("Please Enter statust");

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
                        Toast.makeText(getApplicationContext(), "Data Updated..", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = Objects.requireNonNull(task.getException()).getLocalizedMessage();
                        Snackbar.make(relativeLayout, "Error :" + errorMessage, Snackbar.LENGTH_SHORT).show();
                        GetImage();
                    }
                }
            });


        }
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
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
                    filePath.putFile(resultUri)
                            .addOnSuccessListener(
                                    taskSnapshot -> {
                                        progressDialog.dismiss();
                                        GetImage();
                                        RootRef.child("Users").child(currentUser).child("image").setValue(currentUser);
                                        Toast.makeText(SettingsActivity.this,
                                                "Image Uploaded!!",
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    });
                }

            }

        }



    public void GetImage( ) {
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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