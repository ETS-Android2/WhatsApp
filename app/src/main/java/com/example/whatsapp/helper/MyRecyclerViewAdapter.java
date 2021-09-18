package com.example.whatsapp.helper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.GroupChatActivity;
import com.example.whatsapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.GroupViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;


    public MyRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }


    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.group_layout, parent, false);
        return new GroupViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder( GroupViewHolder holder, int position) {
        String animal = mData.get(position);
        holder.userName.setText(animal);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent groupChatIntent = new Intent(holder.itemView.getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",animal);
                holder.itemView.getContext().startActivity(groupChatIntent);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView  userName;


        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage=itemView.findViewById(R.id.profile_image_group);
            userName=itemView.findViewById(R.id.text_view_user);
            profileImage.setImageResource(R.drawable.groupicon);


        }
    }


}