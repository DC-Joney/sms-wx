package com.security.demo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDataDto extends BaseRowModel {

    //行政区划
    @ExcelProperty(value = "所在市",index = 0)
    private Integer id;

    //行政区划
    @ExcelProperty(value = "所在市",index = 1)
    private String cityName;

    @ExcelProperty(value = "所在县（区）",index = 2)
    private String countySeat;

    @ExcelProperty(value = "所在乡（镇）",index = 3)
    private String  town;

    @ExcelProperty(value = "所在村",index = 4)
    private String village;

    public String getCityName() {
        return cityName;
    }

    public String getCountySeat() {
        return countySeat == null ? "" : countySeat;
    }

    public String getTown() {
        return town;
    }

    public String getVillage() {
        return village == null ? "" : village;
    }


    public ExcelDataDto setTown(String town){
        this.town = town;
        return this;
    }
}
