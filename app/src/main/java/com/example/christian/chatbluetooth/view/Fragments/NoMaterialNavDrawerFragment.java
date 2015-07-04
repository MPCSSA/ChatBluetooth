package com.example.christian.chatbluetooth.view.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.view.Activities.HistoryActivity;
import com.example.christian.chatbluetooth.view.Activities.ProfileActivity;
import com.example.christian.chatbluetooth.view.Activities.SettingActivity;
import com.example.christian.chatbluetooth.view.Adapters.MenuAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NoMaterialNavDrawerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NoMaterialNavDrawerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoMaterialNavDrawerFragment extends Fragment {
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
     * @return A new instance of fragment NoMaterialNavDrawerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoMaterialNavDrawerFragment newInstance(String param1, String param2) {
        NoMaterialNavDrawerFragment fragment = new NoMaterialNavDrawerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NoMaterialNavDrawerFragment() {
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
        return inflater.inflate(R.layout.fragment_nav_drawer_nomat, container, false);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ViewGroup decor = (ViewGroup) getActivity().getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        FrameLayout container = (FrameLayout) getActivity().findViewById(R.id.drawerLayout);
        container.addView(child);
        decor.addView(container);
        ((TextView) container.findViewById(R.id.username_drawer)).setText(getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE).getString("username", "None"));
        ListView listViewMenu = (ListView) container.findViewById(R.id.list_menu);
        listViewMenu.setAdapter(new MenuAdapter(getActivity(), R.layout.menu_item_layout));
        ((ArrayAdapter) listViewMenu.getAdapter()).add("Profilo");
        ((ArrayAdapter) listViewMenu.getAdapter()).add("Impostazioni");
        ((ArrayAdapter) listViewMenu.getAdapter()).add("Cronologia");
        listViewMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(
                                getActivity().getApplicationContext(),
                                ProfileActivity.class
                        );
                        startActivity(intent);
                        break;

                    case 1:
                        intent = new Intent(
                                getActivity().getApplicationContext(),
                                SettingActivity.class
                        );
                        startActivity(intent);
                        break;

                    case 2:
                        intent = new Intent(
                                getActivity().getApplicationContext(),
                                HistoryActivity.class
                        );
                        startActivity(intent);
                        break;
                }
            }
        });

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        int[] pixel = new int[2];
        pixel[0] = (bmp.getWidth() - 240) / 2;
        pixel[1] = (bmp.getHeight() - 180) / 2;
        bmp = Bitmap.createScaledBitmap(bmp, 240, 180, false);
        bmp.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        ((ImageView) container.findViewById(R.id.image_drawer)).setImageDrawable(new BitmapDrawable(bmp));
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
