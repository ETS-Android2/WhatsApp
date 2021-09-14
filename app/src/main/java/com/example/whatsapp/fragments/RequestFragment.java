package com.example.whatsapp.fragments;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.helper.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {
    View RequestFrag;
    RecyclerView recyclerView;
    DatabaseReference chatRequestRef,UsersRef,ContactsRef;
    String currentUserID;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    String names="";
    NotificationManager notificationManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RequestFrag= inflater.inflate(R.layout.fragment_request, container, false);
        recyclerView=RequestFrag.findViewById(R.id.chat_request_list);
        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        return RequestFrag;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserID),Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
                        final String list_user_id=getRef(position).getKey();
                        DatabaseReference gettypeRef=getRef(position).child("request_type").getRef();
                        gettypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    String type=snapshot.getValue().toString();
                                    if(type.equals("received"))
                                    {
                                        holder.linearLayout.setVisibility(View.VISIBLE);
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.hasChild("image"))
                                                {
                                                    String requestProfileImage=snapshot.child("image").getValue().toString();
                                                      GetImage(holder.profileImage, requestProfileImage,getContext());
                                                }
                                                String requestUsersName=snapshot.child("name").getValue().toString();
                                                holder.userName.setText(requestUsersName);
                                                holder.userStatus.setText("wants to connect with you");
                                                names+=requestUsersName+" ";
                                                show_Notification(requestUsersName);
                                                holder.AcceptBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"

                                                                };
                                                        deleteNotification();
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUsersName+"'s Chat Request");
                                                        builder.setItems(options, (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                                                            if(i==0)
                                                            {
                                                               ContactsRef.child(currentUserID)
                                                                       .child(list_user_id)
                                                                       .child("Contacts")
                                                                       .setValue("Saved")
                                                                       .addOnCompleteListener(task -> {
                                                                           if(task.isSuccessful())
                                                                           {
                                                                               ContactsRef.child(list_user_id)
                                                                                       .child(currentUserID)
                                                                                       .child("Contacts")
                                                                                       .setValue("Saved")
                                                                                       .addOnCompleteListener(task12 -> {
                                                                                           chatRequestRef.child(currentUserID)
                                                                                                   .child(list_user_id)
                                                                                                   .removeValue()
                                                                                                   .addOnCompleteListener(task1 -> {
                                                                                                       if(task1.isSuccessful())
                                                                                                       {
                                                                                                           chatRequestRef.child(list_user_id)
                                                                                                                   .child(currentUserID)
                                                                                                                   .removeValue()
                                                                                                                   .addOnCompleteListener(taskcu -> {
                                                                                                                       if(taskcu.isSuccessful())
                                                                                                                       {
                                                                                                                           Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();

                                                                                                                       }
                                                                                                                   });                                                                                                       }
                                                                                                   });
                                                                                       });

                                                                           }

                                                                       });
                                                            }
                                                            if(i==1)
                                                            {
                                                                chatRequestRef.child(currentUserID)
                                                                        .child(list_user_id)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(task1 -> {
                                                                            if(task1.isSuccessful())
                                                                            {
                                                                                chatRequestRef.child(list_user_id)
                                                                                        .child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(taskcu -> {
                                                                                            if(taskcu.isSuccessful())
                                                                                            {
                                                                                                Toast.makeText(getContext(), "Contact Deleted ", Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        });                                                                                                       }
                                                                        });



                                                            }

                                                        });
                                                        builder.show();
                                                    }
                                                });
                                                holder.CancelBtn.setOnClickListener(new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"

                                                                };
                                                        deleteNotification();
                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUsersName+"'s Chat Request");
                                                        builder.setItems(options, (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                                                            if(i==0)
                                                            {
                                                                ContactsRef.child(currentUserID)
                                                                        .child(list_user_id)
                                                                        .child("Contact")
                                                                        .setValue("Saved")
                                                                        .addOnCompleteListener(task -> {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                ContactsRef.child(list_user_id)
                                                                                        .child(currentUserID)
                                                                                        .child("Contact")
                                                                                        .setValue("Saved")
                                                                                        .addOnCompleteListener(task12 -> {
                                                                                            chatRequestRef.child(currentUserID)
                                                                                                    .child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(task1 -> {
                                                                                                        if(task1.isSuccessful())
                                                                                                        {
                                                                                                            chatRequestRef.child(list_user_id)
                                                                                                                    .child(currentUserID)
                                                                                                                    .removeValue()
                                                                                                                    .addOnCompleteListener(taskcu -> {
                                                                                                                        if(taskcu.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();

                                                                                                                        }
                                                                                                                    });                                                                                                       }
                                                                                                    });
                                                                                        });

                                                                            }

                                                                        });
                                                            }
                                                            if(i==1)
                                                            {
                                                                chatRequestRef.child(currentUserID)
                                                                        .child(list_user_id)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(task1 -> {
                                                                            if(task1.isSuccessful())
                                                                            {
                                                                                chatRequestRef.child(list_user_id)
                                                                                        .child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(taskcu -> {
                                                                                            if(taskcu.isSuccessful())
                                                                                            {
                                                                                                Toast.makeText(getContext(), "Contact Deleted ", Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        });                                                                                                       }
                                                                        });



                                                            }

                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    else if(type.equals("sent"))
                                        {
                                            Button request_send_btn=holder.itemView
                                                    .findViewById(R.id.request_accept_btn);
                                            request_send_btn.setText("Req Sent");

                                            holder.itemView.findViewById(R.id.request_cancel_btn)
                                                    .setVisibility(View.INVISIBLE);
                                            holder.linearLayout.setVisibility(View.VISIBLE);
                                            UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.hasChild("image"))
                                                    {
                                                        String requestProfileImage=snapshot.child("image").getValue().toString();
                                                        GetImage(holder.profileImage, requestProfileImage,getContext());
                                                    }

                                                    String requestUsersName=snapshot.child("name").getValue().toString();
                                                    holder.userName.setText(requestUsersName);
                                                    holder.userStatus.setText("you have sent a request to "+requestUsersName);
                                                    holder.AcceptBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            CharSequence options[]=new CharSequence[]
                                                                    {
                                                                            "Cancel chat Request"
                                                                    };
                                                            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                            builder.setTitle("Cancel Chat Request");
                                                            builder.setItems(options, (DialogInterface.OnClickListener) (dialogInterface, i) -> {

                                                                if(i==0)
                                                                {
                                                                    chatRequestRef.child(currentUserID)
                                                                            .child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(task1 -> {
                                                                                if(task1.isSuccessful())
                                                                                {
                                                                                    chatRequestRef.child(list_user_id)
                                                                                            .child(currentUserID)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(taskcu -> {
                                                                                                if(taskcu.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "You have Canceled Chat Request",Toast.LENGTH_SHORT).show();

                                                                                                }
                                                                                            });                                                                                                       }
                                                                            });



                                                                }

                                                            });
                                                            builder.show();
                                                        }
                                                    });
                                                    holder.CancelBtn.setOnClickListener(new View.OnClickListener() {

                                                        @Override
                                                        public void onClick(View view) {
                                                            CharSequence options[]=new CharSequence[]
                                                                    {
                                                                            "Cancel chat Request"
                                                                    };
                                                            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                            builder.setTitle("Cancel Chat Request");
                                                            builder.setItems(options, (DialogInterface.OnClickListener) (dialogInterface, i) -> {

                                                                if(i==0)
                                                                {
                                                                    chatRequestRef.child(currentUserID)
                                                                            .child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(task1 -> {
                                                                                if(task1.isSuccessful())
                                                                                {
                                                                                    chatRequestRef.child(list_user_id)
                                                                                            .child(currentUserID)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(taskcu -> {
                                                                                                if(taskcu.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "You have Canceled Chat Request", Toast.LENGTH_SHORT).show();

                                                                                                }
                                                                                            });                                                                                                       }
                                                                            });



                                                                }

                                                            });
                                                            builder.show();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                       View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                       RequestViewHolder holder=new RequestViewHolder(view);
                       return holder;

                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }
    public static  class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView  userName, userStatus;
        CircleImageView profileImage;
        Button AcceptBtn, CancelBtn;
        LinearLayout linearLayout;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
           AcceptBtn=itemView.findViewById(R.id.request_accept_btn);
           CancelBtn=itemView.findViewById(R.id.request_cancel_btn);
           linearLayout=itemView.findViewById(R.id.set_requests);
           linearLayout.setVisibility(View.INVISIBLE);

        }
    }
    public void GetImage( CircleImageView imageView,String currentUser, Context context) {
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
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

    public void show_Notification( String heading){

        Intent intent=new Intent(getContext(), MainActivity.class);
        String CHANNEL_ID="MYCHANNEL";
        NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_LOW);
        PendingIntent pendingIntent=PendingIntent.getActivity(getContext(),1,intent,0);
        Notification notification=new Notification.Builder(getContext(),CHANNEL_ID)
                .setContentText("has sent you friend request")
                .setContentTitle(heading)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.wp,"Title",pendingIntent)
                .setChannelId(CHANNEL_ID)
                .setSmallIcon(R.drawable.wp)
                .build();
        notificationManager=(NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1,notification);


    }
    public void deleteNotification()
    {
        notificationManager.cancelAll();
    }
}