package com.example.mywallet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class BudgetFragment extends Fragment {

    TextView tvEmptyBudget, tvHideBalance, tvHideProgress;
    RecyclerView recyclerBudgets;

    LinearLayout actionDeposit, actionWithdraw, actionTransfer;

    DatabaseHelper databaseHelper;
    ArrayList<BudgetModel> budgetList;
    BudgetAdapter adapter;

    boolean isBalanceHidden = false;
    boolean isProgressHidden = false;

    DecimalFormat decimalFormat;

    public BudgetFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        tvEmptyBudget = view.findViewById(R.id.tvEmptyBudget);
        tvHideBalance = view.findViewById(R.id.tvHideBalance);
        tvHideProgress = view.findViewById(R.id.tvHideProgress);

        recyclerBudgets = view.findViewById(R.id.recyclerBudgets);

        actionDeposit = view.findViewById(R.id.actionDeposit);
        actionWithdraw = view.findViewById(R.id.actionWithdraw);
        actionTransfer = view.findViewById(R.id.actionTransfer);

        databaseHelper = new DatabaseHelper(requireContext());
        decimalFormat = new DecimalFormat("#,###");

        recyclerBudgets.setLayoutManager(new LinearLayoutManager(getContext()));

        setupTopActions(view);
        setupBottomActions();
        loadBudgets();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void setupTopActions(View view) {
        view.findViewById(R.id.btnBackBudget).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đang ở màn hình Hũ tiết kiệm", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.btnShareBudget).setOnClickListener(v ->
                Toast.makeText(getContext(), "Chia sẻ hũ tiết kiệm", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.btnAddBudget).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddBudgetActivity.class);
            startActivity(intent);
        });

        tvHideBalance.setOnClickListener(v -> {
            isBalanceHidden = !isBalanceHidden;
            updateHideButtons();

            if (adapter != null) {
                adapter.setHideBalance(isBalanceHidden);
            }
        });

        tvHideProgress.setOnClickListener(v -> {
            isProgressHidden = !isProgressHidden;
            updateHideButtons();

            if (adapter != null) {
                adapter.setHideProgress(isProgressHidden);
            }
        });

        updateHideButtons();
    }

    private void updateHideButtons() {
        if (isBalanceHidden) {
            tvHideBalance.setText("◉  HIỆN SỐ DƯ");
        } else {
            tvHideBalance.setText("◉  ẨN SỐ DƯ");
        }

        if (isProgressHidden) {
            tvHideProgress.setText("▰  HIỆN TIẾN ĐỘ");
        } else {
            tvHideProgress.setText("▰  ẨN TIẾN ĐỘ");
        }
    }

    private void setupBottomActions() {
        actionDeposit.setOnClickListener(v -> showSelectBudgetMoneyDialog(true));

        actionWithdraw.setOnClickListener(v -> showSelectBudgetMoneyDialog(false));

        actionTransfer.setOnClickListener(v -> showTransferDialog());
    }

    private void loadBudgets() {
        if (databaseHelper == null) {
            return;
        }

        budgetList = databaseHelper.getAllBudgets();
        double totalExpense = databaseHelper.getTotalExpense();

        if (budgetList.isEmpty()) {
            tvEmptyBudget.setVisibility(View.VISIBLE);
            recyclerBudgets.setVisibility(View.GONE);
        } else {
            tvEmptyBudget.setVisibility(View.GONE);
            recyclerBudgets.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new BudgetAdapter(requireContext(), budgetList, totalExpense);
                adapter.setHideBalance(isBalanceHidden);
                adapter.setHideProgress(isProgressHidden);
                recyclerBudgets.setAdapter(adapter);
            } else {
                adapter.updateList(budgetList, totalExpense);
                adapter.setHideBalance(isBalanceHidden);
                adapter.setHideProgress(isProgressHidden);
            }
        }
    }

    private void showSelectBudgetMoneyDialog(boolean isDeposit) {
        budgetList = databaseHelper.getAllBudgets();

        if (budgetList == null || budgetList.isEmpty()) {
            Toast.makeText(getContext(), "Chưa có hũ tiết kiệm nào", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        AutoCompleteTextView dropdownBudget = new AutoCompleteTextView(requireContext());
        dropdownBudget.setHint("Chọn hũ");
        dropdownBudget.setInputType(InputType.TYPE_NULL);
        dropdownBudget.setSingleLine(true);
        dropdownBudget.setPadding(20, 20, 20, 20);

        EditText edtAmount = new EditText(requireContext());
        edtAmount.setHint("Nhập số tiền");
        edtAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtAmount.setSingleLine(true);
        edtAmount.setPadding(20, 20, 20, 20);

        ArrayList<String> budgetNames = new ArrayList<>();

        for (BudgetModel budget : budgetList) {
            budgetNames.add(budget.getName() + " - " + decimalFormat.format(budget.getCurrentAmount()) + " đ");
        }

        ArrayAdapter<String> adapterNames = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                budgetNames
        );

        dropdownBudget.setAdapter(adapterNames);
        dropdownBudget.setOnClickListener(v -> dropdownBudget.showDropDown());

        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        fieldParams.setMargins(0, 0, 0, 18);

        layout.addView(dropdownBudget, fieldParams);
        layout.addView(edtAmount, fieldParams);

        String title = isDeposit ? "Thêm tiền tiết kiệm" : "Rút tiền khỏi hũ";

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(layout)
                .setPositiveButton(isDeposit ? "Thêm" : "Rút", (dialog, which) -> {
                    int budgetIndex = budgetNames.indexOf(dropdownBudget.getText().toString());
                    String amountText = edtAmount.getText().toString().trim();

                    if (budgetIndex < 0) {
                        Toast.makeText(getContext(), "Chọn hũ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amountText.isEmpty()) {
                        Toast.makeText(getContext(), "Nhập số tiền", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;

                    try {
                        amount = Double.parseDouble(amountText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount <= 0) {
                        Toast.makeText(getContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BudgetModel budget = budgetList.get(budgetIndex);

                    double newAmount;

                    if (isDeposit) {
                        double walletBalance = databaseHelper.getBalance();
                        double totalMoneyInBudgets = getTotalMoneyInBudgets();
                        double availableBalance = walletBalance - totalMoneyInBudgets;

                        if (availableBalance < 0) {
                            availableBalance = 0;
                        }

                        if (amount > availableBalance) {
                            Toast.makeText(
                                    getContext(),
                                    "Số dư ví không đủ. Có thể thêm tối đa "
                                            + decimalFormat.format(availableBalance) + " đ",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        newAmount = budget.getCurrentAmount() + amount;

                        if (newAmount > budget.getLimitAmount()) {
                            Toast.makeText(getContext(), "Số tiền thêm vượt quá mục tiêu", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        if (amount > budget.getCurrentAmount()) {
                            Toast.makeText(getContext(), "Số tiền rút lớn hơn số tiền đã tiết kiệm", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        newAmount = budget.getCurrentAmount() - amount;
                    }

                    boolean success;

                    if (isDeposit) {
                        success = databaseHelper.addMoneyToBudgetAndLog(budget, amount);
                    } else {
                        success = databaseHelper.withdrawMoneyFromBudgetAndLog(budget, amount);
                    }

                    if (success) {
                        Toast.makeText(
                                getContext(),
                                isDeposit ? "Đã thêm tiền tiết kiệm" : "Đã rút tiền khỏi hũ",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadBudgets();
                    } else {
                        Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }


                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private double getTotalMoneyInBudgets() {
        double total = 0;

        if (budgetList == null) {
            return 0;
        }

        for (BudgetModel budget : budgetList) {
            total += budget.getCurrentAmount();
        }

        return total;
    }

    private void showTransferDialog() {
        budgetList = databaseHelper.getAllBudgets();

        if (budgetList == null || budgetList.size() < 2) {
            Toast.makeText(getContext(), "Cần ít nhất 2 hũ để chuyển tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        AutoCompleteTextView dropdownFrom = new AutoCompleteTextView(requireContext());
        dropdownFrom.setHint("Chọn hũ nguồn");
        dropdownFrom.setInputType(InputType.TYPE_NULL);
        dropdownFrom.setSingleLine(true);
        dropdownFrom.setPadding(20, 20, 20, 20);

        AutoCompleteTextView dropdownTo = new AutoCompleteTextView(requireContext());
        dropdownTo.setHint("Chọn hũ nhận");
        dropdownTo.setInputType(InputType.TYPE_NULL);
        dropdownTo.setSingleLine(true);
        dropdownTo.setPadding(20, 20, 20, 20);

        EditText edtAmount = new EditText(requireContext());
        edtAmount.setHint("Nhập số tiền");
        edtAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtAmount.setSingleLine(true);
        edtAmount.setPadding(20, 20, 20, 20);

        ArrayList<String> budgetNames = new ArrayList<>();

        for (BudgetModel budget : budgetList) {
            budgetNames.add(budget.getName() + " - " + decimalFormat.format(budget.getCurrentAmount()) + " đ");
        }

        ArrayAdapter<String> adapterNames = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                budgetNames
        );

        dropdownFrom.setAdapter(adapterNames);
        dropdownTo.setAdapter(adapterNames);

        dropdownFrom.setOnClickListener(v -> dropdownFrom.showDropDown());
        dropdownTo.setOnClickListener(v -> dropdownTo.showDropDown());

        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        fieldParams.setMargins(0, 0, 0, 18);

        layout.addView(dropdownFrom, fieldParams);
        layout.addView(dropdownTo, fieldParams);
        layout.addView(edtAmount, fieldParams);

        new AlertDialog.Builder(requireContext())
                .setTitle("Chuyển tiền giữa các hũ")
                .setView(layout)
                .setPositiveButton("Chuyển tiền", (dialog, which) -> {
                    int fromIndex = budgetNames.indexOf(dropdownFrom.getText().toString());
                    int toIndex = budgetNames.indexOf(dropdownTo.getText().toString());
                    String amountText = edtAmount.getText().toString().trim();

                    if (fromIndex < 0) {
                        Toast.makeText(getContext(), "Chọn hũ nguồn", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (toIndex < 0) {
                        Toast.makeText(getContext(), "Chọn hũ nhận", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (fromIndex == toIndex) {
                        Toast.makeText(getContext(), "Hũ nguồn và hũ nhận không được trùng nhau", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amountText.isEmpty()) {
                        Toast.makeText(getContext(), "Nhập số tiền cần chuyển", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;

                    try {
                        amount = Double.parseDouble(amountText);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount <= 0) {
                        Toast.makeText(getContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BudgetModel fromBudget = budgetList.get(fromIndex);
                    BudgetModel toBudget = budgetList.get(toIndex);

                    if (amount > fromBudget.getCurrentAmount()) {
                        Toast.makeText(getContext(), "Số tiền chuyển lớn hơn số dư hũ nguồn", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double newToAmount = toBudget.getCurrentAmount() + amount;

                    if (newToAmount > toBudget.getLimitAmount()) {
                        Toast.makeText(getContext(), "Hũ nhận sẽ vượt quá mục tiêu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = databaseHelper.transferBudgetMoney(
                            fromBudget.getId(),
                            toBudget.getId(),
                            amount
                    );

                    if (success) {
                        Toast.makeText(getContext(), "Đã chuyển tiền giữa các hũ", Toast.LENGTH_SHORT).show();
                        loadBudgets();
                    } else {
                        Toast.makeText(getContext(), "Chuyển tiền thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}