package com.example.christian.chatbluetooth.view.Adapters;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ChatListAdapter extends FragmentPagerAdapter {

    public ChatListAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = new com.example.christian.chatbluetooth.view.Fragments.ListFragment();

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
