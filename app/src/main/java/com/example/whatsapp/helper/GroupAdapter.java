package com.example.whatsapp.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.FindFriendViewHolder> {
    private final Context context;
    private List<Gp> userMessagesList;
    private FirebaseAuth mAuth;
    public GroupAdapter (List<Gp> userMessagesList, Context context)
    {
        this.userMessagesList = userMessagesList;
        this.context=context;
    }
    @NonNull
    public GroupAdapter.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.groupchat, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new GroupAdapter.FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {
        Gp gp=userMessagesList.get(position);
        String uid=gp.getUid();
        String chatName=gp.getName();
        String chatMessage=gp.getMessage();
        String chatTime=gp.getTime();
        String chatDate=gp.getDate();
        String currentUserId=mAuth.getCurrentUser().getUid();
        if (uid.equals(currentUserId)) {
            holder.lls.setVisibility(View.VISIBLE);
            holder.llr.setVisibility(View.GONE);
            holder.sendermsg.setText(chatMessage);
            holder.sname.setText(chatName);
            holder.slst.setText("last seen : "+chatTime+" "+chatDate);
        }
        else
        {
            holder.lls.setVisibility(View.GONE);
            holder.llr.setVisibility(View.VISIBLE);
            holder.recicevermsg.setText(chatMessage);
            holder.rname.setText(chatName);
            holder.rlst.setText("last seen : "+chatTime+" "+chatDate);

        }
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout lls,llr;
        TextView sendermsg,recicevermsg,sname,rname,slst,rlst;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            llr=itemView.findViewById(R.id.llrg);
            lls=itemView.findViewById(R.id.llsg);
            recicevermsg=itemView.findViewById(R.id.reciever_messages_group);
            sendermsg=itemView.findViewById(R.id.sender_messages_group);
            sname=itemView.findViewById(R.id.sender_messages_username);
            rname=itemView.findViewById(R.id.reciever_messages_username);
            slst=itemView.findViewById(R.id.sender_messages_lastssen);
            rlst=itemView.findViewById(R.id.reciever_messages_lastseen);
            llr.setVisibility(View.GONE);
            lls.setVisibility(View.GONE);

        }
    }
}
