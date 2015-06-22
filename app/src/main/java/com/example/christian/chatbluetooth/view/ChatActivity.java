package com.example.christian.chatbluetooth.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatUser;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {

    private final BroadcastReceiver blueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:

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

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setItemAnimator(new DefaultItemAnimator());

        RecycleAdapter cardadapt = new RecycleAdapter(createList(10));
        recList.setAdapter(cardadapt);
    }

    private List createList(int size) {

        List result = new ArrayList();
        for (int i=1; i <= size; i++) {
            ChatUser card = new ChatUser();
            result.add(card);

        }

        return result;
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
}
