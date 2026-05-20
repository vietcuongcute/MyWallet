package com.example.mywallet;

public class TransactionModel {
    private int id;
    private double amount;
    private String type;
    private String category;
    private String date;
    private String note;

    public TransactionModel(int id, double amount, String type, String category, String date, String note) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public TransactionModel(double amount, String type, String category, String date, String note) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNote(String note) {
        this.note = note;
    }
}