package com.intel.most.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.intel.most.tools.data.GraphData;
import com.intel.most.tools.model.Partition;
import com.intel.most.tools.utils.ParseLogTools;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends Activity implements View.OnClickListener {
    private Button btnParse;
    private BarChart partBartChart;
    private GraphData graphData;
    private ProgressDialog progressDialog;

    private List<String> mParts = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        mParts.add("Cache");
        mParts.add("Data");
        mParts.add("System");

        graphData = new GraphData();
        btnParse = (Button)findViewById(R.id.bt_parse);
        btnParse.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        partBartChart = (BarChart)findViewById(R.id.part_bar_chart);
        partBartChart.setDrawGridBackground(false);
        partBartChart.setDescription("");
        // I don't want handle touch event
        partBartChart.setTouchEnabled(false);

        Legend l = partBartChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        l.setYOffset(0f);
        l.setYEntrySpace(0f);
        l.setTextSize(8f);

        XAxis xl = partBartChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = partBartChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(30f);
        leftAxis.setAxisMinValue(0);
        leftAxis.setValueFormatter(new LargeValueFormatter());
        partBartChart.getAxisRight().setEnabled(false);

        BarData barData = new BarData();
        barData.addXValue(mParts.get(0));
        barData.addXValue(mParts.get(1));
        barData.addXValue(mParts.get(2));
        partBartChart.setData(barData);
        partBartChart.invalidate();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_parse:
                handleParse();
                break;

        }
    }


    private void handleParse() {
        Log.e("yangjun", "------ addDataSet ------");
        new ParseGraphTask().execute();
    }

    class ParseGraphTask extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Parse Graph Data");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            graphData.addPartition(ParseLogTools.parseCache(GraphActivity.this));
            graphData.addPartition(ParseLogTools.parseData(GraphActivity.this));
            graphData.addPartition(ParseLogTools.parseSystem(GraphActivity.this));
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "------- doInBackground success -------";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            addDataSet(graphData);
            progressDialog.dismiss();
            Log.e("yangjun", s);
        }
    }


    private void addDataSet(GraphData graphData) {
        Log.e("yangjun", "----- addDataSet ----");
        BarData barData = partBartChart.getData();
        if (barData != null) {
            Log.e("yangjun", "----- clear values ----");
            barData.clearValues();
        }

        List<Partition> partitions = graphData.getPartitions();
        // READ
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        for (int i = 0; i < partitions.size(); i++) {
            yVals1.add(new BarEntry(partitions.get(i).readLogs.size(), i));
        }
        // WRITE
        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
        for (int i = 0; i < partitions.size(); i++) {
            yVals2.add(new BarEntry(partitions.get(i).writeLogs.size(), i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "READ");
        set1.setColor(Color.rgb(104, 241, 175));
        set1.setValueFormatter(new LargeValueFormatter());
        BarDataSet set2 = new BarDataSet(yVals2, "WRITE");
        set2.setColor(Color.rgb(164, 228, 251));
        set2.setValueFormatter(new LargeValueFormatter());

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);

        barData.addDataSet(set1);
        barData.addDataSet(set2);

        partBartChart.notifyDataSetChanged();
        partBartChart.invalidate();
    }
}
