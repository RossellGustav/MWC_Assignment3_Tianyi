package com.example.stepappv4.ui.Day;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepappv4.StepAppOpenHelper;
import com.example.stepappv4.R;

public class DayFragment extends Fragment {

    public int todaySteps = 0;
    TextView numStepsTextView;
    AnyChartView anyChartView;

    Date cDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat newSdf = new SimpleDateFormat("dd.MM.yyyy");
    String current_time = sdf.format(cDate);

    public Map<String, Integer> stepsByDay = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_day, container, false);

        anyChartView = root.findViewById(R.id.dayBarChart);
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBarDay));

        Cartesian cartesian = createColumnChart();
        anyChartView.setBackgroundColor("#00000000");
        anyChartView.setChart(cartesian);

        return root;
    }

    public Cartesian createColumnChart() {
        stepsByDay = StepAppOpenHelper.loadStepsByDay(getContext());

        Map<String, Integer> graph_map = new TreeMap<>();

        for (int i = 0; i < 7; i++) {
            String day = sdf.format(new Date(cDate.getTime() - i * 24 * 60 * 60 * 1000));
            graph_map.put(day, 0);
        }

        if (stepsByDay != null) {
            for (String key : graph_map.keySet()) {
                if (stepsByDay.containsKey(key)) {
                    graph_map.put(key, stepsByDay.get(key));
                }
            }
        } else {
            Log.e("DayFragment", "stepsByDay is null! Unable to populate graph_map.");
        }

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<String,Integer> entry : graph_map.entrySet()) {
            try {
                String formattedDate = newSdf.format(sdf.parse(entry.getKey()));
                data.add(new ValueDataEntry(formattedDate, entry.getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Column column = cartesian.column(data);

        column.fill("#1EB980");
        column.stroke("#1EB980");

        column.tooltip()
                .titleFormat("day: {%X}")
                .format("{%Value} steps")
                .anchor(Anchor.RIGHT_BOTTOM);

        column.tooltip()
                .position(Position.RIGHT_TOP).offsetX(0d)
                .offsetY(5);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);

        int maxYValue = stepsByDay != null ? (stepsByDay.values().isEmpty() ? 100 : (int) Math.ceil(1.1 * stepsByDay.values().stream().max(Integer::compare).get())) : 100;
        cartesian.yScale().maximum(maxYValue);

        cartesian.yAxis(0).title("steps");
        cartesian.xAxis(0).title("day");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }

}
