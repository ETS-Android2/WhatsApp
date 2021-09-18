package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.helper.Gp;
import com.example.whatsapp.helper.Gp;
import com.example.whatsapp.helper.GroupAdapter;
import com.example.whatsapp.helper.MessageAdapter;
import com.example.whatsapp.helper.Messages;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
     Toolbar toolbar;
     RecyclerView find;
     ImageButton SendMessageButton;
     EditText userMessageInput;
     Gp gp;
      LinearLayoutManager linearLayoutManager;
    private final List<Gp> messagesList = new ArrayList<>();
     GroupAdapter groupAdapter;
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
        groupAdapter=new GroupAdapter(messagesList,getApplicationContext());
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        GetUserInfo();
       // scrollview = ((ScrollView) findViewById(R.id.my_scroll_view));
        SendMessageButton.setOnClickListener(view -> {
            SaveMessageInfoToDatabase();
            userMessageInput.setText("");
         //  ScrollPage();
        });
        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        find.setLayoutManager(linearLayoutManager);
        find.setAdapter(groupAdapter);

    }


    @Override
    protected void onStart() {
        super.onStart();
        setRecyclerView();
    }


    public void setRecyclerView()
    {
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists())
                {
                    Gp messages=snapshot.getValue(Gp.class);
                    messagesList.add(messages);
                  groupAdapter.notifyDataSetChanged();
                   find.smoothScrollToPosition(find.getAdapter()
                            .getItemCount());
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

    private void SaveMessageInfoToDatabase() {
        String message=userMessageInput.getText().toString();
        String messageKey=GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message)) {
            userMessageInput.setError("Please write message first ....");
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
            messageInfoMap.put("uid",currentUserId);
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
        find=findViewById(R.id.recyclerviewgroup);

    }

}