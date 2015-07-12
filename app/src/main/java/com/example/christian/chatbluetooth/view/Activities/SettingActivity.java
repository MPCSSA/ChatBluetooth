package com.example.christian.chatbluetooth.view.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Fragments.SettingsFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingActivity extends Activity implements SettingsFragment.OnFragmentInteractionListener {

    public boolean[] checked = new boolean[3]; //CheckBoxes status before entering
    public int gender, country, flag = 0;
    public String usr, birth;
    //User information before changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //Call main fragment
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
            //Check for changes and update User Information; user information can be skipped at registration
            //and inserted here

            SharedPreferences sh = getSharedPreferences("preferences", MODE_PRIVATE);

            if (!usr.equals(sh.getString("username", "Unknown")) ||
                !birth.equals(sh.getString("birth", null)) ||
                gender != sh.getInt("gender", 0) ||
                flag != sh.getInt("country", 0) ||
                checked[0] != sh.getBoolean("COUNTRY_VISIBLE", true) ||
                checked[1] != sh.getBoolean("AGE_VISIBLE", true) ||
                checked[2] != sh.getBoolean("GENDER_VISIBLE", true)) {
                //if any of those fields were changed, save them all into SharedPreferences and update user record

                sh.edit().putString("username", usr).apply(); //new username

                sh.edit().putString("birth", birth).apply(); //new birth date
                long birthday;
                try {
                    birthday = (new SimpleDateFormat("dd mm yyyy")).parse(birth.replace("/", " ")).getTime();
                } catch (ParseException ignore) {
                    birthday = 0l;
                }
                sh.edit().putLong("birth_timestamp", birthday).apply(); //new birth date timestamp

                sh.edit().putInt("gender", gender).apply(); //new gender

                sh.edit().putInt("country", country).apply(); //new country

                sh.edit().putBoolean("COUNTRY_VISIBLE", checked[0]).apply();
                sh.edit().putBoolean("AGE_VISIBLE", checked[1]).apply();
                sh.edit().putBoolean("GENDER_VISIBLE", checked[2]).apply();
                //Checked CheckBoxes

                long timestamp = (new Date()).getTime();
                sh.edit().putLong("timestamp", timestamp).apply(); //new LastUpd field

                int country = (checked[0]) ? this.country : 0;
                long age = (checked[1]) ? birthday : 0l;
                int gender = (checked[2]) ? this.gender : 0;
                BlueCtrl.updateProfile(timestamp, usr, country, gender, age);
                //DB contains public user information, therefore if some are hidden change to default value

                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            }

            super.onBackPressed();
            //get back
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
