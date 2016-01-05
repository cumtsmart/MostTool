package com.intel.most.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.intel.most.tools.handler.BaseHandler;
import com.intel.most.tools.mobibench.MobiActivity;
import com.intel.most.tools.utils.Constant;
import com.intel.most.tools.utils.UnzipAssets;

import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button mStart;
    private Button mStop;
    private Button mMost;
    private Button mFilter;
    private Button mGoto;

    private EditText mEditText;

    private boolean mBound;
    private ShellService mShellService;

    public static final int SHOW_DIALOG = 1;
    public static final int UPDATE_PROGRESS = 2;
    public static final int DISMISS_DIALOG = 3;

    private ProgressDialog progressDialog;

    private final DialogHandler mHandler = new DialogHandler(this);

    static class DialogHandler extends BaseHandler {
        public DialogHandler(MainActivity activity) {
            super(activity);
        }

        public void handleMessage(Message msg) {
            MainActivity activity = (MainActivity)getContext();
            if (activity != null) {
                Log.e("yangjun", "------handleMessage------");
                switch (msg.what) {
                    case SHOW_DIALOG:
                        activity.showDialog();
                        break;
                    case UPDATE_PROGRESS:
                        int progress = msg.arg1;
                        activity.undateProgress(progress);
                        break;
                    case DISMISS_DIALOG:
                        activity.dismissDialog();
                        break;
                }
            }
        }
    }

    public void showDialog() {
        Log.e("yangjun", "show");
        progressDialog.show();
        progressDialog.onStart();
        progressDialog.setProgress(0);
    }

    public void undateProgress(int progress) {
        Log.e("yangjun", "progress");
        progressDialog.setProgress(progress);
    }

    public void dismissDialog() {
        Log.e("yangjun", "dismiss");
        progressDialog.dismiss();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ShellService.LocalBinder binder = (ShellService.LocalBinder)iBinder;
            mShellService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mMost = (Button) findViewById(R.id.most);
        mFilter = (Button) findViewById(R.id.filter);
        mGoto = (Button) findViewById(R.id.to_graph);
        mEditText = (EditText) findViewById(R.id.pack_name);

        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mMost.setOnClickListener(this);
        mGoto.setOnClickListener(this);
        mFilter.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("progress");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);

        // Unzip mostbin
        String outputDirectory = getExternalFilesDirs(null)[0].getPath();
        Log.e("yangjun", "most bin path:" + outputDirectory);
        try {
            UnzipAssets.unZip(this, Constant.MOST_BIN, outputDirectory, true);
        } catch (IOException e) {
            Log.e("yangjun", "----- unzip exception -----");
            e.printStackTrace();
        }
    }

    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ShellService.class);
        Messenger messenger = new Messenger(mHandler);
        intent.putExtra("msg", messenger);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_mobi) {
            Intent intent = new Intent(this, MobiActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                handleStart(view);
                break;
            case R.id.stop:
                handleStop(view);
                break;
            case R.id.most:
                handleMost(view);
                break;
            case R.id.to_graph:
                Intent intent = new Intent(this, GraphActivity.class);
                startActivity(intent);
                break;
            case R.id.filter:
                handleFilter(view);
                break;
        }
    }

    private void handleStart(View view) {
        mStart.setEnabled(false);
        mStop.setEnabled(true);
        mMost.setEnabled(false);
        mFilter.setEnabled(false);

        if (!Shell.SU.available()) {
            Snackbar.make(view, "SU not available", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        mShellService.handleStart();
    }

    private void handleStop(View view) {
        mShellService.handleStop();
        mStart.setEnabled(true);
        mStop.setEnabled(false);
        mMost.setEnabled(true);
        mFilter.setEnabled(false);
    }

    private void handleMost(View view) {
        Log.e("yangjun", "handleMost");
        mStart.setEnabled(true);
        mStop.setEnabled(false);
        mMost.setEnabled(true);
        mShellService.handleMost();
        mFilter.setEnabled(true);
    }

    private void handleFilter(View view) {
        //TODO enable after EditText input text
        mStart.setEnabled(true);
        mStop.setEnabled(false);
        mMost.setEnabled(true);
        mFilter.setEnabled(true);
        String packName = mEditText.getText().toString().trim();
        mShellService.filterLog(packName);
    }
}
