package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.controller.MessageThread;
import com.example.christian.chatbluetooth.model.ChatMessage;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.view.Activities.ChatActivity;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText msgText;
    private ChatUser user;
    private Handler handler;


    public void setUser(ChatUser chatUser) {
        this.user = chatUser;
    }

    /*public void setMac(byte [] mac){
        this.mac = mac;
    }

    public void setAddress(String address){
        this.user = address;
    }

    public void setDevice(BluetoothDevice dvc){
        this.dvc = dvc;
    }*/

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment() {
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
        int layout = (BlueCtrl.version) ? R.layout.fragment_chat : R.layout.fragment_chat_nomat;
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


    @Override
    public void onActivityCreated(Bundle savedIstanceState) {
        super.onActivityCreated(savedIstanceState);

        ActionBar actionBar = getActivity().getActionBar();
        ((ChatActivity) getActivity()).setState(true);

        Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        SpannableString title = new SpannableString(user.getName());
        title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(title);
        if (BlueCtrl.version) actionBar.setHomeAsUpIndicator(null);

        msgText = (EditText) getActivity().findViewById(R.id.etMsg);

        ListView listView = (ListView) getActivity().findViewById(R.id.msgList);
        listView.setAdapter(BlueCtrl.msgAdapt);

        ImageButton sendBtn = (ImageButton) getActivity().findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);

        (getActivity().findViewById(R.id.emoBtn)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.sendBtn:

                String msg = msgText.getText().toString();
                if (!msg.equals("")) {
                    Date time = new Date();
                    BlueCtrl.sendMsg(user.getNextNode(),
                            BlueCtrl.buildMsg(user.getMacInBytes(),
                                    BlueCtrl.macToBytes(BluetoothAdapter.getDefaultAdapter().getAddress()),
                                    msg.getBytes()),
                            handler);

                    BlueCtrl.insertMsgTable(msg, user.getMac(), time, 0);
                    BlueCtrl.msgAdapt.add(new ChatMessage(msg, false, time.getTime(), false));
                    BlueCtrl.msgAdapt.notifyDataSetChanged();
                    msgText.setText(null);
                    msgText.requestFocus();

                }
                break;

            case R.id.emoBtn:

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                int layout = (BlueCtrl.version) ? R.layout.emoticon_layout : R.layout.emoticon_layout_nomat;
                View view = inflater.inflate(layout, (ViewGroup) getActivity().findViewById(R.id.emoticons));

                GridView grid = (GridView) view.findViewById(R.id.emo_grid);
                grid.setAdapter(BlueCtrl.emoticons);
                BlueCtrl.emoticons.clear();

                Bitmap emoBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.red_emoticons);
                emoBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

                int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5;
                for (int y = 0; y < 5; ++y) {
                    for (int x = 0; x < 5; ++x) {

                        Bitmap single = Bitmap.createScaledBitmap(Bitmap.createBitmap(emoBitmap, x * w, y * h, w, h), w / 2, h / 2, false);
                        BlueCtrl.emoticons.add(single);

                    }
                }

                grid.setHorizontalSpacing(0);
                grid.setVerticalSpacing(0);

                final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setHeight((h / 2) * 5);
                popupWindow.setWidth((w / 2) * 5);
                popupWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.END, 16, 80);
                if (BlueCtrl.version) popupWindow.setElevation(8f);

                grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        System.out.println("HEYOH!");
                        Date time = new Date();
                        BlueCtrl.sendMsg(user.getNextNode(),
                                BlueCtrl.buildEmoticon(user.getMacInBytes(),
                                        BlueCtrl.macToBytes(BluetoothAdapter.getDefaultAdapter().getAddress()),
                                        (byte) i), handler);

                        BlueCtrl.insertMsgTable(String.valueOf(i), user.getMac(), time, 1);
                        BlueCtrl.msgAdapt.add(new ChatMessage(String.valueOf(i), false, time.getTime(), true));
                        BlueCtrl.msgAdapt.notifyDataSetChanged();

                        popupWindow.dismiss();
                    }
                });
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
