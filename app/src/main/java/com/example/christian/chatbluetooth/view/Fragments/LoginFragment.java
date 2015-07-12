package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Activities.ChatActivity;
import com.example.christian.chatbluetooth.view.Activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    EditText userField;
    EditText passwField;
    String username;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layout;
        if ((getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE).getBoolean("logged in", false)))
            layout = R.layout.logo_layout;
        else layout = R.layout.fragment_login;
        //Show LoginFragment only at first run or after user logged out, else load an empty Fragment
        // Inflate the layout for this fragment
        return inflater.inflate(layout, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    @Override
    public void onActivityCreated(Bundle savedIstanceState){
        super.onActivityCreated(savedIstanceState);


        if (getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE).getBoolean("logged in", false)) {

            //On first run and after user logged out, skip Login routine and load ChatActivity
            Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivityForResult(discoverable, 1);
            //Request Bluetooth discoverable option
        }

        else { //show Login Fragment

            Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

            userField = (EditText) getActivity().findViewById(R.id.logName);
            passwField = (EditText) getActivity().findViewById(R.id.logPass);

            Button login = (Button) getActivity().findViewById(R.id.loginButton);
            Button registration = (Button) getActivity().findViewById(R.id.logRegButton);

            SpannableString title = new SpannableString(getString(R.string.login));
            title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ActionBar actionBar = getActivity().getActionBar();
            actionBar.setTitle(title);
            //Login Fragment items initialization


            userField.setTypeface(type);
            passwField.setTypeface(type);
            login.setTypeface(type);
            registration.setTypeface(type);

            registration = (Button) getActivity().findViewById(R.id.logRegButton); //Register Button
            registration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Show RegistrationFragment
                    RegistrationFragment registrationFragment = new RegistrationFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.container, registrationFragment);
                    fragmentTransaction.commit();
                }
            });

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log in the application
                    SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
                    //SharedPreferences stores user information and authentication data

                    username = preferences.getString("username", null); // null is a default value when username doesn't exist
                    String password = preferences.getString("password", null); // null is a default value when password doesn't exist

                    if (username == null) {

                        //Not registered
                        Toast.makeText(getActivity(), getString(R.string.must_register), Toast.LENGTH_SHORT).show();
                    }
                    else if (username != null && password != null) {
                        //User registered

                        if (userField.getText().toString().equals(username) && passwField.getText().toString().equals(password)) {

                            //Entered valid authentication data, call ChatActivity through Discoverable Intent
                            Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                            startActivityForResult(discoverable, 1);
                        }
                        else {

                            // Wrong username or password
                            Toast.makeText(getActivity(), getString(R.string.wrong), Toast.LENGTH_SHORT).show();
                            userField.requestFocus();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Before showing ChatActivity, the user must allow the application to turn on Bluetooth and set
        //indefinite discoverable state

        if (requestCode == 1) {
            //only code that should pop up

            if (resultCode == 1) {
                //user agreed to turn on Bluetooth and be discoverable

                SharedPreferences sh = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
                if (sh.getBoolean("1stRun", true)) {

                    BlueCtrl.closeDB(); //Database opened at first run for tables initialization
                    sh.edit().putBoolean("1stRun", false).apply();
                    //no longer first run
                }

                Toast.makeText(getActivity(), getString(R.string.login_greeting) + sh.getString("username", "Unknown") + "!", Toast.LENGTH_SHORT).show();
                //Welcome Toast
                sh.edit().putBoolean("logged in", true).apply();
                //Set user as logged in for future sessions

                Intent intent = new Intent(
                        getActivity().getApplicationContext(),
                        ChatActivity.class
                );
                intent.putExtra("newbie", ((MainActivity) getActivity()).isNew());
                startActivity(intent);
                //Call the activity, sending a boolean value to force it enter registration information
            }
            else getActivity().finish(); //User did not agree to turn on Bleutooth, so the application won't start
        }
        else getActivity().finish(); //Should not happen
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
