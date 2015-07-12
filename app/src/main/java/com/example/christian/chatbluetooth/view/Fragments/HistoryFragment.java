package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatMessage;
import com.example.christian.chatbluetooth.view.Adapters.HistoryAdapter;
import com.melnykov.fab.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements TextWatcher, View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RadioGroup historyGroup;
    EditText searchBar;
    ListView historyList;
    HistoryAdapter adapter;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HistoryFragment() {
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
        return inflater.inflate(R.layout.fragment_history, container, false);
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
        Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        SpannableString title = new SpannableString(getString(R.string.history_activity));
        title.setSpan(type, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(title);

        Button searchBtn = (Button) getActivity().findViewById(R.id.btnHistory); //get all messages Button
        searchBtn.setOnClickListener(this);

        searchBar = (EditText) getActivity().findViewById(R.id.etHistory);
        searchBar.clearFocus();
        searchBar.addTextChangedListener(this);
        //EditText form for key research; DB queried on the fly thanks to TextWatcher

        final CheckBox boxAll = (CheckBox) getActivity().findViewById(R.id.cboxAll);
        boxAll.setOnClickListener(this);
        //CheckBox for all messages selection

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fabHistory);
        fab.setOnClickListener(this);
        //Floating Action Button for message deleting

        historyList = (ListView) getActivity().findViewById(R.id.listHistory);
        adapter = new HistoryAdapter(getActivity(), R.layout.item_history);
        historyList.setAdapter(adapter);
        //List of messages

        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                boxAll.setChecked(false);
                //when selecting a message, set the All Messages CheckBox to unchecked

                ChatMessage message = adapter.getItem(i); //get element item
                message.setSentBy(!message.getSender()); //set sender to its negation; ChatMessage field sentBy is used to store CheckBox value
                adapter.notifyDataSetChanged();
            }
        });

        historyGroup = (RadioGroup) getActivity().findViewById(R.id.historyGroup);

        searchBtn.setTypeface(type);
        searchBar.setTypeface(type);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        adapter.clear(); //while writing, clear the adapter so that previous research does not appear
    }


    @Override
    public void afterTextChanged(Editable editable) {

        String str = editable.toString();
        //String in the EditText

        if (str.equals("")) return; //No empty research, use searchButton instead

        int mode; //RadioButton checked
        switch(historyGroup.getCheckedRadioButtonId()) {

            case R.id.in_message: //only messages

                mode = 0;
                break;

            case R.id.in_user: //search by username

                mode = 1;
                break;

            default: //both

                mode = 2;
                break;
        }

        adapter.addAll(BlueCtrl.fetchHistory(str, mode)); //add all messages found in DB
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

        ChatMessage message;

        switch (view.getId()) {

            case R.id.btnHistory:
                //search Button, fetch and display all messages

                adapter.clear();

                adapter.addAll(BlueCtrl.fetchHistory(null, 0)); //null String used as guide for method
                break;

            case R.id.fabHistory:
                //FloatingActionButton listener; deletes any messages with checked internal CheckBox

                int count = 0, max = historyList.getCount(); //initial number of elements

                 while (count < max) {

                     message = adapter.getItem(count); //get messages sequentially

                     if (message == null) {
                         //should not happen
                         count++;
                         continue;
                     }

                     if (message.getSender()) {

                         //sentBy field of ChatMessage instance is set to true to store CheckBox value,
                         //so it is selected for deleting
                         BlueCtrl.remove(message); //remove from database
                         adapter.remove(message); //remove from adapter
                         max--; //Adapter shrinks, max length got decremented
                     }
                     else {

                         //not selected for deleting
                         count++;
                     }
                }

                ((CheckBox)getActivity().findViewById(R.id.cboxAll)).setChecked(false);
                //set AllCheckBox to unchecked

                break;

            case R.id.cboxAll:
                //Select all messages shown

                for (int i = 0; i < adapter.getCount(); ++i) {

                    //For every message shown in the Adapter, mark sentBy field as CheckBox value
                    adapter.getItem(i).setSentBy(((CheckBox) view).isChecked());
                }
                break;
        }

        adapter.notifyDataSetChanged();
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
