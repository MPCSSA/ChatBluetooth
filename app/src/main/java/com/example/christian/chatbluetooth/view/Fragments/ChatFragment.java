package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.example.christian.chatbluetooth.model.ChatMessage;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.view.Activities.ChatActivity;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
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
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText msgText;
    private ChatUser user;


    public void setUser(ChatUser chatUser) {
        this.user = chatUser;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
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
        return inflater.inflate(R.layout.fragment_chat, container, false);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
        actionBar.setHomeAsUpIndicator(null);
        //ActionBar initialized

        user.notificationRead(); //all new messages displayed

        msgText = (EditText) getActivity().findViewById(R.id.etMsg); //Text Message form

        final ListView listView = (ListView) getActivity().findViewById(R.id.msgList); //Messages list
        listView.setAdapter(BlueCtrl.msgAdapt); //MessageAdapter
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //The only clickable item is the top item of the list, which loads older messages into the adapter

                if (i == 0 && adapterView.getAdapter().getItem(0) == null) {
                    //special null item is always on top

                    Cursor cursor = BlueCtrl.fetchMsgHistory(user.getMac(), ((ChatMessage) adapterView.getAdapter().getItem(1)).getDate().getTime());
                    int pos = 0;
                    //fetch old messages

                    if (cursor.getCount() > 0) {

                        ArrayList<ChatMessage> adapt = new ArrayList<>();

                        for (int _ = 1; _ < BlueCtrl.msgAdapt.getCount(); ++_) {

                            adapt.add(BlueCtrl.msgAdapt.getItem(_));
                        }
                        //newer messages have to stay in the bottom, therefore they will be popped and re-added

                        BlueCtrl.msgAdapt.clear(); //delete messages

                        BlueCtrl.msgAdapt.add(null); //insert special item on top

                        if (cursor.getCount() > 0) {
                            cursor.moveToLast();
                            do {
                                BlueCtrl.msgAdapt.add(new ChatMessage(cursor.getString(0), cursor.getInt(2) == 1, cursor.getLong(1), cursor.getInt(3) == 1));
                                ++pos;
                            } while (cursor.moveToPrevious());
                        }
                        //Add older messages

                        BlueCtrl.msgAdapt.addAll(adapt); //add newer messages
                    }
                    else BlueCtrl.msgAdapt.remove(null); //No more messages to show, delete special item

                    listView.setStackFromBottom(false);
                    listView.setSelection(pos);
                    /*
                    new messages always make the list scroll down; but when the user is searching for older messages,
                    it is implied he wants to keep reading from where it left. StackFromBottom option has to be
                    suspended until repositioning
                     */

                    BlueCtrl.msgAdapt.notifyDataSetChanged();

                    listView.setStackFromBottom(true);
                }
            }
        });

        ImageButton sendBtn = (ImageButton) getActivity().findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);
        //Send Message Button

        (getActivity().findViewById(R.id.emoBtn)).setOnClickListener(this); //Emoticon button

        FloatingActionButton favFab = (FloatingActionButton) getActivity().findViewById(R.id.fabFav);
        int heart = (user.isFav()) ? R.drawable.fav : R.drawable.unfav;
        favFab.setImageDrawable(getResources().getDrawable(heart));
        favFab.setOnClickListener(this);
        //Favorites Floating Action Button; initialized with DB information
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.sendBtn:
                //wrap up and send a Text Message

                String tmp = msgText.getText().toString(); //temporary message
                if (!tmp.equals("")) { //empty text is blocked

                    Date time = new Date(); //when it is sent

                    byte[] msg = (BlueCtrl.SPY == (byte)0) ? tmp.getBytes() : BlueCtrl.encrypt(tmp.getBytes());
                    //Spy Mode activated forces encrypting

                    BlueCtrl.sendMsg(user.getNextNode(),
                            BlueCtrl.buildMsg(user.getMacInBytes(),
                                    BlueCtrl.macToBytes(BluetoothAdapter.getDefaultAdapter().getAddress()),
                                    msg),
                            ((ChatActivity)getActivity()).getHandler());
                    //send the message

                    BlueCtrl.insertMsgTable(tmp, user.getMac(), time, 0, 0);
                    BlueCtrl.msgAdapt.add(new ChatMessage(tmp, false, time.getTime(), false));
                    BlueCtrl.msgAdapt.notifyDataSetChanged();
                    //Add the message to DB and MessageAdapter

                    msgText.setText(null);
                    msgText.requestFocus();
                    //clear message form
                }

                break;

            case R.id.emoBtn:
                //show emoticons

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.emoticon_popup, (ViewGroup) getActivity().findViewById(R.id.emoticons));

                GridView grid = (GridView) view.findViewById(R.id.emo_grid);
                grid.setAdapter(BlueCtrl.emoticons);
                BlueCtrl.emoticons.clear();
                //initialize emoticon GridList

                Bitmap emoBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.red_emoticons);
                emoBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);

                int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5;
                for (int y = 0; y < 5; ++y) {
                    for (int x = 0; x < 5; ++x) {

                        Bitmap single = Bitmap.createScaledBitmap(Bitmap.createBitmap(emoBitmap, x * w, y * h, w, h), w / 2, h / 2, false);
                        BlueCtrl.emoticons.add(single);
                    }
                }
                //populate GridList

                grid.setHorizontalSpacing(0);
                grid.setVerticalSpacing(0);

                final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setHeight((h / 2) * 5);
                popupWindow.setWidth((w / 2) * 5);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                popupWindow.setAnimationStyle(R.style.EmoticonAnim);
                popupWindow.setElevation(8f);
                //Emoticon PopupWindow; showed on top of message form

                popupWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.END, 36, msgText.getHeight() + 32);

                grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //On selecting one emoticon, it is automatically sent

                        Date time = new Date(); //when it was sent
                        BlueCtrl.sendMsg(user.getNextNode(),
                                BlueCtrl.buildEmoticon(user.getMacInBytes(),
                                        BlueCtrl.macToBytes(BluetoothAdapter.getDefaultAdapter().getAddress()),
                                        (byte) i), ((ChatActivity)getActivity()).getHandler());
                        //send Emoticon

                        BlueCtrl.insertMsgTable(String.valueOf(i), user.getMac(), time, 0, 1);
                        BlueCtrl.msgAdapt.add(new ChatMessage(String.valueOf(i), false, time.getTime(), true));
                        BlueCtrl.msgAdapt.notifyDataSetChanged();
                        //insert Emoticon message into DB and MessageAdapter

                        popupWindow.dismiss();
                        //close window
                    }
                });

                break;

            case R.id.fabFav:
                //FAV/UNFAV

                user.setFav(!user.isFav());
                int value = (user.isFav()) ? 1 : 0;
                BlueCtrl.setFavorite(user.getMac(), value);
                //set Favorites value in DB

                if (user.isFav()) BlueCtrl.favList.add(user);
                //new favorite
                else BlueCtrl.favList.remove(user);
                //un-faved

                int heart = (user.isFav()) ? R.drawable.fav : R.drawable.unfav;
                ((FloatingActionButton)v).setImageDrawable(getResources().getDrawable(heart));
                //Update Floating Action Button image
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
