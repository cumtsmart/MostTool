package com.intel.most.tools.mobibench;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.intel.most.tools.R;

import java.util.ArrayList;
import java.util.List;

import esos.MobiBench.MobiBenchExe;

public class ResultActivity extends ListActivity {

    private ListAdapter mAdapter;
    public static List<DataItem> mData = new ArrayList<DataItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ResultAdapter(this);
        setListAdapter(mAdapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    public void setData(List<DataItem> data) {
        mData = data;
    }

    class ViewHolder {
        ImageView typeImg;
        TextView  cpuText;
        TextView  ctxText;
        TextView titleText;
    }

    class ResultAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ResultAdapter(Context context) {
            mInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = new ViewHolder();
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.result_list_item,null, false);
                holder.typeImg = (ImageView)convertView.findViewById(R.id.type_img);
                holder.titleText = (TextView)convertView.findViewById(R.id.tx_title);
                holder.cpuText = (TextView)convertView.findViewById(R.id.tx_cpu);
                holder.ctxText = (TextView)convertView.findViewById(R.id.tx_ctx);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // set holder info
            DataItem item = mData.get(position);
            String title1 = MobiBenchExe.ExpName[item.exp_id] + ":" + item.throughput;
            String title2 = MobiBenchExe.ExpName[item.exp_id] + ":" + item.db_tps;
            switch (item.exp_id) {
                case 0:
                    holder.typeImg.setImageResource(R.drawable.icon_sw);
                    holder.titleText.setText(title1);
                    break;
                case 1:
                    holder.typeImg.setImageResource(R.drawable.icon_sr);
                    holder.titleText.setText(title1);
                    break;
                case 2:
                    holder.typeImg.setImageResource(R.drawable.icon_rw);
                    holder.titleText.setText(title1);
                    break;
                case 3:
                    holder.typeImg.setImageResource(R.drawable.icon_rr);
                    holder.titleText.setText(title1);
                    break;
                case 4:
                    holder.typeImg.setImageResource(R.drawable.icon_insert);
                    holder.titleText.setText(title2);
                    break;
                case 5:
                    holder.typeImg.setImageResource(R.drawable.icon_update);
                    holder.titleText.setText(title2);
                    break;
                case 6:
                    holder.typeImg.setImageResource(R.drawable.icon_delete);
                    holder.titleText.setText(title2);
                    break;
            }

            String cpu = "CPU:" + "Busy("+ item.cpu_act + ")" + "Iow(" + item.cpu_iow +")" + "Idle(" + item.cpu_idl + ")";
            String ctx = "Ctx sw:" + item.cs_tot + "(" + item.cs_vol + ")";
            holder.cpuText.setText(cpu);
            holder.ctxText.setText(ctx);
            return convertView;
        }
    }
}
