package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.whatsapp.helper.MessageAdapter;
import com.example.whatsapp.helper.Messages;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    ImageButton senMessage,sendFiles;
    EditText message;
    public static int removedContact=0;
    String messageRecievedID, messageReciveName,messagerECIVERiMAGE="",messageSenderId;
    TextView userName,UserLastSeen;
    Toolbar toolbar;
    FirebaseAuth mauth;
    DatabaseReference RootRef;
    FirebaseAuth mAuth;
    Button contactsetting;
    CircleImageView userImage;
    Uri fileUri;
    ProgressDialog pd;
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    StorageReference storageReference;
    RecyclerView recyclerView;
    private final List<Messages> messagesList = new ArrayList<>();
    private String saveCurrentTime, saveCurrentDate,checker="";
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mauth=FirebaseAuth.getInstance();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        messageRecievedID=getIntent().getExtras().get("visit_user_ID").toString();
        messageReciveName=getIntent().getExtras().get("visit_user_name").toString();
        messagerECIVERiMAGE=getIntent().getExtras().get("visit_image").toString();
        toolbar=findViewById(R.id.chat_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        contactsetting=(Button)findViewById(R.id.contactsettings);
        contactsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToChatActivity();
            }
        });
        userName=(TextView)findViewById(R.id.custom_profile_name);
        UserLastSeen=(TextView)findViewById(R.id.custom_user_last_seen);
        userImage=(CircleImageView) findViewById(R.id.custom_profile_image);
       message=findViewById(R.id.chat_input_msg);
       senMessage=findViewById(R.id.chat_input_img);
       sendFiles=findViewById(R.id.file_input_img);
       mAuth=FirebaseAuth.getInstance();
       messageSenderId=mAuth.getCurrentUser().getUid();
       RootRef= FirebaseDatabase.getInstance().getReference();
       messageAdapter=new MessageAdapter(messagesList,getApplicationContext());
        if(messageReciveName!=null && messageRecievedID!=null && messagerECIVERiMAGE!=null)
        {
          //  Toast.makeText(this, messageReciveName+messagerECIVERiMAGE, Toast.LENGTH_SHORT).show();
            userName.setText(messageReciveName);
            GetImage(messagerECIVERiMAGE,userImage,getApplicationContext());
        }
        senMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                   sendMessage();
            }
        });
        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView=findViewById(R.id.private_messages_list_of_users);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
        DisplayLastSeen();
        pd = new ProgressDialog(this);
        sendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                       if(i==0) {
                           checker = "image";
                           Intent intent = new Intent();
                           intent.setAction(Intent.ACTION_GET_CONTENT);
                           intent.setType("image/*");
                           startActivityForResult(Intent.createChooser(intent, "Select Image"), 438);
                       }
                        if(i==1) {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(Intent.createChooser(intent, "Select Pdf File"), 438);
                        }
                        if(i==2) {
                            checker = "docx";
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                            startActivityForResult(intent, 438);                     }

                    }
                });
                builder.show();
            }
        });
        setRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==438 && resultCode==RESULT_OK
        && data.getData()!=null)
        {
            pd.setTitle("Sending File");
            pd.setMessage("Please wait ,we are sending the file..");
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            fileUri=data.getData();
            if (!checker.equals("image"))
            {

                    StorageReference storageReference
                            =FirebaseStorage.getInstance().getReference().child("Document Files");
                    String messageSenderRef="Messages/"+messageSenderId+"/"+messageRecievedID;
                    String messageRecieverRef="Messages/"+messageRecievedID+"/"+messageSenderId;
                    DatabaseReference userMessageRef=RootRef.child("Messages")
                            .child(messageSenderId).child(messageRecievedID).push();
                    String messagePushId=userMessageRef.getKey();
                    StorageReference filePath=storageReference.child(messagePushId+"."+checker);
                    filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful())
                            {
                                Map messageTextReady = new HashMap();
                                messageTextReady.put("message",messagePushId);
                                messageTextReady.put("name",fileUri.getLastPathSegment());
                                messageTextReady.put("type",checker);
                                messageTextReady.put("from",messageSenderId);
                                messageTextReady.put("to",messageRecievedID);
                                messageTextReady.put("messageID",messagePushId);
                                messageTextReady.put("time", saveCurrentTime);
                                messageTextReady.put("date", saveCurrentDate);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
                                messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
                                RootRef.updateChildren(messageBodyDetails);

                                pd.dismiss();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(ChatActivity.this, "" +
                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                           int p= (int) ((100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount());
                            pd.setMessage((int)p+"% Uploading...");
                        }
                    });



            }
            else if (checker.equals("image"))
            {
               StorageReference storageReference
                       =FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef="Messages/"+messageSenderId+"/"+messageRecievedID;
                String messageRecieverRef="Messages/"+messageRecievedID+"/"+messageSenderId;
                DatabaseReference userMessageRef=RootRef.child("Messages")
                        .child(messageSenderId).child(messageRecievedID).push();
                String messagePushId=userMessageRef.getKey();
                StorageReference filePath=storageReference.child(messagePushId+"."+"jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWith(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Map messageTextReady = new HashMap();
                        messageTextReady.put("message",messagePushId);
                        messageTextReady.put("name",fileUri.getLastPathSegment());
                        messageTextReady.put("type",checker);
                        messageTextReady.put("from",messageSenderId);
                        messageTextReady.put("to",messageRecievedID);
                        messageTextReady.put("messageID",messagePushId);
                        messageTextReady.put("time", saveCurrentTime);
                        messageTextReady.put("date", saveCurrentDate);

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
                        messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
                        RootRef.updateChildren(messageBodyDetails);
                        pd.dismiss();





                    }
                });
            }
            else
            {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Nothing is Selected", Toast.LENGTH_SHORT).show();
              }


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void GetImage(String currentUser, CircleImageView imageView, Context context) {
        storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Profile Images/" + currentUser + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(imageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }
    private void sendMessage()
    {
        String messageText=message.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            message.setError("Please enter a message");

        }
        else
        {
            String messageSenderRef="Messages/"+messageSenderId+"/"+messageRecievedID;
            String messageRecieverRef="Messages/"+messageRecievedID+"/"+messageSenderId;
            DatabaseReference userMessageRef=RootRef.child("Messages")
                    .child(messageSenderId).child(messageRecievedID).push();
            String messagePushId=userMessageRef.getKey();
            Map messageTextReady = new HashMap();
            messageTextReady.put("message",messageText);
            messageTextReady.put("type","text");
            messageTextReady.put("from",messageSenderId);
            messageTextReady.put("to",messageRecievedID);
            messageTextReady.put("messageID",messagePushId);
            messageTextReady.put("time", saveCurrentTime);
            messageTextReady.put("date", saveCurrentDate);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextReady);
            messageBodyDetails.put(messageRecieverRef+"/"+messagePushId,messageTextReady);
            RootRef.updateChildren(messageBodyDetails);
            message.setText("");







        }
    }
    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageRecievedID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                UserLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                              UserLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        }
                        else
                        {
                           UserLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void updateUserStatus(String state) {
        FirebaseUser currentUser = mauth.getCurrentUser();
        String currentUserID = currentUser.getUid();
        RootRef.child("Users").child(currentUserID).child("userState")
                .child("state").setValue(state);
    }
    public void setRecyclerView()
    {
        RootRef.child("Messages").child(messageSenderId).child(messageRecievedID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages=snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        // Toast.makeText(getApplicationContext(), ""+messages.getMessage(), Toast.LENGTH_SHORT).show();
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter()
                                .getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void sendUserToChatActivity() {
        Intent profileIntent=new Intent(ChatActivity.this,ProfileActivity.class);
        profileIntent.putExtra("visit_user_id",messageRecievedID);
        startActivity(profileIntent);
    }
}