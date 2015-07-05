package com.example.christian.chatbluetooth.view.Activities;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Fragments.LoginFragment;
import com.example.christian.chatbluetooth.view.Fragments.RegistrationFragment;


public class MainActivity extends Activity implements LoginFragment.OnFragmentInteractionListener,
        RegistrationFragment.OnFragmentInteractionListener {

    //TODO: encapsulate ChatFragment and UsersFragment

    private LoginFragment loginFragment;
    private boolean okPass = false, okConf = false, registered = false;


    public boolean getOkPass() {
        return okPass;
    }
    public void setOkPass(boolean bool) {
        okPass = bool;
    }

    public boolean getOkConf() {
        return okConf;
    }
    public void setOkConf(boolean bool) {
        okConf = bool;
    }

    public boolean isNew() { return this.registered; }
    public void registered() { this.registered = true; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    /*
    DEBUG ONLY
    */
        BlueCtrl.version = Build.VERSION.SDK_INT >= 21;
    /*
    DEBUG ONLY
    */

        loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, loginFragment);
        fragmentTransaction.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
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
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            fragmentTransaction.replace(R.id.container, loginFragment);
            fragmentTransaction.commit();
            ActionBar actionBar = this.getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
