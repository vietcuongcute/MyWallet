package com.example.mywallet;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TransactionFragment extends Fragment {

    RecyclerView recyclerTransactions;
    TextView tvEmpty;
    TextInputEditText edtSearchTransaction;
    MaterialButton btnFilterAll, btnFilterIncome, btnFilterExpense, btnFilterDate, btnClearDate;

    DatabaseHelper databaseHelper;
    ArrayList<TransactionModel> transactionList;
    TransactionAdapter adapter;

    String currentFilter = "Tất cả";
    String selectedDate = "";

    Calendar calendar;
    SimpleDateFormat dateFormat;

    public TransactionFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        recyclerTransactions = view.findViewById(R.id.recyclerTransactions);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        edtSearchTransaction = view.findViewById(R.id.edtSearchTransaction);

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterIncome = view.findViewById(R.id.btnFilterIncome);
        btnFilterExpense = view.findViewById(R.id.btnFilterExpense);
        btnFilterDate = view.findViewById(R.id.btnFilterDate);
        btnClearDate = view.findViewById(R.id.btnClearDate);

        databaseHelper = new DatabaseHelper(getContext());

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTransactions();
        setupSearch();
        setupFilterButtons();
        setupDateFilter();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (databaseHelper != null) {
            loadTransactions();
            applyFilterAndSearch();
        }
    }

    private void loadTransactions() {
        transactionList = databaseHelper.getAllTransactions();

        if (adapter == null) {
            adapter = new TransactionAdapter(getContext(), transactionList);
            recyclerTransactions.setAdapter(adapter);
        } else {
            adapter.updateList(transactionList);
        }

        updateEmptyState(transactionList, "Chưa có giao dịch nào");
    }

    private void setupSearch() {
        edtSearchTransaction.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFilterButtons() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "Tất cả";
            updateFilterButtonStyle();
            applyFilterAndSearch();
        });

        btnFilterIncome.setOnClickListener(v -> {
            currentFilter = "Thu nhập";
            updateFilterButtonStyle();
            applyFilterAndSearch();
        });

        btnFilterExpense.setOnClickListener(v -> {
            currentFilter = "Chi tiêu";
            updateFilterButtonStyle();
            applyFilterAndSearch();
        });

        updateFilterButtonStyle();
    }

    private void setupDateFilter() {
        btnFilterDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        selectedDate = dateFormat.format(calendar.getTime());
                        btnFilterDate.setText(selectedDate);
                        btnClearDate.setVisibility(View.VISIBLE);

                        applyFilterAndSearch();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });

        btnClearDate.setOnClickListener(v -> {
            selectedDate = "";
            btnFilterDate.setText("Chọn ngày");
            btnClearDate.setVisibility(View.GONE);

            applyFilterAndSearch();
        });
    }

    private void applyFilterAndSearch() {
        if (transactionList == null || adapter == null) {
            return;
        }

        String query = edtSearchTransaction.getText().toString().toLowerCase().trim();

        ArrayList<TransactionModel> filteredList = new ArrayList<>();

        for (TransactionModel transaction : transactionList) {
            boolean matchTypeFilter = currentFilter.equals("Tất cả")
                    || transaction.getType().equals(currentFilter);

            boolean matchDateFilter = selectedDate.isEmpty()
                    || transaction.getDate().equals(selectedDate);

            String category = transaction.getCategory().toLowerCase();
            String type = transaction.getType().toLowerCase();
            String date = transaction.getDate().toLowerCase();

            String note = "";
            if (transaction.getNote() != null) {
                note = transaction.getNote().toLowerCase();
            }

            boolean matchSearch = category.contains(query)
                    || type.contains(query)
                    || date.contains(query)
                    || note.contains(query);

            if (matchTypeFilter && matchDateFilter && matchSearch) {
                filteredList.add(transaction);
            }
        }

        adapter.updateList(filteredList);
        updateEmptyState(filteredList, "Không tìm thấy giao dịch");
    }

    private void updateEmptyState(ArrayList<TransactionModel> list, String emptyMessage) {
        if (list == null || list.isEmpty()) {
            tvEmpty.setText(emptyMessage);
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerTransactions.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void updateFilterButtonStyle() {
        setButtonSelected(btnFilterAll, currentFilter.equals("Tất cả"), "#5B3FFF", "#5B3FFF");
        setButtonSelected(btnFilterIncome, currentFilter.equals("Thu nhập"), "#16A34A", "#16A34A");
        setButtonSelected(btnFilterExpense, currentFilter.equals("Chi tiêu"), "#EF4444", "#EF4444");
    }

    private void setButtonSelected(MaterialButton button, boolean selected, String activeColor, String textColor) {
        if (selected) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(activeColor)));
            button.setTextColor(Color.WHITE);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            button.setTextColor(Color.parseColor(textColor));
        }
    }
}