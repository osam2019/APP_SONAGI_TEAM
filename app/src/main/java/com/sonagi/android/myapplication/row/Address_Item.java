package com.sonagi.android.myapplication.row;

public class Address_Item {
    // PersonData 정보를 담고 있는 객체 생성
    private String sDate;
    private String eDate;
    private String plan;
    boolean checkBoxState;

    public Address_Item() {
    }

    public Address_Item(String sDate, String eDate, String plan, boolean checkBoxState) {
        this.sDate = sDate;
        this.eDate = eDate;
        this.plan = plan;
        this.checkBoxState = checkBoxState;
    }
    public String getsDate() {
        return sDate;
    }

    public void setsDate(String sDate) {
        this.sDate = sDate;
    }

    public String geteDate() {
        return eDate;
    }

    public void seteDate(String eDate) {
        this.eDate = eDate;
    }

    public String getPlan() {
        return plan;
    }

    public void setplan(String plan) {
        this.plan = plan;
    }

    public boolean isCheckBoxState() {
        return checkBoxState;
    }

    public void setCheckBoxState(boolean checkBoxState) {
        this.checkBoxState = checkBoxState;
    }
}