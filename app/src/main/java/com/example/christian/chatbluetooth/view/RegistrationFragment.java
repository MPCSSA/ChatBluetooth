package com.example.christian.chatbluetooth.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.ColorRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;

import java.util.ArrayList;
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
        return inflater.inflate(R.layout.fragment_registration, container, false);
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

        Typeface type = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Roboto-Regular.ttf");
        name = (EditText) getActivity().findViewById(R.id.regName);
        passw = (EditText) getActivity().findViewById(R.id.regPass);
        confirmPassw = (EditText) getActivity().findViewById(R.id.regConfirmPass);
        date = (TextView) getActivity().findViewById(R.id.regDate);
        male = (RadioButton) getActivity().findViewById(R.id.radioButton);
        female = (RadioButton) getActivity().findViewById(R.id.radioButton2);
        registration = (Button) getActivity().findViewById(R.id.regButton);

        name.setTypeface(type);
        passw.setTypeface(type);
        confirmPassw.setTypeface(type);
        male.setTypeface(type);
        female.setTypeface(type);
        registration.setTypeface(type);

        SpannableString title = new SpannableString("Registrazione");
        title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(title);

        this.nations = (Spinner) getActivity().findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        list.add("Nazione di nascita");
        list.add("Francia");
        list.add("Germania");
        list.add("Inghilterra");
        list.add("Italia");
        list.add("Spagna");
        ArrayAdapter<String> dataAdapter= new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, list);
        nations.setAdapter(dataAdapter);

        /*
        PRENDERE GIORNO,MESE , ANNO :
        //    gg / mm / aaaa
        //    01 2 34 5 6789
        String Data = data.getText().toString();
        int Giorno = Integer.parseInt(Data.substring(0,2));
        int Mese = Integer.parseInt(Data.substring(3,5));
        String Anno = Data.substring(6);

        */

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog();
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
                Toset = Toset + dayOfMonth + "/";
                if(monthOfYear < 10)
                    Toset = Toset + "0";
                Toset = Toset + (monthOfYear+1) + "/" + year;

                date.setText(Toset);
                date.setTextColor(getResources().getColor(R.color.text));
                date.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Roboto-Regular.ttf"));

            }};
        DatePickerDialog dpDialog=new DatePickerDialog(getActivity(), R.style.DialogTheme,listener, 1980, 1,1);
        dpDialog.show();//mostra la dialog

    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
