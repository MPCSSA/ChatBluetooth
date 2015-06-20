package com.example.christian.chatbluetooth.view;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;


public class MainActivity extends Activity implements LoginFragment.OnFragmentInteractionListener,
        RegistrationFragment.OnFragmentInteractionListener, View.OnClickListener {

    //TODO: encapsulate ChatFragment and UsersFragment

    private LoginFragment loginFragment;
    private boolean okPass = false, okConf = false;


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
            case R.id.btn_signup:

                if (((EditText)findViewById(R.id.et_reg_username)).getText().toString() != null && okPass) {

                    if (okConf) {

                        SharedPreferences preferences = getSharedPreferences(BlueCtrl.UUID, MODE_PRIVATE);
                        preferences.edit().putString("username", ((EditText) findViewById(R.id.et_reg_username)).getText().toString());
                        preferences.edit().putString("password", ((EditText) findViewById(R.id.et_reg_password)).getText().toString());
                        preferences.edit().putLong("birth", 0);
                        if ((findViewById(R.id.radioGroup_reg)).isPressed())
                            preferences.edit().putString("gender", findViewById((((RadioGroup) findViewById(R.id.radioGroup_reg)).getCheckedRadioButtonId())).getTag().toString());
                        preferences.edit().putString("nationality", ((Spinner)findViewById(R.id.spin_nations)).getSelectedItem().toString());
                        break;
                    }
                    else Toast.makeText(this, "Confirm password!", Toast.LENGTH_SHORT).show();
                }

                else Toast.makeText(this, "Check username and password fields!", Toast.LENGTH_SHORT).show();

            case R.id.loginButton:
                //TODO: username/password check
                //TODO: set remember user
        }
    }
}
