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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.DisplayMetrics;
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
import android.widget.Switch;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.AsyncScavenger;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.controller.ServerThread;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.view.Adapters.EmoticonAdapter;
import com.example.christian.chatbluetooth.view.Fragments.ChatFragment;
import com.example.christian.chatbluetooth.view.Fragments.ListFragment;
import com.example.christian.chatbluetooth.view.Adapters.MenuAdapter;
import com.example.christian.chatbluetooth.view.Adapters.MessageAdapter;
import com.example.christian.chatbluetooth.view.Fragments.NoMaterialNavDrawerFragment;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class ChatActivity extends Activity implements ListFragment.OnFragmentInteractionListener,
                                                      ChatFragment.OnFragmentInteractionListener,
    /*DEBUG ONLY*/                                    NoMaterialNavDrawerFragment.OnFragmentInteractionListener {

    private DrawerLayout drawerLayout;
    private ListView listViewMenu;
    private Switch switchVisibility;
    private Handler handler;
    public boolean state = false;

    private final BroadcastReceiver blueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {

                case BluetoothDevice.ACTION_FOUND:

                    BluetoothDevice dvc = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (!BlueCtrl.closeDvc.contains(dvc)) {
                        System.out.println("greetings");
                        BlueCtrl.greet(dvc, handler);
                    }
                    else {

                        BlueCtrl.sendMsg(dvc, new byte[] {BlueCtrl.ACK}, handler);
                    }

                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (!BlueCtrl.DISCOVERY_SUSPENDED) {
                        //(new AsyncScavenger()).execute();
                        while (!BluetoothAdapter.getDefaultAdapter().startDiscovery()) { }
                        System.out.println("Discovering");
                    }
                    else System.out.println("Suspended");
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* NEW PART */

        handler = new Handler() {
            /*
            This is the brains behind the app; everytime a connection Thread ends, be it naturally or
            caused by an exception, a Message is sent to this Handler in order to manage data and
            network.
             */
            @Override
            public void handleMessage(Message msg) {

                ChatUser user;

                switch (msg.what) {
                    //Switch Message Ack

                    case BlueCtrl.GRT_HEADER:
                        //Some device successfully submitted himself to this device

                        user = BlueCtrl.userQueue.remove(0);
                        //take ChatUser object from Buffer

                        if (BlueCtrl.version) BlueCtrl.userAdapt.add(user);
                        else BlueCtrl.userNomat.add(user);
                        //Add found user in the user list

                        BlueCtrl.tokenMap.put(user.getMac(), BlueCtrl.TKN);
                        System.out.println("TOKEN " + BlueCtrl.TKN);
                        //Every time a message is successfully received restore Token

                        if (!BlueCtrl.closeDvc.contains(user.getNextNode())) {
                            BlueCtrl.greet(user.getNextNode(), handler);
                            System.out.println("Instant Greetings");
                        }
                        //Instant greeting: instead of waiting for the discovery, this device takes
                        //a BluetoothDevice object from this ChatUser and greets it

                        ArrayList<byte[]> updCascade = new ArrayList<>();
                        for (ChatUser ch : BlueCtrl.userList) {
                            if (!ch.getMac().equals(user.getMac())) {
                                updCascade.add(ch.getSegment());
                            }
                        }

                        if (updCascade.size() > 0) {
                            BlueCtrl.sendMsg(user.getNextNode(), BlueCtrl.buildUpdMsg(updCascade), handler);
                            System.out.println("UPDATE CASCADE");
                        }
                        //Send an Update Cascade to the newly found device, in order to connect it to the network

                        ArrayList<byte[]> singleupd = new ArrayList<>();
                        singleupd.add(user.getSegment());

                        BlueCtrl.dispatchNews(BlueCtrl.buildUpdMsg(singleupd), user.getNextNode(), handler);
                        //send the greeted user segment information to all reachable devices

                        break;

                    case BlueCtrl.UPD_HEADER:
                        //Some device has successfully notified the existence of other devices to this device

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN " + BlueCtrl.TKN);
                        //Restore Token

                        if (BlueCtrl.userQueue.size() > 0) {
                            if (BlueCtrl.version)
                                BlueCtrl.userAdapt.add(BlueCtrl.userQueue.remove(0));
                            else BlueCtrl.userNomat.add(BlueCtrl.userQueue.remove(0));
                        }
                        //Pop ChatUser from Buffer
                        break;

                    case BlueCtrl.CRD_HEADER:
                        //some device has successfully sent up to date information on a certain user

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN " + BlueCtrl.TKN);
                        //Restore Token

                        BlueCtrl.cardUpdate(msg.getData().getString("MAC"));
                        //fetch new user information and update the list

                        break;

                    case BlueCtrl.PIC_HEADER:

                        //TODO: Thread for picking and resizing the image for Thumbnail
                        break;

                    case BlueCtrl.MSG_HEADER:
                        //Some device successfully sent a chat message to this device

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN " + BlueCtrl.TKN);
                        //Restore Token

                        if (!BlueCtrl.msgBuffer.isEmpty()) {
                            BlueCtrl.msgAdapt.add(BlueCtrl.msgBuffer.remove(0));
                        }
                        //New message could be from a chat currently opened, therefore it could require update
                        break;


                    case BlueCtrl.NAK:
                        //This device failed to deliver a message to another device; there is need for a message recovery

                        String mac = msg.getData().getString("dvc");
                        //MAC address of the wannabe receiver

                        Integer counter = BlueCtrl.tokenMap.get(mac);
                        //Tokens left
                        if (counter == null) {

                            System.out.println("NO TOKEN");
                            //The remote user has not been registered yet; Handler usually goes here when a greetings fails.
                            //No further action is required
                            break;
                        }

                        if (counter < 1) {

                            //The remote device ran out of Tokens and is likely that is no longer reachable; it is deleted
                            //from all lists; if it shows up again, it must first greet again

                            BlueCtrl.tokenMap.remove(mac);
                            BlueCtrl.closeDvc.remove(mac);
                            BlueCtrl.userList.remove(mac);
                        }
                        else {

                            //Connection failed, but the remote device could be simply busy;
                            //a Token is taken from him, and this device tries to contact it again

                            BlueCtrl.tokenMap.put(mac, --counter);
                            System.out.println("TOKEN " + BlueCtrl.tokenMap.get(mac));

                            user = BlueCtrl.scanUsers(msg.getData().getString("dvc"));
                            //Find the next node from ChatUser information
                            byte[] mail = msg.getData().getByteArray("msg");
                            //get back the unsent message
                            System.out.println("RE-SENDING MESSAGE NUMBER " + mail[0]);
                            if (user != null) {
                                BlueCtrl.sendMsg(user.getNextNode(), mail, handler);
                                //re-send
                            }
                            //if no user was found it probabily is no longer reachable, and
                            //there's no point in forwarding the message again
                            //END OF RESEND CHAIN
                        }
                        break;
                }

                if (BlueCtrl.version) BlueCtrl.userAdapt.notifyDataSetChanged();
                else BlueCtrl.userNomat.notifyDataSetChanged();
                //notify any changes to the List Adapter

            }
        };

        ListFragment listFragment = new ListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.add(R.id.containerChat, listFragment);
        fragmentTransaction.commit();

        /* NEW PART */

        if (BlueCtrl.version) {

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
            ((ArrayAdapter) listViewMenu.getAdapter()).add("Profilo");
            ((ArrayAdapter) listViewMenu.getAdapter()).add("Impostazioni");
            ((ArrayAdapter) listViewMenu.getAdapter()).add("Cronologia");
            listViewMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
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
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, null, R.string.app_name, R.string.app_name) {
                /**
                 * Called when a drawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    getActionBar().setTitle("Lista Contatti");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /**
                 * Called when a drawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getActionBar().setTitle("Menu");
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            drawerLayout.setDrawerListener(drawerToggle);

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
            int[] pixel = new int[2];
            pixel[0] = (bmp.getWidth() - 240) / 2;
            pixel[1] = (bmp.getHeight() - 180) / 2;
            bmp = Bitmap.createScaledBitmap(bmp, 240, 180, false);
            bmp.setDensity(DisplayMetrics.DENSITY_DEFAULT);
            ((ImageView) findViewById(R.id.image_drawer)).setImageDrawable(new BitmapDrawable(bmp));

            switchVisibility = (Switch) findViewById(R.id.switch_state);

            (findViewById(R.id.image_switch)).setBackground(getDrawable(R.mipmap.visibility));

        /* END NEW PART */

        }

        BlueCtrl.emoticons = new EmoticonAdapter(this, R.layout.item_emoticon_picker);

        BlueCtrl.openDatabase(this);
        if (getIntent().getBooleanExtra("newbie", false)) {
            SharedPreferences sh = getSharedPreferences("preferences", MODE_PRIVATE);
            long timestamp = sh.getLong("timestamp", 0);
            int age = 0;
            try {
                long millis = (new SimpleDateFormat("dd mm yyyy")).parse(sh.getString("birth", "01 01 1980")).getTime() -
                              (new Date()).getTime();
                age = (int) (millis / 31536000000l);
            }
            catch (Exception ignore) {}
            BlueCtrl.insertUserTable(BluetoothAdapter.getDefaultAdapter().getAddress(), timestamp, sh.getString("username", "Unknown"), age, sh.getInt("gender", 0), sh.getInt("nationality", 0));
        }

        BlueCtrl.validateUser(BluetoothAdapter.getDefaultAdapter().getAddress(), getSharedPreferences("preferences", MODE_PRIVATE).getLong("timestamp", 0));

        BlueCtrl.msgAdapt = new MessageAdapter(this, R.layout.listitem_discuss);
        BlueCtrl.msgAdapt.setAddress(new String());

        //if (BlueCtrl.appFolder == null) BlueCtrl.appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);


        try {
            (new ServerThread(BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("com.example.christian.chatbluetooth", UUID.fromString(BlueCtrl.UUID)), handler)).start();
        }

        catch (IOException e){
            e.printStackTrace();
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
                if (BlueCtrl.version) drawerLayout.openDrawer(findViewById(R.id.left_drawer));
    /*
    DEBUG ONLY
     */
                else {
                    NoMaterialNavDrawerFragment fragment = new NoMaterialNavDrawerFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerChat, fragment);
                    fragmentTransaction.commit();
                }
    /*
    DEBUG ONLY
     */
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
        /*
        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        BluetoothAdapter.getDefaultAdapter().startDiscovery();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*unregisterReceiver(blueReceiver);*/
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void setState(boolean val){
        state = val;
    }
}
