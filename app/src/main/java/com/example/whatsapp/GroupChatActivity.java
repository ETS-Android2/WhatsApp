package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
     Toolbar toolbar;
     ImageButton SendMessageButton;
     EditText userMessageInput;
     ScrollView scrollview;
     TextView displayTextMessage;
     String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;
     FirebaseAuth mauth;
     DatabaseReference UserRef,GroupNameRef,GroupMsgKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        Intialize();
        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();
        currentGroupName=getIntent().getExtras().get("groupName").toString();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);
        Toast.makeText(getApplicationContext(), currentGroupName, Toast.LENGTH_SHORT).show();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        GetUserInfo();
        scrollview = ((ScrollView) findViewById(R.id.my_scroll_view));
        SendMessageButton.setOnClickListener(view -> {
            SaveMessageInfoToDatabase();
            userMessageInput.setText("");
           ScrollPage();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    DisplayMessage(snapshot);
                }
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

    public void DisplayMessage(DataSnapshot snapshot)
    {
        Iterator iterator=snapshot.getChildren().iterator();
        while (iterator.hasNext())
        {
            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();
            displayTextMessage.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "  "+ chatDate+"\n\n\n");

           ScrollPage();

        }

    }


    private void SaveMessageInfoToDatabase() {
        String message=userMessageInput.getText().toString();
        String messageKey=GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(getApplicationContext(), "Please write message first ....", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar ccalForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentDate=currentDateFormat.format(ccalForDate.getTime());
            Calendar ccalForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(ccalForTime.getTime());
            HashMap<String,Object> groupMessageKey=new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);
            GroupMsgKey=GroupNameRef.child(messageKey);
            HashMap<String , Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            GroupMsgKey.updateChildren(messageInfoMap);


        }
    }

    private void GetUserInfo() {
        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    currentUserName=snapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Intialize() {
        toolbar=findViewById(R.id.group_chat_bar_layout);
        SendMessageButton=findViewById(R.id.Send_msg_button);
       userMessageInput=findViewById(R.id.input_group_msgs);
       displayTextMessage=findViewById(R.id.group_chat_text_display);

    }
    public void ScrollPage()
    {
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}