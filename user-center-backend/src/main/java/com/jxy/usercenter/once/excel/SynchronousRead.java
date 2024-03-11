package com.jxy.usercenter.once.excel;

import java.util.List;

import com.alibaba.excel.EasyExcel;

import lombok.extern.slf4j.Slf4j;

/**
 * 同步读
 *
 */

@Slf4j
public class SynchronousRead {

    /**
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void main(String[] args) {
        String fileName = "D:\\code\\project\\partner-matching\\user-center-backend\\src\\main\\resources\\prodExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<ExcelUserInfo> list = EasyExcel.read(fileName).head(ExcelUserInfo.class).sheet().doReadSync();
        for (ExcelUserInfo data : list) {
            System.out.println(data);
        }
    }

}