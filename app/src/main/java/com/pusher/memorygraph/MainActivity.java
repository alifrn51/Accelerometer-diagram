package com.pusher.memorygraph;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LineChart mChart;

    private Pusher pusher;

    private static final String PUSHER_APP_KEY = "PUSHER_APP_KEY";
    private static final String PUSHER_APP_CLUSTER = "PUSHER_APP_CLUSTER";
    private static final String CHANNEL_NAME = "stats";
    private static final String EVENT_NAME = "new_memory_stat";


    private SensorManager sensorManager;
    private Sensor acceleration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_FASTEST);



        mChart = (LineChart) findViewById(R.id.chart);

        setupChart();
        setupAxes();
        setupData();
        setLegend();

        /*PusherOptions options = new PusherOptions();
        options.setCluster(PUSHER_APP_CLUSTER);
        pusher = new Pusher(PUSHER_APP_KEY, options);
        Channel channel = pusher.subscribe(CHANNEL_NAME);

        SubscriptionEventListener eventListener = new SubscriptionEventListener() {
            @Override
            public void onEvent(String channel, final String event, final String data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Received event with data: " + data);
                        Gson gson = new Gson();
                        Stat stat = gson.fromJson(data, Stat.class);
                        addEntry(stat);
                    }
                });
            }
        };

        channel.bind(EVENT_NAME, eventListener);
        pusher.connect();
*/
    }

    private void setupChart() {

        // disable description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // enable scaling
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        // set an alternative background color
        mChart.setBackgroundColor(Color.DKGRAY);

    }

    private void setupAxes() {

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Add a limit line
        LimitLine ll = new LimitLine(10, "Upper Limit");
        ll.setLineWidth(2f);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll.setTextSize(10f);
        ll.setTextColor(Color.WHITE);
        // reset all limit lines to avoid overlapping lines
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll);
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

    }

    private void setupData() {

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

    }

    private void setLegend() {

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(Color.WHITE);

    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Memory Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.VORDIPLOM_COLORS[0]);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        // To show values of each point
        set.setDrawValues(true);

        return set;
    }

    private void addEntry(SensorEvent sensorEvent) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), sensorEvent.values[0]), 0);

            // let the chart know it's data has changed
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();

            // limit the number of visible entries

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pusher.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        addEntry(sensorEvent);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
