package com.example.mywallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mywallet.db";
    public static final int DATABASE_VERSION = 8;

    public static final String TABLE_TRANSACTION = "transactions";
    public static final String COL_ID = "id";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_TYPE = "type";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DATE = "date";
    public static final String COL_NOTE = "note";

    public static final String TABLE_BUDGET = "budgets";
    public static final String BUDGET_ID = "id";
    public static final String BUDGET_NAME = "name";
    public static final String BUDGET_ICON = "icon";
    public static final String BUDGET_LIMIT = "limit_amount";
    public static final String BUDGET_CURRENT = "current_amount";
    public static final String BUDGET_CYCLE_DAYS = "cycle_days";
    public static final String BUDGET_COLOR = "color";

    public static final String TABLE_BUDGET_HISTORY = "budget_history";
    public static final String HISTORY_ID = "id";
    public static final String HISTORY_BUDGET_NAME = "budget_name";
    public static final String HISTORY_ACTION = "action";
    public static final String HISTORY_AMOUNT = "amount";
    public static final String HISTORY_DATE = "date";
    public static final String HISTORY_NOTE = "note";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTransactionTable = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AMOUNT + " REAL, " +
                COL_TYPE + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_NOTE + " TEXT" +
                ")";

        String createBudgetTable = "CREATE TABLE " + TABLE_BUDGET + " (" +
                BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BUDGET_NAME + " TEXT, " +
                BUDGET_ICON + " TEXT, " +
                BUDGET_LIMIT + " REAL, " +
                BUDGET_CURRENT + " REAL DEFAULT 0, " +
                BUDGET_CYCLE_DAYS + " INTEGER DEFAULT 12, " +
                BUDGET_COLOR + " TEXT" +
                ")";

        String createBudgetHistoryTable = "CREATE TABLE " + TABLE_BUDGET_HISTORY + " (" +
                HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HISTORY_BUDGET_NAME + " TEXT, " +
                HISTORY_ACTION + " TEXT, " +
                HISTORY_AMOUNT + " REAL, " +
                HISTORY_DATE + " TEXT, " +
                HISTORY_NOTE + " TEXT" +
                ")";

        db.execSQL(createTransactionTable);
        db.execSQL(createBudgetTable);
        db.execSQL(createBudgetHistoryTable);

        insertDefaultBudgets(db);
    }

    private void insertDefaultBudgets(SQLiteDatabase db) {
        insertBudgetDirect(db, "Hũ đi chơi", "💖", 2000000, 0, 12, "#FF9AAD");
        insertBudgetDirect(db, "Hũ chia sẻ", "💖", 1000000, 0, 12, "#FF9AAD");
        insertBudgetDirect(db, "Ăn uống", "🍽", 3000000, 0, 12, "#FF6F6F");
    }

    private void insertBudgetDirect(SQLiteDatabase db, String name, String icon, double limit, double current, int cycleDays, String color) {
        ContentValues values = new ContentValues();
        values.put(BUDGET_NAME, name);
        values.put(BUDGET_ICON, icon);
        values.put(BUDGET_LIMIT, limit);
        values.put(BUDGET_CURRENT, current);
        values.put(BUDGET_CYCLE_DAYS, cycleDays);
        values.put(BUDGET_COLOR, color);
        db.insert(TABLE_BUDGET, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String createBudgetTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET + " (" +
                BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BUDGET_NAME + " TEXT, " +
                BUDGET_ICON + " TEXT, " +
                BUDGET_LIMIT + " REAL, " +
                BUDGET_CURRENT + " REAL DEFAULT 0, " +
                BUDGET_CYCLE_DAYS + " INTEGER DEFAULT 12, " +
                BUDGET_COLOR + " TEXT" +
                ")";

        String createBudgetHistoryTable = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET_HISTORY + " (" +
                HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HISTORY_BUDGET_NAME + " TEXT, " +
                HISTORY_ACTION + " TEXT, " +
                HISTORY_AMOUNT + " REAL, " +
                HISTORY_DATE + " TEXT, " +
                HISTORY_NOTE + " TEXT" +
                ")";

        db.execSQL(createBudgetTable);
        db.execSQL(createBudgetHistoryTable);

        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_BUDGET + " ADD COLUMN " + BUDGET_CURRENT + " REAL DEFAULT 0");
            } catch (Exception ignored) {
            }
        }

        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_BUDGET + " ADD COLUMN " + BUDGET_CYCLE_DAYS + " INTEGER DEFAULT 12");
            } catch (Exception ignored) {
            }
        }

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BUDGET, null);
        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        if (count == 0) {
            insertDefaultBudgets(db);
        }
    }

    private String getToday() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    private void insertTransactionDirect(SQLiteDatabase db, double amount, String type, String category, String note) {
        ContentValues values = new ContentValues();
        values.put(COL_AMOUNT, amount);
        values.put(COL_TYPE, type);
        values.put(COL_CATEGORY, category);
        values.put(COL_DATE, getToday());
        values.put(COL_NOTE, note);
        db.insert(TABLE_TRANSACTION, null, values);
    }

    private void insertBudgetHistoryDirect(SQLiteDatabase db, String budgetName, String action, double amount, String note) {
        ContentValues values = new ContentValues();
        values.put(HISTORY_BUDGET_NAME, budgetName);
        values.put(HISTORY_ACTION, action);
        values.put(HISTORY_AMOUNT, amount);
        values.put(HISTORY_DATE, getToday());
        values.put(HISTORY_NOTE, note);
        db.insert(TABLE_BUDGET_HISTORY, null, values);
    }

    public boolean insertTransaction(TransactionModel transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_AMOUNT, transaction.getAmount());
        values.put(COL_TYPE, transaction.getType());
        values.put(COL_CATEGORY, transaction.getCategory());
        values.put(COL_DATE, transaction.getDate());
        values.put(COL_NOTE, transaction.getNote());

        long result = db.insert(TABLE_TRANSACTION, null, values);
        db.close();

        return result != -1;
    }

    public boolean updateTransaction(TransactionModel transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_AMOUNT, transaction.getAmount());
        values.put(COL_TYPE, transaction.getType());
        values.put(COL_CATEGORY, transaction.getCategory());
        values.put(COL_DATE, transaction.getDate());
        values.put(COL_NOTE, transaction.getNote());

        int result = db.update(
                TABLE_TRANSACTION,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())}
        );

        db.close();

        return result > 0;
    }

    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_TRANSACTION,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();

        return result > 0;
    }

    public boolean deleteAllTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(TABLE_TRANSACTION, null, null);

        db.close();

        return result >= 0;
    }

    public ArrayList<TransactionModel> getAllTransactions() {
        ArrayList<TransactionModel> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TRANSACTION + " ORDER BY " + COL_ID + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE));

                TransactionModel transaction = new TransactionModel(
                        id,
                        amount,
                        type,
                        category,
                        date,
                        note
                );

                list.add(transaction);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    public ArrayList<TransactionModel> getRecentTransactions() {
        ArrayList<TransactionModel> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TRANSACTION +
                        " ORDER BY " + COL_ID + " DESC LIMIT 3",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE));

                TransactionModel transaction = new TransactionModel(
                        id,
                        amount,
                        type,
                        category,
                        date,
                        note
                );

                list.add(transaction);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    public double getTotalIncome() {
        double total = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTION +
                        " WHERE " + COL_TYPE + " = ?",
                new String[]{"Thu nhập"}
        );

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        return total;
    }

    public double getTotalExpense() {
        double total = 0;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTION +
                        " WHERE " + COL_TYPE + " = ?",
                new String[]{"Chi tiêu"}
        );

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        return total;
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public ArrayList<String> getExpenseByCategory() {
        ArrayList<String> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") AS total FROM " + TABLE_TRANSACTION +
                        " WHERE " + COL_TYPE + " = ?" +
                        " GROUP BY " + COL_CATEGORY +
                        " ORDER BY total DESC",
                new String[]{"Chi tiêu"}
        );

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double total = cursor.getDouble(1);

                list.add(category + "|" + total);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    public boolean insertBudget(BudgetModel budget) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BUDGET_NAME, budget.getName());
        values.put(BUDGET_ICON, budget.getIcon());
        values.put(BUDGET_LIMIT, budget.getLimitAmount());
        values.put(BUDGET_CURRENT, budget.getCurrentAmount());
        values.put(BUDGET_CYCLE_DAYS, budget.getCycleDays());
        values.put(BUDGET_COLOR, budget.getColor());

        long result = db.insert(TABLE_BUDGET, null, values);
        db.close();

        return result != -1;
    }

    public boolean updateBudget(BudgetModel budget) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BUDGET_NAME, budget.getName());
        values.put(BUDGET_ICON, budget.getIcon());
        values.put(BUDGET_LIMIT, budget.getLimitAmount());
        values.put(BUDGET_CURRENT, budget.getCurrentAmount());
        values.put(BUDGET_CYCLE_DAYS, budget.getCycleDays());
        values.put(BUDGET_COLOR, budget.getColor());

        int result = db.update(
                TABLE_BUDGET,
                values,
                BUDGET_ID + " = ?",
                new String[]{String.valueOf(budget.getId())}
        );

        db.close();

        return result > 0;
    }

    public boolean updateBudgetCurrentAmount(int budgetId, double newAmount) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BUDGET_CURRENT, newAmount);

        int result = db.update(
                TABLE_BUDGET,
                values,
                BUDGET_ID + " = ?",
                new String[]{String.valueOf(budgetId)}
        );

        db.close();

        return result > 0;
    }

    public boolean addMoneyToBudgetAndLog(BudgetModel budget, double amount) {
        if (budget == null || amount <= 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            double newAmount = budget.getCurrentAmount() + amount;

            ContentValues budgetValues = new ContentValues();
            budgetValues.put(BUDGET_CURRENT, newAmount);

            int budgetResult = db.update(
                    TABLE_BUDGET,
                    budgetValues,
                    BUDGET_ID + " = ?",
                    new String[]{String.valueOf(budget.getId())}
            );

            if (budgetResult <= 0) {
                return false;
            }

            insertTransactionDirect(
                    db,
                    amount,
                    "Chi tiêu",
                    "Nạp hũ",
                    "Nạp hũ: " + budget.getName()
            );

            insertBudgetHistoryDirect(
                    db,
                    budget.getName(),
                    "Nạp hũ",
                    amount,
                    "Nạp tiền vào hũ " + budget.getName()
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean withdrawMoneyFromBudgetAndLog(BudgetModel budget, double amount) {
        if (budget == null || amount <= 0 || amount > budget.getCurrentAmount()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            double newAmount = budget.getCurrentAmount() - amount;

            ContentValues budgetValues = new ContentValues();
            budgetValues.put(BUDGET_CURRENT, newAmount);

            int budgetResult = db.update(
                    TABLE_BUDGET,
                    budgetValues,
                    BUDGET_ID + " = ?",
                    new String[]{String.valueOf(budget.getId())}
            );

            if (budgetResult <= 0) {
                return false;
            }

            insertTransactionDirect(
                    db,
                    amount,
                    "Thu nhập",
                    "Rút hũ",
                    "Rút từ hũ: " + budget.getName()
            );

            insertBudgetHistoryDirect(
                    db,
                    budget.getName(),
                    "Rút hũ",
                    amount,
                    "Rút tiền khỏi hũ " + budget.getName()
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean transferBudgetMoney(int fromBudgetId, int toBudgetId, double amount) {
        if (fromBudgetId == toBudgetId || amount <= 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            double fromCurrent;
            double toCurrent;
            String fromName;
            String toName;

            Cursor fromCursor = db.rawQuery(
                    "SELECT " + BUDGET_NAME + ", " + BUDGET_CURRENT + " FROM " + TABLE_BUDGET +
                            " WHERE " + BUDGET_ID + " = ?",
                    new String[]{String.valueOf(fromBudgetId)}
            );

            if (fromCursor.moveToFirst()) {
                fromName = fromCursor.getString(0);
                fromCurrent = fromCursor.getDouble(1);
            } else {
                fromCursor.close();
                return false;
            }

            fromCursor.close();

            Cursor toCursor = db.rawQuery(
                    "SELECT " + BUDGET_NAME + ", " + BUDGET_CURRENT + " FROM " + TABLE_BUDGET +
                            " WHERE " + BUDGET_ID + " = ?",
                    new String[]{String.valueOf(toBudgetId)}
            );

            if (toCursor.moveToFirst()) {
                toName = toCursor.getString(0);
                toCurrent = toCursor.getDouble(1);
            } else {
                toCursor.close();
                return false;
            }

            toCursor.close();

            if (amount > fromCurrent) {
                return false;
            }

            ContentValues fromValues = new ContentValues();
            fromValues.put(BUDGET_CURRENT, fromCurrent - amount);

            ContentValues toValues = new ContentValues();
            toValues.put(BUDGET_CURRENT, toCurrent + amount);

            int fromResult = db.update(
                    TABLE_BUDGET,
                    fromValues,
                    BUDGET_ID + " = ?",
                    new String[]{String.valueOf(fromBudgetId)}
            );

            int toResult = db.update(
                    TABLE_BUDGET,
                    toValues,
                    BUDGET_ID + " = ?",
                    new String[]{String.valueOf(toBudgetId)}
            );

            if (fromResult <= 0 || toResult <= 0) {
                return false;
            }

            insertBudgetHistoryDirect(
                    db,
                    fromName,
                    "Chuyển đi",
                    amount,
                    "Chuyển sang hũ " + toName
            );

            insertBudgetHistoryDirect(
                    db,
                    toName,
                    "Chuyển đến",
                    amount,
                    "Nhận từ hũ " + fromName
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean deleteBudget(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_BUDGET,
                BUDGET_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();

        return result > 0;
    }

    public ArrayList<BudgetModel> getAllBudgets() {
        ArrayList<BudgetModel> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_BUDGET + " ORDER BY " + BUDGET_ID + " ASC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(BUDGET_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(BUDGET_NAME));
                String icon = cursor.getString(cursor.getColumnIndexOrThrow(BUDGET_ICON));
                double limit = cursor.getDouble(cursor.getColumnIndexOrThrow(BUDGET_LIMIT));
                double current = cursor.getDouble(cursor.getColumnIndexOrThrow(BUDGET_CURRENT));
                int cycleDays = cursor.getInt(cursor.getColumnIndexOrThrow(BUDGET_CYCLE_DAYS));
                String color = cursor.getString(cursor.getColumnIndexOrThrow(BUDGET_COLOR));

                if (cycleDays <= 0) {
                    cycleDays = 12;
                }

                BudgetModel budget = new BudgetModel(id, name, icon, limit, current, cycleDays, color);
                list.add(budget);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }
}