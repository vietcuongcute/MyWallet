package com.example.mywallet;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
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

    TextView tvStatisticsTitle, tvTotalTitle;
    TextView tabExpense, tabIncome, tabBudget;

    DonutChartView donutChart;
    LinearLayout layoutLegend;

    DatabaseHelper databaseHelper;
    DecimalFormat decimalFormat;

    int selectedTab = 0;

    int[] chartColors = {
            Color.parseColor("#2ECC71"),
            Color.parseColor("#A855F7"),
            Color.parseColor("#F97316"),
            Color.parseColor("#EF4444"),
            Color.parseColor("#3B82F6"),
            Color.parseColor("#14B8A6"),
            Color.parseColor("#FACC15"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#6366F1"),
            Color.parseColor("#84CC16")
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

        tvStatisticsTitle = view.findViewById(R.id.tvStatisticsTitle);
        tvTotalTitle = view.findViewById(R.id.tvTotalTitle);

        tabExpense = view.findViewById(R.id.tabExpense);
        tabIncome = view.findViewById(R.id.tabIncome);
        tabBudget = view.findViewById(R.id.tabBudget);

        donutChart = view.findViewById(R.id.donutChart);
        layoutLegend = view.findViewById(R.id.layoutLegend);

        databaseHelper = new DatabaseHelper(requireContext());
        decimalFormat = new DecimalFormat("#,###");

        setupTabs();
        loadStatistics();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void setupTabs() {
        tabExpense.setOnClickListener(v -> {
            selectedTab = 0;
            loadStatistics();
        });

        tabIncome.setOnClickListener(v -> {
            selectedTab = 1;
            loadStatistics();
        });

        tabBudget.setOnClickListener(v -> {
            selectedTab = 2;
            loadStatistics();
        });
    }

    private void loadStatistics() {
        if (databaseHelper == null) {
            return;
        }

        updateTabStyle();

        if (selectedTab == 0) {
            loadExpenseStatistics();
        } else if (selectedTab == 1) {
            loadIncomeStatistics();
        } else {
            loadBudgetStatistics();
        }
    }

    private void loadExpenseStatistics() {
        tvStatisticsTitle.setText("Thống Kê Chi Tiêu");

        double total = databaseHelper.getRealExpenseTotalForStatistics();
        tvTotalTitle.setText("Tổng chi: " + decimalFormat.format(total) + "đ");

        ArrayList<String> rawList = databaseHelper.getExpenseByCategoryForStatistics();

        showChart(rawList, total, "Chi tiêu", "Chưa có dữ liệu chi tiêu");
    }

    private void loadIncomeStatistics() {
        tvStatisticsTitle.setText("Thống Kê Thu Nhập");

        double total = databaseHelper.getRealIncomeTotalForStatistics();
        tvTotalTitle.setText("Tổng thu: " + decimalFormat.format(total) + "đ");

        ArrayList<String> rawList = databaseHelper.getIncomeByCategoryForStatistics();

        showChart(rawList, total, "Thu nhập", "Chưa có dữ liệu thu nhập");
    }

    private void loadBudgetStatistics() {
        tvStatisticsTitle.setText("Thống Kê Hũ");

        double total = databaseHelper.getBudgetHistoryTotalForStatistics();
        tvTotalTitle.setText("Tổng nạp/rút hũ: " + decimalFormat.format(total) + "đ");

        ArrayList<String> rawList = databaseHelper.getBudgetHistoryByActionForStatistics();

        showChart(rawList, total, "Hũ", "Chưa có dữ liệu nạp/rút hũ");
    }

    private void showChart(ArrayList<String> rawList, double total, String legendType, String emptyText) {
        ArrayList<DonutChartView.ChartItem> chartItems = new ArrayList<>();

        layoutLegend.removeAllViews();

        if (rawList == null || rawList.isEmpty() || total <= 0) {
            donutChart.setData(chartItems);
            addEmptyLegend(emptyText);
            return;
        }

        for (int i = 0; i < rawList.size(); i++) {
            String item = rawList.get(i);
            String[] parts = item.split("\\|");

            if (parts.length < 2) {
                continue;
            }

            String name = parts[0];
            double amount;

            try {
                amount = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                amount = 0;
            }

            int color = chartColors[i % chartColors.length];

            chartItems.add(new DonutChartView.ChartItem(name, amount, color));
            addLegendItem(name, amount, total, color, legendType);
        }

        donutChart.setData(chartItems);
    }

    private void updateTabStyle() {
        setTabSelected(tabExpense, selectedTab == 0);
        setTabSelected(tabIncome, selectedTab == 1);
        setTabSelected(tabBudget, selectedTab == 2);
    }

    private void setTabSelected(TextView tab, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(14));

        if (selected) {
            bg.setColor(getColorCompat(R.color.primary));
            tab.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.TRANSPARENT);
            tab.setTextColor(getColorCompat(R.color.text_secondary));
        }

        tab.setBackground(bg);
    }

    private void addLegendItem(String name, double amount, double total, int color, String legendType) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);

        TextView colorBox = new TextView(requireContext());
        colorBox.setBackgroundColor(color);

        TextView tvName = new TextView(requireContext());
        tvName.setText(name + "  " + legendType);
        tvName.setTextColor(Color.parseColor("#111827"));
        tvName.setTextSize(14);
        tvName.setPadding(dpToPx(10), 0, 0, 0);

        TextView tvAmount = new TextView(requireContext());
        tvAmount.setText(decimalFormat.format(amount) + "đ");
        tvAmount.setTextColor(Color.parseColor("#6B7280"));
        tvAmount.setTextSize(14);
        tvAmount.setGravity(Gravity.END);

        int percent = 0;

        if (total > 0) {
            percent = (int) Math.round((amount / total) * 100);
        }

        TextView tvPercent = new TextView(requireContext());
        tvPercent.setText(percent + "%");
        tvPercent.setTextColor(Color.parseColor("#6B7280"));
        tvPercent.setTextSize(14);
        tvPercent.setGravity(Gravity.END);

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
                dpToPx(100),
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

    private void addEmptyLegend(String text) {
        TextView empty = new TextView(requireContext());
        empty.setText(text);
        empty.setTextColor(Color.parseColor("#6B7280"));
        empty.setTextSize(15);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 18, 0, 18);

        layoutLegend.addView(empty);
    }

    private int getColorCompat(int colorRes) {
        return requireContext().getColor(colorRes);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}