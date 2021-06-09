package com.android.arijit.firebase.walker;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
        runCounter();
    }

    private int counter = 0;
    private boolean running = false;
    private TextView textHome;
    private Button btnStart, btnStop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        /**
         * init
         */
        getActivity().setTitle(R.string.title_history);

        textHome = root.findViewById(R.id.text_home);
        btnStart = root.findViewById(R.id.btn_start);
        btnStop = root.findViewById(R.id.btn_stop);
        /**
         * init end
         */

        btnStart.setOnClickListener(v -> {
            running = true;
            btnStart.setEnabled(false);
            startService();
        });
        btnStop.setOnClickListener(v -> {
            counter = 0;
            running = false;
            btnStart.setEnabled(true);
            stopService();
        });

        return root;
    }
    private void runCounter(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                final TextView tvHandler = textHome;
                tvHandler.setText(String.valueOf(counter));
                if(running)
                    counter++;
                handler.postDelayed(this, 1000);
            }
        });
    }
    public void startService(){
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        serviceIntent.putExtra("counter", counter);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        getActivity().stopService(serviceIntent);
    }
}