package com.example.mywallet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StatisticsFragment extends Fragment {

    TextView tvTotalExpense;
    DonutChartView donutChart;
    LinearLayout layoutLegend;

    DatabaseHelper databaseHelper;
    DecimalFormat decimalFormat;

    int[] chartColors = {
            Color.parseColor("#2ECC71"),
            Color.parseColor("#A855F7"),
            Color.parseColor("#F97316"),
            Color.parseColor("#EF4444"),
            Color.parseColor("#3B82F6"),
            Color.parseColor("#14B8A6"),
            Color.parseColor("#FACC15"),
            Color.parseColor("#EC4899")
    };

    public StatisticsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        donutChart = view.findViewById(R.id.donutChart);
        layoutLegend = view.findViewById(R.id.layoutLegend);

        databaseHelper = new DatabaseHelper(requireContext());
        decimalFormat = new DecimalFormat("#,###");

        loadStatistics();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void loadStatistics() {
        if (databaseHelper == null) {
            return;
        }

        double totalExpense = databaseHelper.getTotalExpense();

        tvTotalExpense.setText("Tổng chi: " + decimalFormat.format(totalExpense) + "đ");

        ArrayList<String> rawList = databaseHelper.getExpenseByCategory();
        ArrayList<DonutChartView.ChartItem> chartItems = new ArrayList<>();

        layoutLegend.removeAllViews();

        if (rawList == null || rawList.isEmpty() || totalExpense <= 0) {
            donutChart.setData(chartItems);
            addEmptyLegend();
            return;
        }

        for (int i = 0; i < rawList.size(); i++) {
            String item = rawList.get(i);

            String[] parts = item.split("\\|");

            if (parts.length < 2) {
                continue;
            }

            String category = parts[0];
            double amount;

            try {
                amount = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                amount = 0;
            }

            int color = chartColors[i % chartColors.length];

            chartItems.add(new DonutChartView.ChartItem(category, amount, color));
            addLegendItem(category, amount, totalExpense, color);
        }

        donutChart.setData(chartItems);
    }

    private void addLegendItem(String category, double amount, double totalExpense, int color) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);

        TextView colorBox = new TextView(requireContext());
        colorBox.setWidth(dpToPx(14));
        colorBox.setHeight(dpToPx(14));
        colorBox.setBackgroundColor(color);

        TextView tvName = new TextView(requireContext());
        tvName.setText(category + "  Chi tiêu");
        tvName.setTextColor(Color.parseColor("#111827"));
        tvName.setTextSize(14);
        tvName.setPadding(dpToPx(10), 0, 0, 0);

        TextView tvAmount = new TextView(requireContext());
        tvAmount.setText(decimalFormat.format(amount) + "đ");
        tvAmount.setTextColor(Color.parseColor("#6B7280"));
        tvAmount.setTextSize(14);
        tvAmount.setGravity(android.view.Gravity.END);

        int percent = 0;

        if (totalExpense > 0) {
            percent = (int) Math.round((amount / totalExpense) * 100);
        }

        TextView tvPercent = new TextView(requireContext());
        tvPercent.setText(percent + "%");
        tvPercent.setTextColor(Color.parseColor("#6B7280"));
        tvPercent.setTextSize(14);
        tvPercent.setGravity(android.view.Gravity.END);

        LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(
                dpToPx(14),
                dpToPx(14)
        );

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );

        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                dpToPx(92),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams percentParams = new LinearLayout.LayoutParams(
                dpToPx(52),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        row.addView(colorBox, colorParams);
        row.addView(tvName, nameParams);
        row.addView(tvAmount, amountParams);
        row.addView(tvPercent, percentParams);

        layoutLegend.addView(row);
    }

    private void addEmptyLegend() {
        TextView empty = new TextView(requireContext());
        empty.setText("Chưa có dữ liệu chi tiêu để thống kê");
        empty.setTextColor(Color.parseColor("#6B7280"));
        empty.setTextSize(15);
        empty.setGravity(android.view.Gravity.CENTER);
        empty.setPadding(0, 18, 0, 18);

        layoutLegend.addView(empty);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}