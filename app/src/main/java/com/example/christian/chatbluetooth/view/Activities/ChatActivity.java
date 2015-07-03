package com.example.christian.chatbluetooth.view.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.AsyncScavenger;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.controller.ServerThread;
import com.example.christian.chatbluetooth.view.Fragments.ChatFragment;
import com.example.christian.chatbluetooth.view.Fragments.ListFragment;
import com.example.christian.chatbluetooth.view.Adapters.MenuAdapter;
import com.example.christian.chatbluetooth.view.Adapters.MessageAdapter;

import java.io.IOException;
import java.util.UUID;

public class ChatActivity extends Activity implements ListFragment.OnFragmentInteractionListener, ChatFragment.OnFragmentInteractionListener{

    private DrawerLayout drawerLayout;
    private ListView listViewMenu;
    public boolean state = false;

    private final BroadcastReceiver blueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:

                    BluetoothDevice dvc = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (BlueCtrl.addCloseDvc(dvc)) {
                        System.out.println("greetings");
                        BlueCtrl.greet(dvc);
                    }

                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    System.out.println("scavenging");
                    (new AsyncScavenger()).execute();
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

        /* NEW PART */
        ActionBar actionBar = getActionBar();

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        drawerLayout = (DrawerLayout) inflater.inflate(R.layout.nav_drawer, null);

        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        FrameLayout container = (FrameLayout) drawerLayout.findViewById(R.id.nav_container);
        container.addView(child);
        decor.addView(drawerLayout);
        ((TextView) findViewById(R.id.username_drawer)).setText(getSharedPreferences("preferences", MODE_PRIVATE).getString("username", "None"));
        listViewMenu = (ListView) findViewById(R.id.list_menu);
        listViewMenu.setAdapter(new MenuAdapter(this, R.layout.menu_item_layout));
        ((ArrayAdapter)listViewMenu.getAdapter()).add("Profilo");
        ((ArrayAdapter)listViewMenu.getAdapter()).add("Impostazioni");
        ((ArrayAdapter)listViewMenu.getAdapter()).add("Cronologia");
        listViewMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent = new Intent(
                                getApplicationContext(),
                                ProfileActivity.class
                        );
                        startActivity(intent);
                        break;

                    case 1:
                        intent = new Intent(
                                getApplicationContext(),
                                SettingActivity.class
                        );
                        startActivity(intent);
                        break;

                    case 2:
                        intent = new Intent(
                                getApplicationContext(),
                                HistoryActivity.class
                        );
                        startActivity(intent);
                        break;
                }
            }
        });
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, null, R.string.app_name, R.string.app_name){
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle("Lista Contatti");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        ((ImageView) findViewById(R.id.image_drawer)).setImageDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(bmp,240, 180, false)));
        /* END NEW PART */


        ListFragment listFragment = new ListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.add(R.id.containerChat, listFragment);
        fragmentTransaction.commit();

        BlueCtrl.openDatabase(this);
        BlueCtrl.msgAdapt = new MessageAdapter(this, R.layout.listitem_discuss);
        BlueCtrl.msgAdapt.setAddress(new String());
        if (BlueCtrl.appFolder == null) BlueCtrl.appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            (new ServerThread(BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("com.example.christian.chatbluetooth", UUID.fromString(BlueCtrl.UUID)))).start();
        }

        catch (IOException ignore){
            System.out.println("listen failed");
        }

        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home){

            if (state){
                ListFragment listFragment = new ListFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                fragmentTransaction.replace(R.id.containerChat, listFragment);
                fragmentTransaction.commit();
                state = false;
                return true;
            }

            else{
                drawerLayout.openDrawer(findViewById(R.id.left_drawer));
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(blueReceiver);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void setState(boolean val){
        state = val;
    }
}
