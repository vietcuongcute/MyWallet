package com.example.mywallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    TextView tvBalance, tvIncome, tvExpense, tvEmptyRecent;
    MaterialButton btnAddIncome, btnAddExpense;
    RecyclerView recyclerRecentTransactions;

    DatabaseHelper databaseHelper;
    DecimalFormat decimalFormat;

    ArrayList<TransactionModel> recentList;
    TransactionAdapter recentAdapter;
    TextView tvUserName;
    SharedPreferences sharedPreferences;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvBalance = view.findViewById(R.id.tvBalance);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvEmptyRecent = view.findViewById(R.id.tvEmptyRecent);

        btnAddIncome = view.findViewById(R.id.btnAddIncome);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);

        recyclerRecentTransactions = view.findViewById(R.id.recyclerRecentTransactions);
        recyclerRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecentTransactions.setNestedScrollingEnabled(false);

        databaseHelper = new DatabaseHelper(getContext());
        decimalFormat = new DecimalFormat("#,###");
        sharedPreferences = requireContext().getSharedPreferences("MyWalletPrefs", Context.MODE_PRIVATE);

        btnAddIncome.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
            intent.putExtra("type", "Thu nhập");
            startActivity(intent);
        });

        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
            intent.putExtra("type", "Chi tiêu");
            startActivity(intent);
        });

        loadSummary();
        loadRecentTransactions();

        loadUserName();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserName();
        loadSummary();
        loadRecentTransactions();
    }

    private void loadSummary() {
        if (databaseHelper == null || decimalFormat == null) {
            return;
        }

        double income = databaseHelper.getTotalIncome();
        double expense = databaseHelper.getTotalExpense();
        double balance = databaseHelper.getBalance();

        tvIncome.setText("+" + decimalFormat.format(income) + " đ");
        tvExpense.setText("-" + decimalFormat.format(expense) + " đ");
        tvBalance.setText(decimalFormat.format(balance) + " đ");
    }

    private void loadRecentTransactions() {
        if (databaseHelper == null) {
            return;
        }

        recentList = databaseHelper.getRecentTransactions();

        if (recentList.isEmpty()) {
            tvEmptyRecent.setVisibility(View.VISIBLE);
            recyclerRecentTransactions.setVisibility(View.GONE);
        } else {
            tvEmptyRecent.setVisibility(View.GONE);
            recyclerRecentTransactions.setVisibility(View.VISIBLE);

            recentAdapter = new TransactionAdapter(getContext(), recentList);
            recyclerRecentTransactions.setAdapter(recentAdapter);
        }
    }
    private void loadUserName() {
        if (sharedPreferences == null || tvUserName == null) {
            return;
        }

        String name = sharedPreferences.getString("user_name", "MyWallet User");
        tvUserName.setText(name);
    }
}