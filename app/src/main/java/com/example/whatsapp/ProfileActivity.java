package com.example.whatsapp;

import static com.example.whatsapp.ChatActivity.removedContact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.fragments.ChatFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserId , senderUserID,Current_State ;
    CircleImageView userProfileImage;
    TextView userProfileName,userProfileStatus;
    Button SendMessageRequest, DeclineMessageRequest;
    FirebaseAuth mauth;
    String image;
    DatabaseReference RootRef,ChatReqRef,ContactsReq,NotificationRef;
    StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        Intializ();

        mauth = FirebaseAuth.getInstance();
        senderUserID =mauth.getCurrentUser().getUid();
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        RootRef = FirebaseDatabase.getInstance().getReference();
        ChatReqRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsReq= FirebaseDatabase.getInstance().getReference().child("Contacts");
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        RetriveUserInfo(receiverUserId);
    }

    private void RetriveUserInfo(String receiverUserId) {
        RootRef.child("Users").child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && snapshot.hasChild("name") && snapshot.hasChild("image")) {
                    String rusername = snapshot.child("name").getValue().toString();
                    String rstatus = snapshot.child("status").getValue().toString();
                    String rprofile = snapshot.child("image").getValue().toString();
                    userProfileName.setText(rusername);
                    userProfileStatus.setText(rstatus);
                    image=rprofile;
                    GetImage(receiverUserId,userProfileImage);
                    ManageChatRequests();

                } else if ((snapshot.exists()) && snapshot.hasChild("name")) {
                    String rusername = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String rstatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    userProfileName.setText(rusername);
                    userProfileStatus.setText(rstatus);
                    ManageChatRequests();

                } else {
                    userProfileStatus.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Please set and update your profile.....", Toast.LENGTH_SHORT).show();
                    ManageChatRequests();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests() {
        ChatReqRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserId))
                        {
                            String request_type=snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                Current_State="request_sent";
                                SendMessageRequest.setText("Cancel Chat Request");

                            }
                            else if(request_type.equals("received"))
                            {
                                GetNotification("Whatsapp","request sent","request received");
                                Current_State="request_received";
                                SendMessageRequest.setText("Accept Chat Request");
                                DeclineMessageRequest.setVisibility(View.VISIBLE);
                                DeclineMessageRequest.setEnabled(true);
                                DeclineMessageRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        CancelRequestSent();
                                    }
                                });
                            }
                        }
                        else
                        {
                            ContactsReq.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.hasChild(receiverUserId))
                                            {
                                                Current_State="friends";
                                                SendMessageRequest.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        if (!senderUserID.equals(receiverUserId))

        {
            SendMessageRequest.setOnClickListener(view -> {
                SendMessageRequest.setEnabled(false);
                if(Current_State.equals("new"))
                {
                    SendChatReq();
                }
                if(Current_State.equals("request_sent"))
                {
                    CancelRequestSent();
                }
                if(Current_State.equals("request_received"))
                {
                    AcceptChatRequest();
                }
                if(Current_State.equals("friends"))
                {
                    RemoveSpecificContact();
                }
            });
        }
        else
        {
            SendMessageRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsReq.child(senderUserID).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                       ContactsReq.child(receiverUserId).child(senderUserID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful())
                                    {
                                        removedContact=1;
                                        SendMessageRequest.setEnabled(true);
                                        Current_State="new";
                                        SendMessageRequest.setText("Send Message");
                                        DeclineMessageRequest.setVisibility(View.INVISIBLE);
                                        DeclineMessageRequest.setEnabled(false);
                                    }
                                });

                    }
                });

    }

    private void AcceptChatRequest() {
        ContactsReq.child(senderUserID).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsReq.child(receiverUserId).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                ChatReqRef.child(senderUserID).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    ChatReqRef.child(receiverUserId).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                           SendMessageRequest.setEnabled(true);
                                                                                           Current_State="friends";
                                                                                           SendMessageRequest.setText("Remove this Contact");
                                                                                           DeclineMessageRequest.setVisibility(View.INVISIBLE);
                                                                                           DeclineMessageRequest.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelRequestSent()
    {
        ChatReqRef.child(senderUserID).child(receiverUserId)
                 .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatReqRef.child(receiverUserId).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                   SendMessageRequest.setEnabled(true);
                                                   Current_State="new";
                                                   SendMessageRequest.setText("Send Message");
                                                   DeclineMessageRequest.setVisibility(View.INVISIBLE);
                                                   DeclineMessageRequest.setEnabled(false);
                                                                                               }
                                        }
                                    });

                        }
                    }
                });


    }

    private void SendChatReq() {
        ChatReqRef.child(senderUserID).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        ChatReqRef.child(receiverUserId).child(senderUserID)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            HashMap<String, String> chatNotificationMap = new HashMap<>();
                                            chatNotificationMap.put("from", senderUserID);
                                            chatNotificationMap.put("type", "request");

                                            NotificationRef.child(receiverUserId).push()
                                                    .setValue(chatNotificationMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                SendMessageRequest.setEnabled(true);
                                                                Current_State="request_sent";
                                                                SendMessageRequest.setText(R.string.cancelchatreq);
                                                            }
                                                            }
                                                        });
                                                        }


                                    }
                                });
                    }
                });
    }

    private void Intializ() {
        userProfileImage=findViewById(R.id.visit_p_img);
        userProfileName=findViewById(R.id.vuisted_user_name);
        userProfileStatus=findViewById(R.id.visited_user_status);
        DeclineMessageRequest=findViewById(R.id.decline_message_decline_btn);
        SendMessageRequest=findViewById(R.id.send_message_request_btn);
        Current_State="new";
    }
    public void GetNotification(String title,String body,String subject)
    {
        NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify=new Notification.Builder
                (getApplicationContext()).setContentTitle(title).setContentText(body).
                setContentTitle(subject).setSmallIcon(R.drawable.whatsapp).build();

        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);;
    }
    public void GetImage(String currentUser, CircleImageView profileImage) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(removedContact==1)
        {
            Intent in = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(in);
            removedContact=0;
        }
    }
}