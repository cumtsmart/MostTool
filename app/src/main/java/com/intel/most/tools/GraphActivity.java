package com.intel.most.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.intel.most.tools.data.GraphData;
import com.intel.most.tools.model.Partition;
import com.intel.most.tools.utils.ParseLogTools;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends Activity implements View.OnClickListener {
    private Button btnParse;
    private BarChart partBartChart;
    private PieChart ioPercentChart;

    private TextView cacheFiles;
    private TextView dataFiles;
    private TextView systemFiles;

    private TextView cacheTitle;
    private TextView dataTitle;
    private TextView systemTitle;

    private GraphData graphData;
    private ProgressDialog progressDialog;

    private List<String> mParts = new ArrayList<String>();
    private List<String> mPieParts = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        cacheFiles = (TextView)findViewById(R.id.cache_files);
        dataFiles = (TextView)findViewById(R.id.data_files);
        systemFiles = (TextView)findViewById(R.id.system_files);

        cacheTitle = (TextView)findViewById(R.id.cache_title);
        dataTitle = (TextView)findViewById(R.id.data_title);
        systemTitle = (TextView)findViewById(R.id.system_title);

        mParts.add("Cache");
        mParts.add("Data");
        mParts.add("System");

        mPieParts.add("RR");
        mPieParts.add("RW");
        mPieParts.add("SR");
        mPieParts.add("SW");


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

        ioPercentChart = (PieChart)findViewById(R.id.io_percent);
        ioPercentChart.setUsePercentValues(true);
        ioPercentChart.setDescription("");
        ioPercentChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        ioPercentChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        ioPercentChart.setRotationEnabled(true);
        ioPercentChart.setHighlightPerTapEnabled(true);

        Legend pieL = ioPercentChart.getLegend();
        pieL.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        pieL.setXEntrySpace(7f);
        pieL.setYEntrySpace(0f);
        pieL.setYOffset(0f);

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
            addPieData(graphData);
            addVisitFiles(graphData);
            progressDialog.dismiss();
            Log.e("yangjun", s);
        }
    }


    private void addVisitFiles(GraphData graphData) {
        List<Partition> partitions = graphData.getPartitions();
        StringBuffer result1 = new StringBuffer();
        int index = 1;
        for (String file : partitions.get(0).visitFiles) {
            if (file.length() > 0) {
                result1.append(index + ": /cache" + file + "\n");
                cacheFiles.setVisibility(View.VISIBLE);
                cacheTitle.setVisibility(View.VISIBLE);
                index++;
            }
        }
        cacheFiles.setText(result1);

        StringBuffer result2 = new StringBuffer();
        index = 1;
        for (String file : partitions.get(1).visitFiles) {
            if (file.length() > 0) {
                result2.append(index + ": /data" + file + "\n");
                dataFiles.setVisibility(View.VISIBLE);
                dataTitle.setVisibility(View.VISIBLE);
                index++;
            }
        }
        dataFiles.setText(result2);

        StringBuffer result3 = new StringBuffer();
        index = 1;
        for (String file : partitions.get(2).visitFiles) {
            if (file.length() > 0) {
                result3.append(index + ": /system" + file + "\n");
                systemFiles.setVisibility(View.VISIBLE);
                systemTitle.setVisibility(View.VISIBLE);
                index++;
            }
        }
        systemFiles.setText(result3);
    }

    private void addPieData(GraphData graphData) {
        PieData pieData = ioPercentChart.getData();
        if (pieData != null) {
            pieData.clearValues();
            Log.e("yangjun", "pid data is not null");
        } else {
            Log.e("yangjun", "pid data is null");
            pieData = new PieData();
            pieData.addXValue(mPieParts.get(0));
            pieData.addXValue(mPieParts.get(1));
            pieData.addXValue(mPieParts.get(2));
            pieData.addXValue(mPieParts.get(3));
        }


        List<Partition> partitions = graphData.getPartitions();
        int[] results = new int[4];
        results[0] = partitions.get(0).readRandom.size() + partitions.get(1).readRandom.size() + partitions.get(2).readRandom.size();
        results[1] = partitions.get(0).writeRandom.size() + partitions.get(1).writeRandom.size() + partitions.get(2).writeRandom.size();
        results[2] = partitions.get(0).readSequence.size() + partitions.get(1).readSequence.size() + partitions.get(2).readSequence.size();
        results[3] = partitions.get(0).writeSequence.size() + partitions.get(1).writeSequence.size() + partitions.get(2).writeSequence.size();

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < 4; i++) {
            if (results[i] > 0) {
                yVals.add(new Entry(results[i], i));
            }
        }

        PieDataSet dataSet = new PieDataSet(yVals, "IO type");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);
        pieData.addDataSet(dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        ioPercentChart.setData(pieData);
        ioPercentChart.notifyDataSetChanged();
        ioPercentChart.invalidate();
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

        barData.addDataSet(set1);
        barData.addDataSet(set2);

        partBartChart.notifyDataSetChanged();
        partBartChart.invalidate();
    }
}
