package com.intel.most.tools.mobibench.fragment;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.intel.most.tools.R;
import com.intel.most.tools.handler.BaseHandler;

import esos.MobiBench.MobiBenchExe;

public class MeasureFragment extends Fragment implements View.OnClickListener {

    private Button btnAll;
    private Button btnSqlite;
    private Button btnFile;
    private Button btnCustom;
    private ProgressBar progressBar;
    private TextView progressPer;
    private TextView progressTxt;
    private Context mContext;


    // only used to set value
    private MobiBenchExe mobiExe;
    // real start
    private MobiBenchExe mbThread;

    private boolean mFlag = false; // using App stop button

    private boolean mobiIsRunning = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg){
            if(msg.what <= 100) {
                progressBar.setProgress(msg.what);
                progressPer.setText(""+msg.what+"%");
            } else if(msg.what == 999) {
                progressTxt.setText((String)msg.obj);
            } else if(msg.what == 666) {
                mFlag = false;
            } else if(msg.what == 444) {
                if(msg.arg1 == 1) {
                    print_error(1);
                }
                try {
                    mbThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mobiIsRunning = false;
                Log.e("yangjun", "change mobiIsRunning to false");
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.measure_fragment, container, false);
        btnAll = (Button)view.findViewById(R.id.btn_all);
        btnSqlite = (Button)view.findViewById(R.id.btn_sql);
        btnFile = (Button)view.findViewById(R.id.btn_fio);
        btnCustom = (Button)view.findViewById(R.id.btn_ctm);
        progressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        progressTxt = (TextView)view.findViewById(R.id.progress_txt);
        progressPer = (TextView)view.findViewById(R.id.progress_per);
        btnAll.setOnClickListener(this);
        btnSqlite.setOnClickListener(this);
        btnFile.setOnClickListener(this);
        btnCustom.setOnClickListener(this);

        mContext = getActivity();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mobiExe == null) {
            mobiExe = new MobiBenchExe();
            // this will init sdcard_2nd_path
            mobiExe.setStoragePath(mContext.getFilesDir().toString());
            Log.e("yangjun", "files dir:" + mContext.getFilesDir().toString());
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_all:
                startMobibenchExe(0);
                break;
            case R.id.btn_fio:
                startMobibenchExe(1);
                break;
            case R.id.btn_sql:
                startMobibenchExe(2);
                break;
            case R.id.btn_ctm:
                startMobibenchExe(3);
                break;
        }
    }

    private void startMobibenchExe(int type) {
        if(mobiIsRunning){
            print_error(0);
        } else {
            mobiIsRunning = true;
            // TODO: checking space
            mobiExe.setTestType(type);
            print_exp(type);
            mbThread = new MobiBenchExe(mContext, mHandler);
            mbThread.start();
        }
    }

    public void print_exp(int flag) {
        switch(flag){
            case 0:
                Toast.makeText(mContext, "Start Benchmark : File, SQlite", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(mContext, "Start Benchmark : File", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(mContext, "Start Benchmark : SQlite", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(mContext, "Start Benchmark : Customized set", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(mContext, "Nothing selected. Check \"Setting tab\"", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void print_error(int type) {
        switch(type){
            case 0:
                Toast.makeText(mContext, "MobiBench working..", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(mContext, "Benchmark engin exited with error", Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(mContext, "The file size must be less than the free space.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
