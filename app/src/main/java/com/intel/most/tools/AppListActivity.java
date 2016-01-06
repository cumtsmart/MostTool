package com.intel.most.tools;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class AppListActivity extends ListActivity {
    private AppAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new AppAdapter(this);
        setListAdapter(mAdapter);
        Log.e("yangjun", "num:" + mAdapter.getCount());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // launch the app
        LauncherActivityInfo appInfo = mAdapter.getItem(position);
        ComponentName appComponent = appInfo.getComponentName();
        Intent intent = new Intent(Intent.makeMainActivity(appComponent));
        startActivity(intent);
        // appInfo.getApplicationInfo().processName
        Intent result = new Intent();
        result.putExtra("processName", appInfo.getApplicationInfo().processName);
        setResult(Activity.RESULT_OK, result);
        finish();
    }


    class ViewHolder {
        ImageView icon;
        TextView lable;
    }

    class AppAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<LauncherActivityInfo> mData;

        public AppAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            LauncherApps launcherApps = (LauncherApps)getSystemService(Context.LAUNCHER_APPS_SERVICE);
            mData = launcherApps.getActivityList(null, android.os.Process.myUserHandle());
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        // I see these methods as a cleaner approach to accessing my list's data. Instead of directly
        // accessing my adapter object via something like myListData.get(position) i can simply call
        // the adapter like adapter.get(position).
        @Override
        public LauncherActivityInfo getItem(int i) {
            return mData.get(i);
        }

        // Usually I would use this method when I want to execute some task based on the unique ID of
        // an object in the list. This is especially useful when working with a database. The returned
        // id could be a reference to an object in the database which I then could perform different
        // operations on(update/delete/etc)
        // So instead of accessing the ID from the raw data object like myListData.get(position).getId()
        // you can use adapter.getItemId(position)
        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = new ViewHolder();
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.app_list_item,null, false);
                holder.icon = (ImageView)convertView.findViewById(R.id.icon);
                holder.lable = (TextView)convertView.findViewById(R.id.label);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            LauncherActivityInfo appInfo = mData.get(position);
            holder.icon.setImageDrawable(appInfo.getIcon(0));
            holder.lable.setText(appInfo.getLabel());
            return convertView;
        }
    }
}
