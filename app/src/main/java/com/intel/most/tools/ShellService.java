package com.intel.most.tools;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

import com.intel.most.tools.utils.Constant;

public class ShellService extends Service {
    private Shell.Interactive mRootSession;
    private IBinder mBinder = new LocalBinder();
    private Messenger mMessenger;

    static int MOST = 0;
    static int FILTER = 1;


    @Override
    public IBinder onBind(Intent intent) {
        Log.e("yangjun", "====== onBind =======");
        mMessenger = intent.getParcelableExtra("msg");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("yangjun", "====== onUnbind =======");

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e("yangjun", "====== onDestroy =======");
        if (mRootSession != null && mRootSession.isRunning()) {
            handleStop();
            mRootSession.close();
            mRootSession.kill();
        }
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        ShellService getService() {
            return ShellService.this;
        }
    }

    public void handleStart() {
        final String traceCmd = Constant.CMD.CMD_TRACE + " -d " + Constant.PARTITION.FLASH +  " -o " + Constant.PATH.BLK_TRACE + " &";
        // Need run in background thread?
        if (mRootSession != null) {
            sendCommand(traceCmd);
        } else {
            mRootSession = new Shell.Builder()
                    .useSU()
                    .setWantSTDERR(true)
                    .open(new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                            if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
                                // open session failed
                                Log.e("yangjun", "open shell failed");
                            } else {
                                Log.e("yangjun", "shell is up, send request");
                                // execute our first request
                                sendCommand(traceCmd);
                            }
                        }
                    });
        }
    }

    private void sendCommand(String cmd) {
        Log.e("yangjun", "sendCommand:" + cmd);
        mRootSession.addCommand(cmd, 0, new Shell.OnCommandResultListener() {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                Log.e("yangjun", "command exitCode:" + exitCode);
                if (exitCode < 0) {
                    //TODO: this can happen, need handle it.
                } else {
                    // for(String msg: output) Log.e("yangjun", "outPut:" + msg);
                }
            }
        });

        mRootSession.waitForIdle();
    }

    public void handleStop() {
        if (mRootSession.isRunning()) {
            Log.e("yangjun", "------- kill blktrace-----");
            // Open another shell to kill blktrace
            String killCmd = "pgrep " + Constant.CMD.CMD_TRACE + " | xargs kill ";
            sendCommand(killCmd);
        }

    }

    public void handleMost() {
        String parseCmd = Constant.CMD.CMD_PARSE + " -q -i " + Constant.PATH.BLK_TRACE + ">" + Constant.PATH.BLK_PARSE;
        String mostCacheCmd = Constant.CMD.CMD_MOST + Constant.FILE_SYSTEM + Constant.PARTITION.PART_CACHE + Constant.PATH.BLK_PARSE + Constant.PATH.MOST_CACHE;
        String mostDataCmd = Constant.CMD.CMD_MOST + Constant.FILE_SYSTEM + Constant.PARTITION.PART_DATA + Constant.PATH.BLK_PARSE + Constant.PATH.MOST_DATA;
        String mostSystemCmd = Constant.CMD.CMD_MOST + Constant.FILE_SYSTEM + Constant.PARTITION.PART_SYSTEM + Constant.PATH.BLK_PARSE + Constant.PATH.MOST_SYSTEM;

        new MostTask(MOST).execute(parseCmd, mostCacheCmd, mostDataCmd, mostSystemCmd);
    }

    public void filterLog(String procKeyword) {
        Log.e("yangjun", "process keyword:" + procKeyword);

        File cacheFile = new File(getExternalFilesDir(null), Constant.PATH.FILTER_CACHE);
        File dataFile = new File(getExternalFilesDir(null), Constant.PATH.FILTER_DATA);
        File systemFile = new File(getExternalFilesDir(null), Constant.PATH.FILTER_SYSTEM);

        String filterCacheCmd = Constant.CMD.CMD_FILTER + Constant.PATH.MOST_CACHE + cacheFile.getAbsolutePath() + " " + procKeyword;
        String filterDataCmd = Constant.CMD.CMD_FILTER + Constant.PATH.MOST_DATA  + dataFile.getAbsolutePath() + " " + procKeyword;
        String filterSystemCmd = Constant.CMD.CMD_FILTER + Constant.PATH.MOST_SYSTEM + systemFile.getAbsolutePath() + " " + procKeyword;

        new MostTask(FILTER).execute(filterCacheCmd, filterDataCmd, filterSystemCmd);
    }

    private class MostTask extends AsyncTask<String, Integer, String> {
        private int cmdType;

        public MostTask(int type) {
            cmdType = type;
        }

        @Override
        protected void onPreExecute() {
            Log.e("yangjun", "onPreExecute");
            // UI thread
            Message msg = new Message();
            msg.what = MainActivity.SHOW_DIALOG;
            msg.arg1 = cmdType;

            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String[] cmd) {
            Log.e("yangjun", "doInBackground");
            int cmdCount = cmd.length;
            for (int i = 0; i < cmdCount; i++) {
                sendCommand(cmd[i]);
                int progress = (int) (((i+1)/(float)cmdCount) *100);
                publishProgress(progress);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "====== DONE =====";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.e("yangjun", "onProgressUpdate:" + values[0]);
            // UI thread
            Message msg = new Message();
            msg.arg1 = values[0];
            msg.what = MainActivity.UPDATE_PROGRESS;
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("yangjun", "onPostExecute");
            // UI thread
            Message msg = new Message();
            msg.what = MainActivity.DISMISS_DIALOG;
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.e("yangjun", result);
        }
    }
}