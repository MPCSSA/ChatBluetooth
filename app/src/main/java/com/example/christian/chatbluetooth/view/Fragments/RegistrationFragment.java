package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Activities.MainActivity;
import com.example.christian.chatbluetooth.view.Adapters.CountryAdapter;
import com.example.christian.chatbluetooth.view.Watchers.ConfirmationWatcher;
import com.example.christian.chatbluetooth.view.Watchers.PasswordWatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegistrationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistrationFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText name;
    private EditText passw;
    private EditText confirmPassw;
    private TextView date;
    private int id = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegistrationFragment.
     */

    public static RegistrationFragment newInstance(String param1, String param2) {
        RegistrationFragment fragment = new RegistrationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RegistrationFragment() {
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false);
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
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //Home Button lets the user go back to Login fragment

        Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

        name = (EditText) getActivity().findViewById(R.id.et_reg_username);
        passw = (EditText) getActivity().findViewById(R.id.et_reg_password);
        confirmPassw = (EditText) getActivity().findViewById(R.id.et_confirm);
        //Fundamental fields forms

        date = (TextView) getActivity().findViewById(R.id.tv_birth);
        ImageView calendar = (ImageView) getActivity().findViewById(R.id.calendar);
        //Birth form

        RadioButton male = (RadioButton) getActivity().findViewById(R.id.rbtn_male);
        RadioButton female = (RadioButton) getActivity().findViewById(R.id.rbtn_fem);
        //Gender RadioButtons

        Button registration = (Button) getActivity().findViewById(R.id.btn_signup); //Confirm registration Button

        name.setTypeface(type);
        passw.setTypeface(type);
        confirmPassw.setTypeface(type);
        male.setTypeface(type);
        female.setTypeface(type);
        registration.setTypeface(type);

        passw.addTextChangedListener(new PasswordWatcher(getActivity()));
        confirmPassw.addTextChangedListener(new ConfirmationWatcher(getActivity()));
        //TextWatchers to guarantee that password is inserted correctly every time AND that is longer than 3 words

        SpannableString title = new SpannableString(getString(R.string.reg_frag));
        title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(title);

        getActivity().findViewById(R.id.world).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show countries list as a PopupWindow

                name.clearFocus();
                passw.clearFocus();
                confirmPassw.clearFocus();

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.popup_countries,
                        (ViewGroup) getActivity().findViewById(R.id.country_popup_layout), false);

                Point point = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);

                int win_width = (point.x) * 2 / 3;
                int win_height = (point.y) / 3;

                final PopupWindow countryPopup = new PopupWindow(v, win_width, win_height, true);
                countryPopup.setBackgroundDrawable(new BitmapDrawable());
                countryPopup.setAnimationStyle(R.style.SettingsPopup);
                countryPopup.showAtLocation(v, Gravity.CENTER, 0, 0);
                //Show Popup

                ListView countries = (ListView) v.findViewById(R.id.list_countries);
                CountryAdapter adapter = new CountryAdapter(getActivity(), R.layout.item_countries);
                countries.setAdapter(adapter);
                //Initialize ListView

                adapter.addAll(BlueCtrl.fetchFlags()); //populate list
                adapter.notifyDataSetChanged();

                countries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //Items all in order, so country code to assign to persistent information
                        //is selected view position + 1 (records in DB starts from _id = 1)

                        id = i + 1;

                        String country = BlueCtrl.fetchFlag(i + 1).getCountry();
                        ((TextView) getActivity().findViewById(R.id.tv_country)).setText(country);
                        //Show country name

                        countryPopup.dismiss(); //dismiss popup
                    }
                });
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show Calendar
                DateDialog();
            }
        });

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Confirm registration
                SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);

                String User = preferences.getString("username", null); // null is a default value if they don't exist
                String Pass = preferences.getString("password", null); // null is a default value if they don't exist
                //These are the only values you cannot skip

                if (User == null && Pass == null) { //No previous registration

                    if (name.getText().toString() != null && !((MainActivity) getActivity()).getOkPass()) {
                        //Username != null and valid password

                        if (((MainActivity) getActivity()).getOkConf()) {
                            //Password correctly confirmed

                            Date creation = new Date(); //time of profile creation

                            preferences.edit().putString("username", name.getText().toString()).apply();
                            preferences.edit().putString("password", passw.getText().toString()).apply();
                            preferences.edit().putLong("timestamp", creation.getTime()).apply();
                            //Add fundamental information

                            //Every field down here can be skipped by the user; they will be initialized with a default value
                            String birth = date.getText().toString();
                            long birthday;
                            try {
                                birthday = (new SimpleDateFormat("dd mm yyyy")).parse(birth.replace("/", " ")).getTime();
                            } catch (ParseException ignore) {
                                birthday = 0l;
                            }

                            preferences.edit().putString("birth", birth).apply();    //birth date
                            preferences.edit().putLong("birth_timestamp", birthday).apply(); //timestamp

                            int gender = 0;
                            if (((RadioButton) getActivity().findViewById(R.id.rbtn_male)).isChecked())
                                gender = 1; //MALE
                            if (((RadioButton) getActivity().findViewById(R.id.rbtn_fem)).isChecked())
                                gender = 2; //FEMALE

                            preferences.edit().putInt("gender", gender).apply();

                            preferences.edit().putInt("country", id).apply(); //country id in DB

                            ((MainActivity) getActivity()).registered(); //set registered field in LoginFragment to true

                            //Show Login Fragment
                            LoginFragment fragment = new LoginFragment();
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                            fragmentTransaction.replace(R.id.container, fragment);
                            fragmentTransaction.commit();

                        }
                        else Toast.makeText(getActivity(), getString(R.string.confirm_pass), Toast.LENGTH_SHORT).show();
                        //Confirm password

                    }
                    else Toast.makeText(getActivity(), getString(R.string.check_fields), Toast.LENGTH_SHORT).show();
                    //Check fields for errors
                }
                else Toast.makeText(getActivity(), getString(R.string.already_registered), Toast.LENGTH_SHORT).show();
                //Cannot register more than one time
            }
        });

    }

    public void DateDialog(){

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth)
            {
                //Get and parse parameters to dd/mm/yyyy pattern, then write it in TextView
                String Toset = "";

                if(dayOfMonth < 10) Toset="0"; //static pattern
                Toset = Toset + dayOfMonth + "/"; //Day of month

                if(monthOfYear < 10) Toset = Toset + "0"; //static pattern
                Toset = Toset + (monthOfYear+1) + "/" + year; //month and year

                date.setText(Toset); //show picked date

                date.setTextColor(getResources().getColor(R.color.primary_text));
                date.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf"));

                date.clearFocus();

                getActivity().findViewById(R.id.lay_reg_page).requestFocus();

            }};

        name.clearFocus();
        passw.clearFocus();
        confirmPassw.clearFocus();
        DatePickerDialog dpDialog=new DatePickerDialog(getActivity(), R.style.DialogTheme,listener, 1980, 1,1);
        dpDialog.show(); //Show Dialog

    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
