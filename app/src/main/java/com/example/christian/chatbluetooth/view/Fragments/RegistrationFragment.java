package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Activities.MainActivity;
import com.example.christian.chatbluetooth.view.ConfirmationWatcher;
import com.example.christian.chatbluetooth.view.PasswordWatcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegistrationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegistrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistrationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Button registration;
    private Spinner nations;
    private EditText name;
    private EditText passw;
    private EditText confirmPassw;
    private TextView date;
    private RadioGroup gender;
    private RadioButton male;
    private RadioButton female;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegistrationFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        /*
        DEBUG ONLY
        */
        int layout = (BlueCtrl.version) ? R.layout.fragment_registration : R.layout.fragment_registration_nomat;
        return inflater.inflate(layout, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
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

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        name = (EditText) getActivity().findViewById(R.id.et_reg_username);
        passw = (EditText) getActivity().findViewById(R.id.et_reg_password);
        confirmPassw = (EditText) getActivity().findViewById(R.id.et_confirm);
        date = (TextView) getActivity().findViewById(R.id.tv_birth);
        male = (RadioButton) getActivity().findViewById(R.id.rbtn_male);
        female = (RadioButton) getActivity().findViewById(R.id.rbtn_fem);
        registration = (Button) getActivity().findViewById(R.id.btn_signup);

        name.setTypeface(type);
        passw.setTypeface(type);
        confirmPassw.setTypeface(type);
        male.setTypeface(type);
        female.setTypeface(type);
        registration.setTypeface(type);

        passw.addTextChangedListener(new PasswordWatcher(getActivity()));
        confirmPassw.addTextChangedListener(new ConfirmationWatcher(getActivity()));

        SpannableString title = new SpannableString("Registrazione");
        title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(title);

        this.nations = (Spinner) getActivity().findViewById(R.id.spin_nations);
        List<String> list = new ArrayList<String>();
        list.add("Nazione di nascita");
        list.add("Francia");
        list.add("Germania");
        list.add("Inghilterra");
        list.add("Italia");
        list.add("Spagna");
        int spinner_item = (BlueCtrl.version) ? R.layout.spinner_item : R.layout.spinner_item_nomat;
        ArrayAdapter<String> dataAdapter= new ArrayAdapter<String>(getActivity(), spinner_item, list);
        nations.setAdapter(dataAdapter);

        /*
        PRENDERE GIORNO,MESE , ANNO :
        //    gg / mm / aaaa
        //    01 2 34 5 6789
        String Data = data.getText().toString();
        int Giorno = Integer.parseInt(Data.substring(0,2));
        int Mese = Integer.parseInt(Data.substring(3,5));
        String Anno = Data.substring(6);

        NNNOPE
        Gli oggetti Date vengono istanziati tramite long int; è meglio prendere la data come somma di long int

        */

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog();
            }
        });

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getActivity().getSharedPreferences("preferences", getActivity().MODE_PRIVATE);

                String User = preferences.getString("username", null); // null is a default value if they don't exist
                String Pass = preferences.getString("password", null); // null is a default value if they don't exist

                if (User == null && Pass == null) {

                    if (name.getText().toString() != null && !((MainActivity) getActivity()).getOkPass()) {

                        if (((MainActivity) getActivity()).getOkConf()) {

                            Date creation = new Date();

                            preferences.edit().putString("username", name.getText().toString()).apply();
                            preferences.edit().putString("password", passw.getText().toString()).apply();
                            preferences.edit().putLong("timestamp", creation.getTime()).apply();

                            preferences.edit().putString("birth", date.getText().toString());

                            int gender;
                            if ((getActivity().findViewById(R.id.radioGroup_reg)).isSelected()) {
                                gender = (((RadioButton) getActivity().findViewById(R.id.rbtn_male)).isChecked()) ? 1 : 2;
                            }
                            else gender = 0;
                            preferences.edit().putInt("gender", gender).apply();

                            preferences.edit().putInt("nationality", ((Spinner) getActivity().findViewById(R.id.spin_nations)).getSelectedItemPosition()).apply();

                            //BlueCtrl.bindUser(preferences);
                            ((MainActivity)getActivity()).registered();

                            LoginFragment fragment = new LoginFragment();
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            /*DEBUG ONLY*/
                            if (BlueCtrl.version) fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                            fragmentTransaction.replace(R.id.container, fragment);
                            fragmentTransaction.commit();

                        } else
                            Toast.makeText(getActivity(), "Confirm password!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getActivity(), "Check username and password fields!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getActivity(), "You are already registered", Toast.LENGTH_SHORT).show();
                    //TODO: Cancel the registration or not?
                }
            }
        });

    }

    public void DateDialog(){

        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth)
            {
                //prima di mandare il tutto alla textview aggiusto i parametri in modo da essere uniformi a gg/mm/aaaa
                String Toset = "";
                if(dayOfMonth < 10)
                    Toset="0";
                Toset = Toset + dayOfMonth + " ";
                if(monthOfYear < 10)
                    Toset = Toset + "0";
                Toset = Toset + (monthOfYear+1) + " " + year;

                date.setText(Toset);

                //DEBUG ONLY
                if (BlueCtrl.version) {
                    date.setTextColor(getResources().getColor(R.color.primary_text));
                    date.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf"));
                }
                //DEBUG ONLY

            }};
        DatePickerDialog dpDialog=new DatePickerDialog(getActivity(), R.style.DialogTheme,listener, 1980, 1,1);
        dpDialog.show();//mostra la dialog

    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
