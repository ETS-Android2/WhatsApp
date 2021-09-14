package com.example.whatsapp.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.whatsapp.IMageViewActivity;
import com.example.whatsapp.MainActivity;
import com.example.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

   Context context;

    public MessageAdapter (List<Messages> userMessagesList,Context context)
    {
        this.userMessagesList = userMessagesList;
        this.context=context;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public TextView senderMessageTexttd, receiverMessageTexttd;
        public LinearLayout sender, receiver;
        public ImageView senderImageView, receiverImageView;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messages);
            receiverMessageText = (TextView) itemView.findViewById(R.id.reciever_messages);
            senderMessageTexttd = (TextView) itemView.findViewById(R.id.sender_messages_td);
            receiverMessageTexttd = (TextView) itemView.findViewById(R.id.reciever_messages_td);
            sender =  itemView.findViewById(R.id.lls);
            receiver =  itemView.findViewById(R.id.llr);
            senderImageView=itemView.findViewById(R.id.messager_sender_image_view);
            receiverImageView=itemView.findViewById(R.id.receiver_sender_image_view);

        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, @SuppressLint("RecyclerView") int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.receiver.setVisibility(View.GONE);
        messageViewHolder.sender.setVisibility(View.GONE);
        messageViewHolder.receiverImageView.setVisibility(View.GONE);
        messageViewHolder.senderImageView.setVisibility(View.GONE);
        messageViewHolder.receiverMessageTexttd.setVisibility(View.GONE);
        messageViewHolder.senderMessageTexttd.setVisibility(View.GONE);
        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.sender.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageTexttd.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderMessageTexttd.setText(messages.getTime()+" "+
                        messages.getDate());

            }
            else
            {

                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiver.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageTexttd.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverMessageTexttd.setText(messages.getTime()+" "+
                        messages.getDate());

            }
        }
        else if(fromMessageType.equals("image"))
        {
            if(fromUserID.equals(messageSenderId))
            {
                Log.d("input_showed","image showed");
                messageViewHolder.receiverImageView.setVisibility(View.GONE);
                messageViewHolder.senderImageView.setVisibility(View.VISIBLE);
                GetImage(messageViewHolder.senderImageView,messages.getMessageID());

            }
            else
            { messageViewHolder.senderImageView.setVisibility(View.GONE);
                messageViewHolder.receiverImageView.setVisibility(View.VISIBLE);
                GetImage(messageViewHolder.receiverImageView,messages.getMessage());

            }
        }
        else
        {
            if(fromUserID.equals(messageSenderId))
            {
                Log.d("input_showed","image showed");
                messageViewHolder.receiverImageView.setVisibility(View.GONE);
                messageViewHolder.senderImageView.setVisibility(View.VISIBLE);
                messageViewHolder.senderImageView.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);

            }
            else
            {
                messageViewHolder.senderImageView.setVisibility(View.GONE);
                messageViewHolder.receiverImageView.setVisibility(View.VISIBLE);
                messageViewHolder.receiverImageView.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);

            }
        }
        if(fromUserID.equals(messageSenderId))
        {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(messages.getType().equals("pdf")||messages.getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View This Document",
                                        "Cancel",
                                        "Delete for Everyone"

                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                         deleteSendMessageForMe(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                    openFile(messages.getMessageID(),messages.getType());

                                }
                                if(i==3)
                                {
                                    deleteMessageForEveryOne(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Delete for Everyone",
                                        "Cancel"

                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteSendMessageForMe(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {

                                    deleteMessageForEveryOne(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("image"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel",
                                        "Delete for Everyone"


                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteSendMessageForMe(position,messageViewHolder);

                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            IMageViewActivity.class);
                                    intent.putExtra("url",messages.getMessageID());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                                if(i==3)
                                {
                                    deleteMessageForEveryOne(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else
        {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(messages.getType().equals("pdf")||messages.getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View This Document",
                                        "Cancel"

                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                   deleteReciveMessage(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                    openFile(messages.getMessageID(),messages.getType());

                                }if(i==2)
                                {

                                }


                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"

                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteReciveMessage(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {


                                }

                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("image"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel"

                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteReciveMessage(position,messageViewHolder);
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            MainActivity.class);
                                   messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                    Intent intent=new Intent(messageViewHolder.itemView.getContext(),
                                            IMageViewActivity.class);
                                    intent.putExtra("url",messages.getMessageID());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }if(i==2)
                                {

                                }


                            }
                        });
                        builder.show();
                    }
                }
            });

        }
    }


    public void GetImage(ImageView imageView,String currentUser ) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Image Files/" + currentUser + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(imageView);

            }
        });
    }
    public void openFile(String currentUserID,String checker) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Document Files/" + currentUserID + "."+checker).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(checker.equals("pdf")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Intent in = Intent.createChooser(intent, "open file");
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(in);
                }
                else
                {
                    String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, "*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    Intent in = Intent.createChooser(intent, "open file");
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(in);
                }

            }
        });
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
    public void deleteSendMessageForMe(final int pos,final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(pos).getFrom())
                .child(userMessagesList.get(pos).getTo())
                .child(userMessagesList.get(pos).getMessageID())
                .removeValue().addOnCompleteListener(task -> {
                     if(task.isSuccessful())
                     {
                         Toast.makeText(holder.itemView.getContext(),
                                 "Deleted Successfully", Toast.LENGTH_SHORT).show();
                     }
                     else
                     {
                         Toast.makeText(holder.itemView.getContext(),
                                 "Error Occurred", Toast.LENGTH_SHORT).show();
                     }
                });
    }
    public void deleteMessageForEveryOne( int pos,MessageViewHolder holder) {
          DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(pos).getTo())
                .child(userMessagesList.get(pos).getFrom())
                .child(userMessagesList.get(pos).getMessageID())
                .removeValue();
        rootRef.child("Messages")
                .child(userMessagesList.get(pos).getFrom())
                .child(userMessagesList.get(pos).getTo())
                .child(userMessagesList.get(pos).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    public void deleteReciveMessage(final int pos,final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(pos).getTo())
                .child(userMessagesList.get(pos).getFrom())
                .child(userMessagesList.get(pos).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {

                            Toast.makeText(holder.itemView.getContext(),
                                    "Deleted Successfully", Toast.LENGTH_SHORT).show();



                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),
                            "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}