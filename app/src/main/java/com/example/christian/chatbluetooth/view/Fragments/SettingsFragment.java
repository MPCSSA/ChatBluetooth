package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
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
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.Country;
import com.example.christian.chatbluetooth.view.Adapters.CountryAdapter;
import com.example.christian.chatbluetooth.view.Adapters.SettingAdapter;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        SettingAdapter adapt = new SettingAdapter(getActivity(), R.layout.setting_item);
        settingList.setAdapter(adapt);

        adapt.add(getString(R.string.setting_mod_nick));
        adapt.add(getString(R.string.setting_mod_country));
        adapt.add(getString(R.string.setting_mod_age));
        adapt.add(getString(R.string.setting_mod_gender));

        settingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {

                    case 0: //Modify username

                        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.popup_username,
                                (ViewGroup) getActivity().findViewById(R.id.popup_nick));

                        int win_width;
                        int win_height;
                        Point point = new Point();
                        getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);

                        win_width = (point.x) * 2 / 3;
                        win_height = (point.y) / 3;

                        final PopupWindow usernamePopup = new PopupWindow(layout, win_width, win_height, true);
                        usernamePopup.setBackgroundDrawable(new BitmapDrawable());
                        usernamePopup.setAnimationStyle(R.style.SettingsPopup);
                        usernamePopup.showAtLocation(layout, Gravity.CENTER, 0, 0);

                        final EditText nickEditText = (EditText) layout.findViewById(R.id.nickModText);
                        Button btnNameConfirm = (Button) layout.findViewById(R.id.btn_mod_popup);
                        Button btnNameCanc = (Button) layout.findViewById(R.id.btn_canc_popup);

                        btnNameConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newNick = nickEditText.getText().toString();
                                System.out.println(newNick);
                                usernamePopup.dismiss();
                            }
                        });

                        btnNameCanc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                usernamePopup.dismiss();
                            }
                        });

                        break;

                    case 1:

                        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        layout = inflater.inflate(R.layout.popup_countries,
                                (ViewGroup) getActivity().findViewById(R.id.country_layout));

                        ListView countries = (ListView) layout.findViewById(R.id.list_countries);
                        CountryAdapter adapter = new CountryAdapter(getActivity(), R.layout.item_countries);

                        Cursor flag_cursor = BlueCtrl.fetchFlags(Locale.getDefault().getDisplayCountry());
                        Country country;
                        if (flag_cursor.moveToFirst()) {

                            do {

                                 adapter.add(new Country(flag_cursor.getString(0), flag_cursor.getInt(1)));
                            } while(flag_cursor.moveToNext());
                        }

                        countries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        });

                        point = new Point();
                        getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);

                        win_width = (point.x) * 2 / 3;
                        win_height = (point.y) / 3;

                        final PopupWindow countryPopup = new PopupWindow(layout, win_width, win_height, true);
                        countryPopup.setBackgroundDrawable(new BitmapDrawable());
                        countryPopup.setAnimationStyle(R.style.SettingsPopup);
                        countryPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);


                        break;

                    case 2:

                        DateDialog();

                        break;

                    case 3: //TODO GENDER

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

    public void DateDialog() {

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                TextView tv = ((TextView) view.findViewById(R.id.set_item));

                //prima di mandare il tutto alla textview aggiusto i parametri in modo da essere uniformi a gg/mm/aaaa
                String Toset = "";
                if (dayOfMonth < 10)
                    Toset = "0";
                Toset = Toset + dayOfMonth + "/";
                if (monthOfYear < 10)
                    Toset = Toset + "0";
                Toset = Toset + (monthOfYear + 1) + "/" + year;

                tv.setText(Toset);

                //DEBUG ONLY
                if (BlueCtrl.version) {
                    tv.setTextColor(getResources().getColor(R.color.primary_text));
                    tv.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf"));
                }
                //DEBUG ONLY

                tv.clearFocus();

                getActivity().findViewById(R.id.setting_layout).requestFocus();
            }
        };
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
