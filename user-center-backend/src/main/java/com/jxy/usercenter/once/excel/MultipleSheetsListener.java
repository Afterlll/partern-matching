package com.jxy.usercenter.once.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;

public class MultipleSheetsListener implements ReadListener<ExcelUserInfo> {
    @Override
    //每一行读取完毕会调用
    public void invoke(ExcelUserInfo data, AnalysisContext context) {
        // 当前sheet的名称 编码获取类似
        context.readSheetHolder().getSheetName();
        System.out.println(data);
    }

    @Override
    //每个sheet读取完毕会调用
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 当前sheet的名称 编码获取类似
        context.readSheetHolder().getSheetName();
    }
}
