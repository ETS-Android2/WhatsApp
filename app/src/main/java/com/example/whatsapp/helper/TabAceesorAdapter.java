package com.example.whatsapp.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.whatsapp.fragments.ChatFragment;
import com.example.whatsapp.fragments.ContactsFragment;
import com.example.whatsapp.fragments.GroupFragment;
import com.example.whatsapp.fragments.RequestFragment;


public class TabAceesorAdapter extends FragmentPagerAdapter {
    public TabAceesorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0 :
                ChatFragment chatFragment= new ChatFragment();
                return chatFragment;
            case 1 :
                GroupFragment groupFragment= new GroupFragment();
                return groupFragment;
            case 2 :
                ContactsFragment contactsFragment= new ContactsFragment();
                return contactsFragment;
            case 3 :
                RequestFragment requestFragment= new RequestFragment();
                return requestFragment;
            default:
                return null;

 }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position)
        {
            case 0 :
                 return "Chats";
            case 1 :
                return "Groups";
            case 2 :
                return "Status";
            case 3 :
                return "Requests";
            default:
                return null;

        }
    }
}

