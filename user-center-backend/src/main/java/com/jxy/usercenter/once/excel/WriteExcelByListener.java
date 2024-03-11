package com.jxy.usercenter.once.excel;

import com.alibaba.excel.EasyExcel;

public class WriteExcelByListener {
    public static void main(String[] args) {
        String file = "D:\\code\\project\\partner-matching\\user-center-backend\\src\\main\\resources\\prodExcel.xlsx";
        EasyExcel.read(file, ExcelUserInfo.class, new MultipleSheetsListener()).doReadAll();
    }

}
