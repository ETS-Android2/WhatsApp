package com.example.whatsapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.R;
import com.example.whatsapp.StatusActivity;
import com.example.whatsapp.helper.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactsFragment extends Fragment {
    View ContactView;
    RecyclerView myContactsList;
    FloatingActionButton statusbutton;
    StorageReference  storageReference;
    FirebaseAuth mAuth;
    String currentUser;
    DatabaseReference databaseReference;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    DatabaseReference UserRef;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ContactView= inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList=ContactView.findViewById(R.id.myContactList);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser().getUid();
        storageReference=FirebaseStorage.getInstance().getReference().child("Status");
        databaseReference=FirebaseDatabase.getInstance().getReference().child("Status");
        statusbutton=ContactView.findViewById(R.id.btn_create_channel2);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        statusbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
    });
        FirebaseRecyclerOptions<Contacts> options=new
                FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ContactsFragment.FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactsFragment.FindFriendViewHolder>(options) {
            @SuppressLint("RecyclerView")
            @Override
            protected void onBindViewHolder(@NonNull ContactsFragment.FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                holder.uh.setVisibility(View.GONE);
                holder.h.setVisibility(View.GONE);
                if(model.getTimeuploaded()!=null && model.getValid()!=null) {
                    Date date = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
                    String today = dateFormat.format(date);
                    Date val = null, now = null;
                    try {
                        val = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").parse(model.getValid());
                        now = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").parse(today);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    assert val != null;
                    if (val.getTime()<now.getTime()) {
                        holder.uh.setVisibility(View.VISIBLE);
                        holder.h.setVisibility(View.GONE);
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());

                        try {
                            GetImage(holder.profileImage, model.getuid());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(), StatusActivity.class);
                                intent.putExtra("userid", model.getuid());
                                intent.putExtra("time", model.getTimeuploaded());
                                intent.putExtra("username", model.getName());
                                startActivity(intent);
                            }
                        });
                    }
                    else
                    {
                        holder.uh.setVisibility(View.GONE);
                        holder.h.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    holder.uh.setVisibility(View.GONE);
                    holder.h.setVisibility(View.VISIBLE);
                }
            }


            @NonNull
            @Override
            public ContactsFragment.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.statuslaayout,viewGroup,false);
                ContactsFragment.FindFriendViewHolder viewHolder=new ContactsFragment.FindFriendViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    return ContactView;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            if(filePath != null){
                final ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Uploading");
                progressDialog.show();
                StorageReference picsRef = storageReference.child(currentUser+".jpg");
                picsRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Calendar c=Calendar.getInstance();
                                c.add(Calendar.HOUR,6);
                                Date validate=c.getTime();
                                Date date = Calendar.getInstance().getTime();
                                DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
                                String today = dateFormat.format(date);
                                String vali = dateFormat.format(validate);
                                UserRef.child(currentUser).child("timeuploaded").setValue(today);
                                UserRef.child(currentUser).child("valid").setValue(vali);
                                Toast.makeText(getContext(),"Story posted",Toast.LENGTH_SHORT);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(),"Story couldn't be posted",Toast.LENGTH_SHORT);
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                            }
                        });
            }

        }
    }
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout uh,h;
        TextView userName , userStatus;
        CircleImageView profileImage;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.status_contact_name);
            userStatus=itemView.findViewById(R.id.status_upload_time);
            profileImage=itemView.findViewById(R.id.image_preview_status);
            uh=itemView.findViewById(R.id.unhide);
            h=itemView.findViewById(R.id.hide);
            uh.setVisibility(View.GONE);
            h.setVisibility(View.GONE);

        }
    }
    public void GetImage(CircleImageView userProfileImg ,String userid) throws IOException {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Status/" +userid + ".jpg");
        File localFile = File.createTempFile(userid , ".jpg");
        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bmImg = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                userProfileImg.setImageBitmap(bmImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

}