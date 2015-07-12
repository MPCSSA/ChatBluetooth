package com.example.christian.chatbluetooth.view.Activities;

import android.app.Activity;
import android.app.Fragment;
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
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ChatActivity extends Activity implements ListFragment.OnFragmentInteractionListener,
                                                      ChatFragment.OnFragmentInteractionListener {

    private DrawerLayout drawerLayout;
    private Switch switchVisibility;
    private static Handler handler;
    public boolean state = false, state2 = false, chat = false;

    public Handler getHandler() { return ChatActivity.handler; }
    public void setState(boolean val) { state = val; }

    private final BroadcastReceiver blueReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {

                case BluetoothDevice.ACTION_FOUND:

                    BluetoothDevice dvc = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (!BlueCtrl.closeDvc.containsValue(dvc)) {
                        //Unrecognised user, trying to greet it
                        BlueCtrl.sendMsg(dvc, BlueCtrl.buildGrtMsg(), handler);
                    }
                    else {
                        //Known user, make sure it's not dead
                        BlueCtrl.sendMsg(dvc, new byte[] {BlueCtrl.ACK}, handler);
                    }

                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:

                    if (!BlueCtrl.DISCOVERY_SUSPENDED) { //natural end of discovery

                        BlueCtrl.dispatchNews(new byte[] {BlueCtrl.ACK}, null, handler); //Search for disconnected devices

                        boolean isDiscovering;
                        do {
                            isDiscovering = BluetoothAdapter.getDefaultAdapter().startDiscovery();
                        }
                        while (!isDiscovering);
                    }

                    break;

                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    //Manage Bluetooth state changes

                    switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        case BluetoothAdapter.STATE_ON:
                            BluetoothAdapter.getDefaultAdapter().startDiscovery(); //start discovering
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)); //request for Bluetooth enabling
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            unregisterReceiver(this);
                            Intent close = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(close);
                            //User decided to quit Bluetooth, and therefore the app has to be closed
                    }

                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //HANDLER IMPLEMENTATION
        handler = new Handler() {
            /*
            This is the brains behind the app; everytime a connection Thread ends, be it naturally or
            caused by an exception, a Message is sent to this Handler in order to manage data and
            network.
             */
            @Override
            public void handleMessage(Message msg) {

                obtainMessage();

                ChatUser user;
                BluetoothDevice dvc;
                String mac;
                Integer counter;

                switch (msg.what) {
                    //Switch Message Ack

                    case BlueCtrl.GRT_HEADER:
                        //Some device successfully submitted himself to this device

                        user = BlueCtrl.userQueue.remove(0);
                        //take ChatUser object from Buffer

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN ASSIGNED TO " + msg.getData().getString("MAC"));
                        //Every time a message is successfully received restore Token

                        BlueCtrl.sendMsg(user.getNextNode(), BlueCtrl.buildGrtMsg(), handler);
                        System.out.println("GREET BACK ROUTINE");
                        //Greet Back: instead of waiting for the discovery, this device takes
                        //a BluetoothDevice object from this ChatUser and greets it

                        dvc = user.getNextNode();
                        BlueCtrl.closeDvc.put(dvc.getAddress(), dvc);
                        /*
                        New known reachable device; note that, in order to filter unnecessary Greetings,
                        a greeter device adds the remote device BluetoothDevice instance right after
                        it finished sending the Greeting; when it receives an Instant Greeting it just
                        updates the value into the map
                        */

                        BlueCtrl.userList.add(user);
                        if (user.isFav()) BlueCtrl.favList.add(user);
                        //Add found user in the user list

                        ArrayList<byte[]> updCascade = new ArrayList<>();
                        for (ChatUser ch : BlueCtrl.userList) {
                            if (!ch.getMac().equals(user.getMac())) {
                                updCascade.add(ch.getSegment());
                            }
                        }

                        if (updCascade.size() > 0) {
                            BlueCtrl.sendMsg(dvc, BlueCtrl.buildUpdMsg(updCascade), handler);
                            System.out.println("UPDATE CASCADE");
                        }
                        //Send an Update Cascade to the newly found device, in order to connect it to the network

                        ArrayList<byte[]> singleupd = new ArrayList<>();
                        singleupd.add(user.getSegment());

                        BlueCtrl.dispatchNews(BlueCtrl.buildUpdMsg(singleupd), dvc, handler);
                        //send the greeted user segment information to all reachable devices

                        break;

                    case BlueCtrl.UPD_HEADER:
                        //Some device has successfully notified the existence of other devices to this device

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN ASSIGNED TO " + msg.getData().getString("MAC"));
                        //Restore Token

                        if (BlueCtrl.userQueue.size() > 0) {
                            user = BlueCtrl.userQueue.remove(0);
                            BlueCtrl.userList.add(user);
                            if (user.isFav()) BlueCtrl.favList.add(user);
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

                    case BlueCtrl.DRP_HEADER:
                        //Some device successfully sent the MAC address of a lost device

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN " + BlueCtrl.TKN);
                        //Restore Token

                        user = BlueCtrl.scanUsers(msg.getData().getString("LST"));
                        BlueCtrl.userList.remove(user);
                        BlueCtrl.favList.remove(user);
                        //Remove user from the RecyclerView

                        break;

                    case BlueCtrl.INVISIBLE:

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN " + BlueCtrl.TKN);
                        //Restore Token

                        user = BlueCtrl.scanUsers(msg.getData().getString("MAC"));
                        BlueCtrl.userList.remove(user);
                        BlueCtrl.favList.remove(user);

                        break;

                    case BlueCtrl.ACK:

                        BlueCtrl.tokenMap.put(msg.getData().getString("MAC"), BlueCtrl.TKN);
                        System.out.println("TOKEN " + BlueCtrl.TKN);
                        //Restore Token

                        break;

                    case BlueCtrl.NAK:
                        //This device failed to deliver a message to another device; there is need for a message recovery

                        mac = msg.getData().getString("MAC");
                        //MAC address of the wannabe receiver

                        counter = BlueCtrl.tokenMap.get(mac);
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

                            ArrayList<ChatUser> lstDvcs = (ArrayList<ChatUser>) BlueCtrl.dropUsers(mac);

                            new AsyncScavenger(handler).execute((Runnable) lstDvcs.iterator()); //DROP routine

                            for (ChatUser u : lstDvcs) {
                                BlueCtrl.favList.remove(u); //Remove from GUI
                            }
                        }
                        else {

                            //Connection failed, but the remote device could be simply busy;
                            //a Token is taken from him, and this device tries to contact it again

                            BlueCtrl.tokenMap.put(mac, --counter);
                            System.out.println(mac + " TOKEN " + BlueCtrl.tokenMap.get(mac));

                            dvc = BlueCtrl.scanUsersForDvc(msg.getData().getString("MAC"));
                            //Find the device
                            byte[] mail = msg.getData().getByteArray("MSG");
                            //get back the unsent message
                            if (dvc != null) {
                                BlueCtrl.sendMsg(dvc, mail, handler);
                                System.out.println("RE-SENDING MESSAGE NUMBER " + mail[0]);
                                //re-send
                            }
                            //if no user was found it probabily is no longer reachable, and
                            //there's no point in forwarding the message again
                            //END OF RESEND CHAIN
                        }

                        break;

                    case BlueCtrl.LST:
                        //Receive list of dropped devices

                        byte[] tmp = msg.getData().getByteArray("LST");
                        if (tmp.length > 0) {

                            byte[] drp = new byte[tmp.length + 2];
                            drp[0] = BlueCtrl.DRP_HEADER;
                            drp[1] = (byte) (tmp.length / 6); //LST packages have always multiple of 6 length because they are all MACs

                            System.arraycopy(tmp, 0, drp, 2, tmp.length);

                            BlueCtrl.dispatchNews(drp, BlueCtrl.closeDvc.get(msg.getData().getString("MAC")), handler);
                        }

                        break;
                }

                BlueCtrl.userAdapt.notifyDataSetChanged();
                //notify any changes to the List Adapter

            }
        };

        (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {

                            try {
                                Thread.sleep(10000l);
                            } catch (InterruptedException e) {
                                BlueCtrl.dispatchNews(new byte[]{BlueCtrl.ACK}, null, handler);
                            }
                        }}
                })).start();
        //ACK mechanism to catch no longer connected devices

        ListFragment listFragment = new ListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.add(R.id.containerChat, listFragment, "LIST_FRAGMENT");
        fragmentTransaction.commit();

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        drawerLayout = (DrawerLayout) inflater.inflate(R.layout.nav_drawer, null);

        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        FrameLayout container = (FrameLayout) drawerLayout.findViewById(R.id.nav_container);
        container.addView(child);
        decor.addView(drawerLayout);

        ((TextView) findViewById(R.id.username_drawer)).setText(getSharedPreferences("preferences", MODE_PRIVATE).getString("username", "Unknown"));

        ListView listViewMenu = (ListView) findViewById(R.id.list_menu);
        MenuAdapter adapter = new MenuAdapter(this, R.layout.menu_item_layout);
        listViewMenu.setAdapter(adapter);
        adapter.add(getString(R.string.profile));
        adapter.add(getString(R.string.settings));
        adapter.add(getString(R.string.history));
        listViewMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {

                    case 0:
                        Intent intent = new Intent(
                                getApplicationContext(),
                                ProfileActivity.class);
                        startActivity(intent);
                        //start ProfileActivity
                        break;

                    case 1:
                        intent = new Intent(
                                getApplicationContext(),
                                SettingActivity.class);
                        startActivityForResult(intent, 42);
                        //Start SettingActivity
                        break;

                    case 2:
                        intent = new Intent(
                                getApplicationContext(),
                                HistoryActivity.class);
                        startActivity(intent);
                        //start HistoryActivity
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
                getActionBar().setTitle(R.string.contact_list);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /**
            * Called when a drawer has settled in a completely open state.
            */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(R.string.menu);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        String path = getSharedPreferences("preferences", Context.MODE_PRIVATE).getString("PROFILE_PIC", "NoPhoto");
        Bitmap bitmap;
        if (path.equals("NoPhoto")) bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        else {

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(path, bmOptions);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float ratio;

        if (w < h){
            ratio = 240 / (float)w;
        }
        else {
            ratio = 180 / (float)h;
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.floor(bitmap.getWidth() * ratio), (int) Math.floor(bitmap.getHeight() * ratio),false);
        bitmap = Bitmap.createBitmap(bitmap,(bitmap.getWidth() -  240) / 2 , (bitmap.getHeight() -  180) / 2, 240, 180);
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        ((ImageView) findViewById(R.id.image_drawer)).setImageDrawable(new BitmapDrawable(bitmap));
        //Drawer Image

        switchVisibility = (Switch) findViewById(R.id.switch_state); //visibility switch
        (findViewById(R.id.image_switch)).setBackground(getDrawable(R.mipmap.visibility));

        switchVisibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    BlueCtrl.STS = (byte) ((switchVisibility.isChecked()) ? 0 : 1);
                    BlueCtrl.dispatchNews(BlueCtrl.buildGrtMsg(), null, handler);
                    //Switch to Spy Mode
                }
            });


        final Switch switchSpy = (Switch) findViewById(R.id.switch_spy); //Spy Mode switch
        switchSpy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BlueCtrl.SPY = (byte) ((switchSpy.isChecked()) ? 1 : 0);
            }
        });

        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //LOGOUT

                getSharedPreferences("preferences", Context.MODE_PRIVATE).edit().putBoolean("logged in", false).apply();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        SharedPreferences sh = getSharedPreferences("preferences", MODE_PRIVATE);
        BlueCtrl.openDatabase(this);

        //SAVING USER PROFILE IN DATABASE
        if (getIntent().getBooleanExtra("newbie", false)) { //First login

            long timestamp = sh.getLong("timestamp", 0), age = sh.getLong("birth_timestamp", 0);

            BlueCtrl.insertUserTable(BluetoothAdapter.getDefaultAdapter().getAddress(), timestamp, sh.getString("username", "Unknown"), age, sh.getInt("gender", 0), sh.getInt("country", 0));
        }

        BlueCtrl.msgAdapt = new MessageAdapter(this, R.layout.listitem_discuss); //MessageAdapter initialized
        BlueCtrl.msgAdapt.setAddress("");
        BlueCtrl.emoticons = new EmoticonAdapter(this, R.layout.item_emoticon_picker); //EmoticonAdapter initialized

        try {
            (new ServerThread(BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("com.example.christian.chatbluetooth", UUID.fromString(BlueCtrl.UUID)), handler)).start();
            //Initiating listening routine
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("LISTEN FAILED");
        }

        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(this.blueReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(this.blueReceiver, new IntentFilter(String.valueOf(BluetoothAdapter.ACTION_STATE_CHANGED)));
        //Registering intents for BroadcastService routines

        BluetoothAdapter.getDefaultAdapter().startDiscovery(); //Begin discovery
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 42 && resultCode == RESULT_OK) {

            SharedPreferences sh = getSharedPreferences("preferences", Context.MODE_PRIVATE);
            ((TextView) findViewById(R.id.username_drawer)).setText(sh.getString("username", "Unknown"));

            String path = sh.getString("PROFILE_PIC", "NoPhoto");
            Bitmap bitmap;
            if (path.equals("NoPhoto")) bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
            else {

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(path, bmOptions);
            }

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            float ratio;

            if (w < h){
                ratio = 240 / (float)w;
            }
            else {
                ratio = 180 / (float)h;
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.floor(bitmap.getWidth() * ratio), (int) Math.floor(bitmap.getHeight() * ratio),false);
            bitmap = Bitmap.createBitmap(bitmap,(bitmap.getWidth() -  240) / 2 , (bitmap.getHeight() -  180) / 2, 240, 180);
            bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
            ((ImageView) findViewById(R.id.image_drawer)).setImageDrawable(new BitmapDrawable(bitmap));
        }
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getFragmentManager().findFragmentByTag("LIST_FRAGMENT"); //Main fragment

        if (fragment != null) {

            if(fragment.isVisible()) { //Currently on ListFragment

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                //Quit application
            }
            else {

                ListFragment listFragment = new ListFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                fragmentTransaction.add(R.id.containerChat, listFragment, "LIST_FRAGMENT");
                fragmentTransaction.commit();
                //Not in ListFragment anymore, go back there
            }
        }

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

        if (id == android.R.id.home) {

            if (state) {

                ListFragment listFragment = new ListFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                fragmentTransaction.replace(R.id.containerChat, listFragment, "LIST_FRAGMENT");
                fragmentTransaction.commit();

                View view = this.getCurrentFocus();
                if (view != null) {

                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                state = false;
            } else {
                drawerLayout.openDrawer(findViewById(R.id.left_drawer));
            }
        }


        if (id == R.id.action_fav){
            if(!state2) {

                item.setIcon(R.drawable.contacts);
                System.out.println("PIPPO BAUDO");
                ListFragment listFragment = new ListFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                fragmentTransaction.replace(R.id.containerChat, listFragment, "FAVORITES");
                fragmentTransaction.commit();
                state2 = true;
            }

            else{

                item.setIcon(R.drawable.favorite);
                System.out.println("PIPPO BAUDO");
                ListFragment listFragment = new ListFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.containerChat, listFragment, "LIST_FRAGMENT");
                fragmentTransaction.commit();
                state2 = false;
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
    public void onFragmentInteraction(Uri uri) {

    }
}
