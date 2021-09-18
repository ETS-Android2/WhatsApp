package com.example.whatsapp.fragments;

import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.whatsapp.GroupChatActivity;
import com.example.whatsapp.R;
import com.example.whatsapp.helper.Gp;
import com.example.whatsapp.helper.GroupAdapter;
import com.google.api.Distribution;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.spec.GCMParameterSpec;


public class GroupFragment extends Fragment {

    View groupFargmentView;
    ListView  list_View;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> list_of_groups=new ArrayList<>();
    DatabaseReference GroupRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFargmentView= inflater.inflate(R.layout.fragment_group, container, false);
        GroupRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        Initialize();

        RetriveandDisplayGroups();
        list_View.setOnItemClickListener((adapterView, view, position, l) -> {
            String currentGroupName=adapterView.getItemAtPosition(position).toString();
            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
            groupChatIntent.putExtra("groupName",currentGroupName);
            startActivity(groupChatIntent);
        });

        return groupFargmentView;
    }

    private void RetriveandDisplayGroups() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    set.add((dataSnapshot).getKey());
                }

                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Initialize()
    {
        list_View = (ListView) groupFargmentView.findViewById(R.id.group_List_View);

        arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,list_of_groups);
        list_View.setAdapter(arrayAdapter);
    }
}