package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.view.Adapters.SettingAdapter;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private String imageNameFile;
    private File image;

    private PopupWindow pwindo;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
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
        return inflater.inflate(R.layout.fragment_settings, container, false);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(getString(R.string.setting_activity));

        ListView settingList = (ListView) getActivity().findViewById(R.id.setting_list);


        Point point = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);
        System.out.println("La larghezza: " + point.x);
        int width = point.x;
        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);*/

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        bmp = Bitmap.createScaledBitmap(bmp, width, (9*width)/16, false);
        bmp.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.setting_image);
        imageView.setImageDrawable(new BitmapDrawable(bmp));

        settingList.setAdapter(new SettingAdapter(getActivity(), R.layout.setting_item));

        ((ArrayAdapter<String>)settingList.getAdapter()).add(getString(R.string.setting_mod_nick));
        ((ArrayAdapter<String>)settingList.getAdapter()).add(getString(R.string.setting_mod_history));

        settingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.nick_popup,
                            (ViewGroup) getActivity().findViewById(R.id.popup_nick));
                    int win_width;
                    int win_height;
                    Point point = new Point();
                    getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);

                    win_width = (point.x)*2/3;
                    win_height = (point.y)/3;
                    pwindo = new PopupWindow(layout, win_width, win_height, true);
                    pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

                    final EditText nickEditText = (EditText) layout.findViewById(R.id.nickModText);
                    Button btnNickConfirm = (Button) layout.findViewById(R.id.btn_mod_popup);
                    Button btnNickCanc = (Button) layout.findViewById(R.id.btn_canc_popup);

                    btnNickConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String newNick = nickEditText.getText().toString();
                            System.out.println(newNick);
                            pwindo.dismiss();
                        }
                    });

                    btnNickCanc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pwindo.dismiss();
                        }
                    });
                }

                if (position == 1){
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.history_popup,
                            (ViewGroup) getActivity().findViewById(R.id.popup_element));
                    int win_width;
                    int win_height;
                    Point point = new Point();
                    getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);

                    win_width = (point.x)*2/3;
                    win_height = (point.y)/3;
                    pwindo = new PopupWindow(layout, win_width, win_height, true);
                    pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

                    Button btnYes = (Button) layout.findViewById(R.id.btn_yes_popup);
                    Button btnNo = (Button) layout.findViewById(R.id.btn_yes_popup);

                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pwindo.dismiss();
                        }
                    });

                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pwindo.dismiss();
                        }
                    });
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        System.out.println("Create failed!");
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageNameFile = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageNameFile,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        System.out.println("Path: " + mCurrentPhotoPath);
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == getActivity().RESULT_OK) {

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

            Point point = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);
            int width = point.x;
            bitmap = Bitmap.createScaledBitmap(bitmap, width, (width*9)/16, false);
            ImageView imageView = (ImageView) getActivity().findViewById(R.id.setting_image);
            imageView.setImageBitmap(bitmap);
        }
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
