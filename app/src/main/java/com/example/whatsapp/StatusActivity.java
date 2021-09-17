package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusActivity extends AppCompatActivity {
   TextView username,usertimeuploaded;
   ImageView statusimage;
   CircleImageView circleImageView;
   String uid,user,time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        username=findViewById(R.id.user);
        usertimeuploaded=findViewById(R.id.user_status_act);
        statusimage=findViewById(R.id.status_image);
        circleImageView=findViewById(R.id.custom_image);
        uid=getIntent().getExtras().get("userid").toString();
        user=getIntent().getExtras().get("username").toString();
        time=getIntent().getExtras().get("time").toString();
        username.setText(user);
        usertimeuploaded.setText("Time : "+time);
        GetImage(statusimage,uid);
        GetImagePro(uid,circleImageView);

    }
    public void GetImage(ImageView userProfileImg ,String userid) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Status/" + userid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
    public void GetImagePro(String currentUser, CircleImageView profileImage) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Profile Images/" + currentUser + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(profileImage);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }
}