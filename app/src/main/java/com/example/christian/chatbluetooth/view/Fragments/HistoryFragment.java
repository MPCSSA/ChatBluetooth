package com.example.christian.chatbluetooth.view.Fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatMessage;
import com.example.christian.chatbluetooth.view.Adapters.HistoryAdapter;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements TextWatcher, View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RadioGroup historyGroup;
    EditText searchBar;
    ListView historyList;
    HistoryAdapter adapter;
    HashMap<Integer, Integer> selected = new HashMap<>();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        actionBar.setTitle(getString(R.string.history_activity));

        historyList = (ListView) getActivity().findViewById(R.id.listHistory);
        adapter = new HistoryAdapter(getActivity(), R.layout.item_history);
        historyList.setAdapter(adapter);
        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                int position = historyList.getPositionForView(view);
                if (selected.containsValue(position)) selected.remove(position);
                else selected.put(position, position);
            }
        });

        historyGroup = (RadioGroup) getActivity().findViewById(R.id.historyGroup);

        Button searchBtn = (Button) getActivity().findViewById(R.id.btnHistory);
        searchBtn.setOnClickListener(this);

        searchBar = (EditText) getActivity().findViewById(R.id.etHistory);
        searchBar.addTextChangedListener(this);

        CheckBox boxAll = (CheckBox) getActivity().findViewById(R.id.cboxAll);
        boxAll.setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fabHistory);
        fab.setOnClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        adapter.clear();
    }


    @Override
    public void afterTextChanged(Editable editable) {

        String str = editable.toString();

        if (str.equals("")) return;

        int mode;
        switch(historyGroup.getCheckedRadioButtonId()) {

            case R.id.in_message:

                mode = 0;
                break;

            case R.id.in_user:

                mode = 1;
                break;

            default:

                mode = 2;
                break;
        }

        adapter.addAll(BlueCtrl.fetchHistory(str, mode));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

        ChatMessage message;

        switch (view.getId()) {

            case R.id.btnHistory:

                adapter.clear();

                adapter.addAll(BlueCtrl.fetchHistory(null, 0));
                break;

            case R.id.fabHistory:

                int count = 0, max = historyList.getCount();

                 while (count < max) {

                     System.out.println("FANCULO");

                     View v = historyList.getChildAt(count);
                     if (v == null) {
                         System.out.println("NULL");
                         count++;
                         continue;
                     }
                     CheckBox check = ((CheckBox)v.findViewById(R.id.cboxSelect));
                     if (check.isChecked()) {
                         System.out.println("REMOVED");
                         adapter.remove(historyList.getPositionForView(v));
                         max--;
                     }
                    else {
                         System.out.println("SKIPPED");
                         count++;
                     }
                }

                break;

            case R.id.cboxAll:

                boolean bool = ((CheckBox)view).isChecked();

                for (int p = 0; p < historyList.getCount(); ++p) {

                    selected.put(p, p);
                    ((CheckBox)adapter.getView(p, null, historyList).findViewById(R.id.cboxSelect)).setChecked(bool);
                    //sets all checkboxes to the value of the checkbox selector
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
