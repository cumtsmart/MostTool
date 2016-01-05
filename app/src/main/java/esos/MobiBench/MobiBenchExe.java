package esos.MobiBench;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.intel.most.tools.mobibench.DataItem;
import com.intel.most.tools.mobibench.ResultActivity;
import com.intel.most.tools.mobibench.StorageOptions;
import com.intel.most.tools.mobibench.fragment.MeasureFragment;
import com.intel.most.tools.mobibench.fragment.SettingFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MobiBenchExe extends Thread {

    // give value from JNI
    public float cpu_active = 0;
    public float cpu_idle = 0;
    public float cpu_iowait = 0;
    public int cs_total = 0;
    public int cs_voluntary = 0;
    public float throughput = 0;
    public float tps = 0;

    private List<DataItem> mData = new ArrayList<DataItem>();

    private Context mContext;
    private Handler mHandler;
    SharedPreferences sharedPref;

    private static int select_flag = 0;
    private String exe_path;
    private int exp_id;
    private Thread thread;
    private boolean runflag = false;

    public static String data_path = null; //getFilesDir()
    public static String sdcard_2nd_path = null;

    static {
        System.loadLibrary("mobibench");
    }

    native void mobibench_run(String str);
    native int getMobibenchProgress();
    native int getMobibenchState();

    public static String ExpName[] = {
            "Seq.Write",
            "Seq.read",
            "Rand.Write",
            "Rand.Read",
            "SQLite.Insert",
            "SQLite.Update",
            "SQLite.Delete"
    };

    public enum eAccessMode {
        WRITE,
        RANDOM_WRITE,
        READ,
        RANDOM_READ
    }

    public enum eDbMode {
        INSERT,
        UPDATE,
        DELETE
    }

    public enum eDbEnable {
        DB_DISABLE,
        DB_ENABLE
    }


    public MobiBenchExe() {

    }

    public MobiBenchExe(Context context, Handler handler) {
        mHandler = handler;
        mContext = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void setStoragePath(String path) {
        data_path = path;
        Log.e("yangjun", "data_path:" + data_path);
        sdcard_2nd_path = StorageOptions.determineStorageOptions();
    }

    public void setTestType(int type) {
        select_flag = type;
    }

    public void run() {
        int is_error = 0;
        mData.clear();
        switch(select_flag){
            case 0:// All
                RunFileIO();
                is_error = (getMobibenchState() == 4) ? 1:0;
                if(is_error != 0) {
                    break;
                }
                RunSqlite();
                break;
            case 1: // File I/O
                RunFileIO();
                is_error = (getMobibenchState() == 4) ? 1:0;
                if(is_error != 0) {
                    break;
                }
                break;
            case 2:
                RunSqlite();
                is_error = (getMobibenchState() == 4) ? 1:0;
                if(is_error != 0) {
                    break;
                }
                // Start Result Activity
                break;
            case 3:
                RunCustom();
                is_error = (getMobibenchState() == 4) ? 1:0;
                if(is_error != 0) {
                    break;
                }
                // Start Result Activity
                break;
        }
        deleteDir(exe_path);
        Message msg = Message.obtain(mHandler, 444, is_error, 0, null);
        mHandler.sendMessage(msg);
        // Start Result Activity
        ResultActivity.mData = mData;
        Intent intent = new Intent(mContext, ResultActivity.class);
        mContext.startActivity(intent);
    }

    private void deleteDir(String path) {
        Log.e("yangjun", "DeleteDir : " + path);
        File file = new File(path);
        File[] childFileList = file.listFiles();
        if (childFileList == null) {
            return;
        }
        for(File childFile : childFileList) {
            if(childFile.isDirectory()) {
                deleteDir(childFile.getAbsolutePath());
            }
            else {
                childFile.delete();
            }
        }
        file.delete();
    }

    public void RunFileIO() {
        int is_error = 0;
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT);
        is_error = (getMobibenchState()==4)?1:0;
        if(is_error != 0) {
            return;
        }
        RunMobibench(eAccessMode.READ, eDbEnable.DB_DISABLE, eDbMode.INSERT);
        is_error = (getMobibenchState()==4)?1:0;
        if(is_error != 0) {
            return;
        }
        RunMobibench(eAccessMode.RANDOM_WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT);
        is_error = (getMobibenchState()==4)?1:0;
        if(is_error != 0) {
            return;
        }
        RunMobibench(eAccessMode.RANDOM_READ, eDbEnable.DB_DISABLE, eDbMode.INSERT);
    }

    public void RunSqlite() {
        int is_error = 0;
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.INSERT);
        is_error = (getMobibenchState()==4)?1:0;
        if(is_error != 0) {
            return;
        }
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.UPDATE);
        is_error = (getMobibenchState()==4)?1:0;
        if(is_error != 0) {
            return;
        }
        RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.DELETE);
    }

    public void RunCustom() {
        int is_error = 0;
        if(sharedPref.getBoolean(SettingFragment.KEY_SEQ_WRITE, false)) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT);
            is_error = (getMobibenchState()==4)?1:0;
            if(is_error != 0) {
                return;
            }
        }
        if(sharedPref.getBoolean(SettingFragment.KEY_SEQ_READ, false)) {
            RunMobibench(eAccessMode.READ, eDbEnable.DB_DISABLE, eDbMode.INSERT);
            is_error = (getMobibenchState()==4)?1:0;
            if(is_error != 0) {
                return;
            }
        }
        if(sharedPref.getBoolean(SettingFragment.KEY_RAN_WRITE, false)) {
            RunMobibench(eAccessMode.RANDOM_WRITE, eDbEnable.DB_DISABLE, eDbMode.INSERT);
            is_error = (getMobibenchState()==4)?1:0;
            if(is_error != 0) {
                return;
            }
        }
        if(sharedPref.getBoolean(SettingFragment.KEY_RAN_READ, false)) {
            RunMobibench(eAccessMode.RANDOM_READ, eDbEnable.DB_DISABLE, eDbMode.INSERT);
            is_error = (getMobibenchState()==4)?1:0;
            if(is_error != 0) {
                return;
            }
        }
        if(sharedPref.getBoolean(SettingFragment.KEY_SQL_INSERT, false)) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.INSERT);
            is_error = (getMobibenchState()==4)?1:0;
            if(is_error != 0) {
                return;
            }
        }
        if(sharedPref.getBoolean(SettingFragment.KEY_SQL_UPDATE, false)) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.UPDATE);
            is_error = (getMobibenchState()==4)?1:0;
            if(is_error != 0) {
                return;
            }
        }
        if(sharedPref.getBoolean(SettingFragment.KEY_SQL_DELETE, false)) {
            RunMobibench(eAccessMode.WRITE, eDbEnable.DB_ENABLE, eDbMode.DELETE);
        }
    }

    private void RunMobibench(eAccessMode access_mode, eDbEnable db_enable, eDbMode db_mode) {
        int exp_id = 0;
        if(db_enable == eDbEnable.DB_DISABLE) {
            if(access_mode == eAccessMode.WRITE)
                exp_id = 0;
            else if(access_mode == eAccessMode.READ)
                exp_id = 1;
            else if(access_mode == eAccessMode.RANDOM_WRITE)
                exp_id = 2;
            else if(access_mode == eAccessMode.RANDOM_READ)
                exp_id = 3;
        } else {
            if(db_mode == eDbMode.INSERT)
                exp_id = 4;
            else if(db_mode == eDbMode.UPDATE)
                exp_id = 5;
            else
                exp_id = 6;
        }

        startThread(exp_id);

        String partition;
        // /data /sdcard /extSdCard
        String partValue = sharedPref.getString(SettingFragment.KEY_PARTITION, "");
        Log.e("yangjun", "------RunMobibench:" + partValue);
        if(partValue.equals("/data")) {
            partition = data_path;
        } else if (partValue.equals("/sdcard")) {
            partition = mContext.getExternalFilesDirs(null)[0].getAbsolutePath();
        } else {
            partition = sdcard_2nd_path; // TODO
        }

        String command = "mobibench";
        exe_path = partition + "/mobibench";
        command += " -p " + exe_path;

        if(db_enable == eDbEnable.DB_DISABLE) {
            if(access_mode == eAccessMode.WRITE || access_mode == eAccessMode.RANDOM_WRITE) {
                String fwSize = sharedPref.getString(SettingFragment.KEY_FW_SIZE, "10");
                command += " -f " + Integer.valueOf(fwSize)*1024;
            } else {
                String frSize = sharedPref.getString(SettingFragment.KEY_FR_SIZE, "32");
                command += " -f " + Integer.valueOf(frSize)*1024;
            }

            command += " -r " + sharedPref.getString(SettingFragment.KEY_IO_SIZE, "4");
            command += " -a " + access_mode.ordinal();
            // 0 1 2 3 4 5 6 7
            command += " -y " + sharedPref.getString(SettingFragment.KEY_SYNC_MODE, "4");
            command += " -t " + sharedPref.getString(SettingFragment.KEY_THREAD_NUM, "1");
        } else {
            command += " -d " + db_mode.ordinal();
            command += " -n " + sharedPref.getString(SettingFragment.KEY_SQL_TRANS, "100");
            command += " -j " + sharedPref.getString(SettingFragment.KEY_SQL_JOUR, "1");
            command += " -s " + sharedPref.getString(SettingFragment.KEY_SQL_MODE, "2");
        }

        Log.e("yangjun", "mobibench command : " + command);

        //native
        mobibench_run(command);

        joinThread();

        sendResult(exp_id);
    }

    public void sendResult(int result_id) {
        printResult();
        // set Activity Value
        DataItem dataItem = new DataItem();
        dataItem.exp_id = result_id;
        switch(select_flag){
            case 0:
                dataItem.result_type = "Test: All";
                break;
            case 1:
                dataItem.result_type = "Test: File IO";
                break;
            case 2:
                dataItem.result_type = "Test: SQLite";
                break;
            case 3:
                dataItem.result_type = "Test: My test";
                break;
        }

        dataItem.cpu_act = String.format("%.0f", cpu_active);
        dataItem.cpu_iow = String.format("%.0f", cpu_iowait);
        dataItem.cpu_idl = String.format("%.0f", cpu_idle);

        dataItem.cs_tot = "" + cs_total;
        dataItem.cs_vol = "" + cs_voluntary;

        if(result_id < 4) {	// File IO
            if(result_id < 2) { // Sequential
                dataItem.throughput = String.format("%.0f KB/s", throughput);
            } else { // Random
                dataItem.throughput = String.format("%.0f IOPS(%sKB)", throughput, sharedPref.getString(SettingFragment.KEY_IO_SIZE, "4"));
            }
        } else { // SQLite
            dataItem.db_tps = String.format("%.0f TPS", tps);
        }
        mData.add(dataItem);
    }

    private void printResult() {
        Log.e("yangjun", "mobibench cpu_active : "+ cpu_active);
        Log.e("yangjun", "mobibench cpu_idle : " + cpu_idle);
        Log.e("yangjun", "mobibench cpu_iowait : " + cpu_iowait);
        Log.e("yangjun", "mobibench cs_total : " + cs_total);
        Log.e("yangjun", "mobibench cs_voluntary : " + cs_voluntary);
        Log.e("yangjun", "mobibench throughput : " + throughput);
        Log.e("yangjun", "mobibench tps : " + tps);
    }

    public void startThread(int id) {
        exp_id = id;
        thread =  new ProgThread();
        thread.start();
    }

    public void joinThread() {
        runflag = false;
        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public class ProgThread extends Thread {

        public void run(){
            int prog = 0;
            int stat = 0;
            int old_prog = 0;
            int old_stat = -1;

            Message msg = Message.obtain(mHandler, 0);
            mHandler.sendMessage(msg);

            runflag = true;
            while(runflag) {
                prog = getMobibenchProgress();
                stat = getMobibenchState();
				/*
				 * state
				 * 0 : NONE
				 * 1 : READY
				 * 2 : EXE
				 * 3 : END
				 */
                if(prog > old_prog || prog == 0 || old_stat != stat) {
                    msg = Message.obtain(mHandler, prog);
                    mHandler.sendMessage(msg);
                    old_prog = prog;
                }

                if(stat < 2) {
                    msg = Message.obtain(mHandler, 999, 0, 0, "Initializing for "+ExpName[exp_id]);
                } else {
                    msg = Message.obtain(mHandler, 999, 0, 0, "Executing "+ExpName[exp_id]);
                }
                mHandler.sendMessage(msg);
                old_stat = stat;

                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (stat == 4) {
                msg = Message.obtain(mHandler, 0);
                mHandler.sendMessage(msg);

                msg = Message.obtain(mHandler, 999, 0, 0, ExpName[exp_id]+" exited with error");
                mHandler.sendMessage(msg);
            } else {
                msg = Message.obtain(mHandler, 100);
                mHandler.sendMessage(msg);

                msg = Message.obtain(mHandler, 999, 0, 0, ExpName[exp_id]+" done");
                mHandler.sendMessage(msg);

            }
        }
    }
}
