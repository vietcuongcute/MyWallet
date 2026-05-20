package com.example.mywallet;

public class BudgetModel {
    private int id;
    private String name;
    private String icon;
    private double limitAmount;
    private double currentAmount;
    private int cycleDays;
    private String color;

    public BudgetModel(int id, String name, String icon, double limitAmount, double currentAmount, int cycleDays, String color) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.limitAmount = limitAmount;
        this.currentAmount = currentAmount;
        this.cycleDays = cycleDays;
        this.color = color;
    }

    public BudgetModel(String name, String icon, double limitAmount, double currentAmount, int cycleDays, String color) {
        this.name = name;
        this.icon = icon;
        this.limitAmount = limitAmount;
        this.currentAmount = currentAmount;
        this.cycleDays = cycleDays;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public int getCycleDays() {
        return cycleDays;
    }

    public String getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public void setCycleDays(int cycleDays) {
        this.cycleDays = cycleDays;
    }

    public void setColor(String color) {
        this.color = color;
    }
}