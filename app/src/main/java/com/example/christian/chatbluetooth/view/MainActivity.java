package com.example.christian.chatbluetooth.view;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;


public class MainActivity extends Activity implements LoginFragment.OnFragmentInteractionListener,
        RegistrationFragment.OnFragmentInteractionListener, View.OnClickListener{

    //TODO: encapsulate ChatFragment and UsersFragment

    private LoginFragment loginFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.regButton:
                //TODO: password confirmation and null fields control
                SharedPreferences preferences = getSharedPreferences(BlueCtrl.UUID, MODE_PRIVATE);
                preferences.edit().putString("username", ((EditText)findViewById(R.id.regName)).getText().toString());
                preferences.edit().putString("password", ((EditText)findViewById(R.id.regPass)).getText().toString());
                preferences.edit().putLong("birth", 0);
                preferences.edit().putString("gender", "none");
                preferences.edit().putString("nationality", "chinese");
                break;

            case R.id.loginButton:
                //TODO: username/password check
                //TODO: set remember user
        }
    }
}
