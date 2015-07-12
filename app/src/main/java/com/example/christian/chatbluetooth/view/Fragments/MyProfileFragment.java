package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.Country;
import com.example.christian.chatbluetooth.view.Adapters.MyProfileAdapter;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyProfileFragment newInstance(String param1, String param2) {
        MyProfileFragment fragment = new MyProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MyProfileFragment() {
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        SpannableString title = new SpannableString(getString(R.string.profile_activity));
        title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(title);

        ListView fieldList = (ListView) getActivity().findViewById(R.id.my_info_list);
        //List of user information

        SharedPreferences sh = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        String path = sh.getString("PROFILE_PIC", "NoPhoto");

        Point point = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);

        Bitmap bitmap;
        if (path.equals("NoPhoto")) bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        else {

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(path, bmOptions);
        }

        int width = point.x, height = point.y;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int wcrop, hcrop;
        float ratio;
        wcrop = width;
        hcrop = width;

        if (w < h){
            ratio = width / (float)w;

        }
        else {
            ratio = height/ (float)h;
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.floor(bitmap.getWidth() * ratio), (int) Math.floor(bitmap.getHeight() * ratio),false);
        bitmap = Bitmap.createBitmap(bitmap,(bitmap.getWidth() -  wcrop) / 2 , (bitmap.getHeight() -  hcrop) / 2, wcrop, hcrop);
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.my_profile_image);
        imageView.setImageDrawable(new BitmapDrawable(bitmap));
        //Displaying Profile Picture in a 1:1 ratio

        MyProfileAdapter adapter = new MyProfileAdapter(getActivity(), R.layout.my_profile_item);
        fieldList.setAdapter(adapter);
        //set Adapter

        adapter.add(new String[]{getString(R.string.username), sh.getString("username", "Unknown")});
        //Insert username; this field is always initialized

        int country = sh.getInt("country", 0); //get country code
        if (country > 0) {

            //if user selected a country show it in the adapter
            Country c = BlueCtrl.fetchFlag(country);
            adapter.add(new String[] {getString(R.string.from), c.getCountry(), String.valueOf(c.getPosition())});
        }

        long timestamp = sh.getLong("birth_timestamp", 0l); //timestamp for age calculation
        if (timestamp > 0) {

            //show age if timestamp value is not default (0)
            int age = (int) (((new Date()).getTime() - timestamp) / 31536000000l); //Age today
            adapter.add(new String[]{getString(R.string.age), String.valueOf(age)});
        }

        int gender = sh.getInt("gender", 0); //user gender
        if (gender > 0) {

            //Show gender if user selected one
            adapter.add(new String[] {String.valueOf(gender)});
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
