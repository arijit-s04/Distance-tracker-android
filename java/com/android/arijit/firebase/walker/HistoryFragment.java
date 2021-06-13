package com.android.arijit.firebase.walker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        FirebaseHelper.liveResultData.observe(this, this.resultDataListObserver);
    }

    private RecyclerView recyclerView;
    private ArrayList<ResultData> resultDataArrayList;
    private String TAG = "HistoryFragment";
    /**
     * data members
     */

    Observer<ArrayList<ResultData>> resultDataListObserver = new Observer<ArrayList<ResultData>>() {
        @Override
        public void onChanged(ArrayList<ResultData> resultData) {
            resultDataArrayList = resultData;
            if(recyclerView !=null && resultDataArrayList!= null){
                ResultDataAdapter mAdapter = new ResultDataAdapter(getContext(), resultDataArrayList);
                recyclerView.setAdapter(mAdapter);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        /**
         * init
         */
        resultDataArrayList = new ArrayList<>();
        recyclerView=root.findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseHelper.fetchData(root);

        return root;
    }

}