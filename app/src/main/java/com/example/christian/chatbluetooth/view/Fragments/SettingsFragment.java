package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.Spannable;
import android.text.SpannableString;
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

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Activities.SettingActivity;
import com.example.christian.chatbluetooth.view.Adapters.CountryAdapter;
import com.example.christian.chatbluetooth.view.Adapters.SettingAdapter;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    SettingActivity activity;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String imageNameFile;

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
        final Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        SpannableString title = new SpannableString(getString(R.string.setting_activity));
        title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(title);

        activity = (SettingActivity) getActivity(); //SettingActivity instance

        SharedPreferences sh = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        //User profile information storage

        activity.checked[0] = sh.getBoolean("COUNTRY_VISIBLE", true);
        activity.checked[1] = sh.getBoolean("AGE_VISIBLE", true);
        activity.checked[2] = sh.getBoolean("GENDER_VISIBLE", true);
        //Where those fields checked before?

        activity.usr = sh.getString("username", "Unknown"); //old username value
        if ((activity.country = sh.getInt("country", 0)) != 0) activity.flag = BlueCtrl.fetchFlag(activity.country).getPosition();
        //old country code
        activity.birth = sh.getString("birth", null); //old birth date
        activity.gender = sh.getInt("gender", 0); //old gender

        activity.mCurrentPhotoPath = sh.getString("PROFILE_PIC", "NoPhoto");

        final ListView settingList = (ListView) getActivity().findViewById(R.id.setting_list);
        //field list

        Point point = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);
        int width = point.x;

        Bitmap bitmap;
        if (activity.mCurrentPhotoPath.equals("NoPhoto")) bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
        else {

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(activity.mCurrentPhotoPath, bmOptions);
        }

        float ratio = width / (float)bitmap.getWidth();

        bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.floor(bitmap.getWidth() * ratio), (int) Math.floor(bitmap.getHeight() * ratio),false);
        int height = (width * 9) / 16;
        bitmap = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() -  height) / 2, width, height);
        bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.setting_image);
        imageView.setImageDrawable(new BitmapDrawable(bitmap));
        //show profile image

        final SettingAdapter adapt = new SettingAdapter(getActivity(), R.layout.setting_item);
        settingList.setAdapter(adapt);

        adapt.add(getString(R.string.setting_mod_nick));
        adapt.add(getString(R.string.setting_mod_country));
        adapt.add(getString(R.string.setting_mod_age));
        adapt.add(getString(R.string.setting_mod_gender));
        //Add fields

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
                        //Change Username popup

                        final EditText nickEditText = (EditText) layout.findViewById(R.id.nickModText);
                        nickEditText.setTypeface(type);
                        nickEditText.setText(activity.usr);

                        Button btnNameConfirm = (Button) layout.findViewById(R.id.btn_mod_popup);
                        Button btnNameCanc = (Button) layout.findViewById(R.id.btn_canc_popup);

                        btnNameConfirm.setTypeface(type);
                        btnNameCanc.setTypeface(type);

                        btnNameConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                activity.usr = nickEditText.getText().toString();
                                usernamePopup.dismiss();
                                //Update username and dismiss
                            }
                        });

                        btnNameCanc.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                usernamePopup.dismiss();
                            }
                        });

                        break;

                    case 1: //Country selection

                        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        layout = inflater.inflate(R.layout.popup_countries,
                                (ViewGroup) getActivity().findViewById(R.id.country_popup_layout));

                        point = new Point();
                        getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);

                        win_width = (point.x) * 2 / 3;
                        win_height = (point.y) / 3;

                        final PopupWindow countryPopup = new PopupWindow(layout, win_width, win_height, true);
                        countryPopup.setBackgroundDrawable(new BitmapDrawable());
                        countryPopup.setAnimationStyle(R.style.SettingsPopup);
                        countryPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);
                        //countries popup

                        ListView countries = (ListView) layout.findViewById(R.id.list_countries);
                        CountryAdapter adapter = new CountryAdapter(getActivity(), R.layout.item_countries);
                        countries.setAdapter(adapter);

                        adapter.addAll(BlueCtrl.fetchFlags()); //populate list
                        adapter.notifyDataSetChanged();

                        countries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                //on item click store new country position and display flag

                                activity.flag = BlueCtrl.fetchFlag(i + 1).getPosition();
                                activity.country = i + 1;

                                adapt.notifyDataSetChanged();
                                countryPopup.dismiss();
                            }
                        });

                        break;

                    case 2: //Modify Birth date

                        try {

                            DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                    //dd/mm/yyyy pattern
                                    String Toset = "";
                                    if (dayOfMonth < 10)
                                        Toset = "0";
                                    Toset = Toset + dayOfMonth + "/";
                                    if (monthOfYear < 10)
                                        Toset = Toset + "0";
                                    Toset = Toset + (monthOfYear + 1) + "/" + year;

                                    activity.birth = Toset;
                                }
                            };

                            Date date;
                            if (activity.birth != null) date = (new SimpleDateFormat("dd mm yyyy")).parse(activity.birth.replace("/", " "));
                            else date = new Date(); //parse error, should not happen

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            int year = cal.get(Calendar.YEAR), month = cal.get(Calendar.MONTH), day = cal.get(Calendar.DAY_OF_MONTH);
                            DatePickerDialog dpDialog = new DatePickerDialog(getActivity(), R.style.DialogTheme, listener, year, month, day);
                            dpDialog.show();
                            //Set the DatePicker to previously set date

                            getActivity().findViewById(R.id.setting_layout).requestFocus();
                        }
                        catch (ParseException ignore) {};

                        break;
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
                        System.out.println("REQUEST TAKING PICTURE");
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

        imageNameFile = "PROFILE_IMG";
        File storageDir = getActivity().getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageNameFile,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        activity.mCurrentPhotoPath = image.getAbsolutePath();
        System.out.println("Path: " + activity.mCurrentPhotoPath);
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == getActivity().RESULT_OK) {

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();

            Bitmap bitmap = BitmapFactory.decodeFile(activity.mCurrentPhotoPath, bmOptions);

            Point point = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getRealSize(point);
            int width = point.x;
            float ratio = width / (float)bitmap.getWidth();

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.floor(bitmap.getWidth() * ratio), (int) Math.floor(bitmap.getHeight() * ratio),false);
            int height = (width * 9) / 16;
            bitmap = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() -  height) / 2, width, height);
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
        void onFragmentInteraction(Uri uri);
    }

}
