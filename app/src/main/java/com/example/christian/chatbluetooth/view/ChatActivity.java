package com.example.christian.chatbluetooth.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatUser;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity implements ListFragment.OnFragmentInteractionListener, ChatFragment.OnFragmentInteractionListener{


    private final BroadcastReceiver blueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice dvc = (BluetoothDevice) getIntent().getExtras().get(BluetoothDevice.EXTRA_DEVICE);
                    if (BlueCtrl.addCloseDvc(dvc))
                        BlueCtrl.greet(dvc);
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (!BlueCtrl.DISCOVERY_SUSPENDED) {
                        if (!BluetoothAdapter.getDefaultAdapter().startDiscovery()) {
                            System.out.println("Discovery failed");
                            //TODO: discovery recovery
                        }
                    }
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ListFragment listFragment = new ListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.add(R.id.containerChat, listFragment);
        fragmentTransaction.commit();

        BlueCtrl.openDatabase(this);
        BlueCtrl.msgAdapt = new MessageAdapter(this, R.layout.listitem_discuss);
        if (BlueCtrl.appFolder == null) BlueCtrl.appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        // TODO: user button must be in the left side of actionBar

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
