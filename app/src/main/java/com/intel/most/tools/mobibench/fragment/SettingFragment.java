package com.intel.most.tools.mobibench.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.intel.most.tools.R;
import com.intel.most.tools.mobibench.StorageOptions;

import esos.MobiBench.MobiBenchExe;

public class SettingFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    public final static String KEY_PARTITION="partition_n";
    public final static String KEY_THREAD_NUM="pref_thd_n";

    public final static String KEY_RAN_READ="ran_read";
    public final static String KEY_RAN_WRITE="ran_write";
    public final static String KEY_SEQ_READ="seq_read";
    public final static String KEY_SEQ_WRITE="seq_write";

    public final static String KEY_SQL_INSERT="sql_insert";
    public final static String KEY_SQL_DELETE="sql_delete";
    public final static String KEY_SQL_UPDATE="sql_update";

    public final static String KEY_FR_SIZE="fr_size";
    public final static String KEY_FW_SIZE="fw_size";
    public final static String KEY_IO_SIZE="io_size";

    public final static String KEY_SYNC_MODE="syc_mode";

    public final static String KEY_SQL_TRANS="sql_trans";
    public final static String KEY_SQL_MODE="sql_mode";
    public final static String KEY_SQL_JOUR="sql_jour";

    public static long freeSpace = 0;
    private static String freeFormat = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.setting);
    }

    @Override
    public void onResume() {
        super.onResume();
        initSummary();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initSummary() {
        //------- how to read
        Log.e("mobi", "----------initSummary--------");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference preference2 = (ListPreference)findPreference(KEY_PARTITION);
        if(StorageOptions.b_2nd_sdcard) {
            preference2.setEntries(R.array.partition);
            preference2.setEntryValues(R.array.partition_value);
        } else {
            preference2.setEntries(R.array.partition2);
            preference2.setEntryValues(R.array.partition2_value);
        }
        String frValue = sharedPref.getString(KEY_PARTITION, "");
        preference2.setSummary("Current test partition:" + frValue);
        //
        calculateFreeSpace(frValue);

        Preference preference = findPreference(KEY_THREAD_NUM);
        frValue = sharedPref.getString(KEY_THREAD_NUM, "");
        preference.setSummary("Current test thread number:" + frValue + "(1~100)");

        preference = findPreference(KEY_FR_SIZE);
        frValue = sharedPref.getString(KEY_FR_SIZE, "");
        preference.setSummary("Current test file size for read:" + frValue + "MB" + " (" + freeFormat +  " free)");

        preference = findPreference(KEY_FW_SIZE);
        frValue = sharedPref.getString(KEY_FW_SIZE, "");
        preference.setSummary("Current test file size for write:" + frValue + "MB" + " (" + freeFormat +  " free)");

        preference = findPreference(KEY_IO_SIZE);
        frValue = sharedPref.getString(KEY_IO_SIZE, "");
        preference.setSummary("Current test IO size:" + frValue + "KB");

        Resources res = getResources();
        preference = findPreference(KEY_SYNC_MODE);
        frValue = sharedPref.getString(KEY_SYNC_MODE, "");
        //
        String[] syncEntry = res.getStringArray(R.array.file_sync_entries);
        String syncSum = syncEntry[Integer.valueOf(frValue)];
        preference.setSummary("Current file sync mode:" + syncSum);

        preference = findPreference(KEY_SQL_TRANS);
        frValue = sharedPref.getString(KEY_SQL_TRANS, "");
        preference.setSummary("Current SQLite transaction number:" + frValue + "(1~10000)");

        preference = findPreference(KEY_SQL_MODE);
        frValue = sharedPref.getString(KEY_SQL_MODE, "");
        //
        String[] sqlEntry = res.getStringArray(R.array.file_sql_entries);
        String sqlSum = sqlEntry[Integer.valueOf(frValue)];
        preference.setSummary("Current file sql mode:" + sqlSum);

        preference = findPreference(KEY_SQL_JOUR);
        frValue = sharedPref.getString(KEY_SQL_JOUR, "");
        //
        String[] jourEntry = res.getStringArray(R.array.file_journal_entries);
        String jourSum = jourEntry[Integer.valueOf(frValue)];
        preference.setSummary("Current file journal mode:" + jourSum);
    }

    private void calculateFreeSpace(String path) {
        String target_path = null;
        if (path.equals("/data")) {
            target_path = Environment.getDataDirectory().getPath();
            Log.e("yangjun", "----- /data -----");
        } else if (path.equals("/sdcard")) {
            target_path = getActivity().getExternalFilesDirs(null)[0].getAbsolutePath();
            Log.e("yangjun", "----- /sdcard -----");
        } else if (path.equals("/extSdCard")) {
            target_path = MobiBenchExe.sdcard_2nd_path;
        }
        freeSpace = StorageOptions.getAvailableSize(target_path);
        Log.e("yangjun", "freeSpace:" + freeSpace);
        freeFormat = StorageOptions.formatSize(freeSpace);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference connectionPref = findPreference(key);
        // if preferences is CheckBoxPreference, then return
        if (key.equals(KEY_RAN_READ) || key.equals(KEY_RAN_WRITE)
                || key.equals(KEY_SEQ_READ)|| key.equals(KEY_SEQ_WRITE)
                || key.equals(KEY_SQL_INSERT)|| key.equals(KEY_SQL_DELETE)
                || key.equals(KEY_SQL_UPDATE)) {
            return;
        }

        String value = sharedPreferences.getString(key, "");
        Resources res = getResources();
        String[] syncEntry = res.getStringArray(R.array.file_sync_entries);
        String[] sqlEntry = res.getStringArray(R.array.file_sql_entries);
        String[] jourEntry = res.getStringArray(R.array.file_journal_entries);

        if (key.equals(KEY_PARTITION)) {
            calculateFreeSpace(value);
            connectionPref.setSummary("Current test partition:" + value);
            //
            Preference frPref = findPreference(KEY_FR_SIZE);
            Preference fwPref = findPreference(KEY_FW_SIZE);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String frValue = sharedPref.getString(KEY_FR_SIZE, "");
            String fwValue = sharedPref.getString(KEY_FW_SIZE, "");
            frPref.setSummary("Current test file size for read:" + frValue + "MB" + " (" + freeFormat +  " free)");
            fwPref.setSummary("Current test file size for write:" + fwValue + "MB" + " (" + freeFormat +  " free)");
        } else if (key.equals(KEY_THREAD_NUM)) {
            connectionPref.setSummary("Current test thread number:" + value + "(1~100)");
        } else if (key.equals(KEY_FR_SIZE)) {
            connectionPref.setSummary("Current test file size for read:" + value + "MB" + " (" + freeFormat +  " free)");
        } else if (key.equals(KEY_FW_SIZE)) {
            connectionPref.setSummary("Current test file size for write:" + value + "MB" + " (" + freeFormat +  " free)");
        } else if (key.equals(KEY_IO_SIZE)) {
            connectionPref.setSummary("Current test IO size:" + value + "KB");
        } else if (key.equals(KEY_SYNC_MODE)) {
            String syncSum = syncEntry[Integer.valueOf(value)];
            connectionPref.setSummary("Current file sync mode:" + syncSum);
        } else if (key.equals(KEY_SQL_TRANS)) {
            connectionPref.setSummary("Current SQLite transaction number:" + value + "(1~10000)");
        } else if (key.equals(KEY_SQL_MODE)) {
            String sqlSum = sqlEntry[Integer.valueOf(value)];
            connectionPref.setSummary("Current file sql mode:" + sqlSum);
        } else if (key.equals(KEY_SQL_JOUR)) {
            String jourSum = jourEntry[Integer.valueOf(value)];
            connectionPref.setSummary("Current file journal mode:" + jourSum);
        }
        Log.e("mobi", "--------- onSharedPreferenceChanged --------");
    }
}
