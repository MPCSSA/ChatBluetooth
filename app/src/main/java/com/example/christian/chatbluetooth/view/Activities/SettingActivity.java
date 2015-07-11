package com.example.christian.chatbluetooth.view.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Fragments.SettingsFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingActivity extends Activity implements SettingsFragment.OnFragmentInteractionListener {

    public boolean[] checked = new boolean[3], newChecked = new boolean[3];
    public int gender, country, flag = 0;
    public String usr, birth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getActionBar();

        SettingsFragment settingFragment = new SettingsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.add(R.id.setting_container, settingFragment);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
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

        if (id == R.id.action_edit) {

            SharedPreferences sh = getSharedPreferences("preferences", MODE_PRIVATE);

            if (!usr.equals(sh.getString("username", "Unknown")) ||
                !birth.equals(sh.getString("birth", null)) ||
                gender != sh.getInt("gender", 0) ||
                flag != sh.getInt("country", 0) ||
                checked[0] != sh.getBoolean("COUNTRY_VISIBLE", true) ||
                checked[1] != sh.getBoolean("AGE_VISIBLE", true) ||
                checked[2] != sh.getBoolean("GENDER_VISIBLE", true)) {

                sh.edit().putString("username", usr).apply();

                sh.edit().putString("birth", birth).apply();
                long birthday;
                try {
                    birthday = (new SimpleDateFormat("dd mm yyyy")).parse(birth.replace("/", " ")).getTime();
                } catch (ParseException ignore) {
                    birthday = 0l;
                }
                sh.edit().putLong("birth_timestamp", birthday).apply();

                sh.edit().putInt("gender", gender).apply();

                sh.edit().putInt("country", country).apply();

                sh.edit().putBoolean("COUNTRY_VISIBLE", checked[0]).apply();
                sh.edit().putBoolean("AGE_VISIBLE", checked[1]).apply();
                sh.edit().putBoolean("GENDER_VISIBLE", checked[2]).apply();

                sh.edit().putLong("timestamp", (new Date()).getTime()).apply();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
