package com.security.demo.excel;

import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.read.context.AnalysisContext;
import com.alibaba.excel.read.event.AnalysisEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelListener<T extends BaseRowModel> extends AnalysisEventListener<T> {

    //自定义用于暂时存储data。
    //可以通过实例获取该值
    private List<T> datas = new ArrayList<>();

    /**
     * 通过 AnalysisContext 对象还可以获取当前 sheet，当前行等数据
     */
    @Override
    public void invoke(T baseRowModel, AnalysisContext context) {
        //数据存储到list，供批量处理，或后续自己业务逻辑处理。
        datas.add(baseRowModel);
        //根据业务自行 do something
        doSomething();

        /*
        如数据过大，可以进行定量分批处理
        if(datas.size()<=100){
            datas.add(object);
        }else {
            doSomething();
            datas = new ArrayList<Object>();
        }
         */

    }

    /**
     * 根据业务自行实现该方法
     */
    private void doSomething() {
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            context.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
            datas.clear();
            解析结束销毁不用的资源
         */
    }

    public List<T> getDatas() {
        System.out.println(datas);
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

}
