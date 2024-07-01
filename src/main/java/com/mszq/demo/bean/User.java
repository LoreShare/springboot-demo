package com.mszq.demo.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("年龄")
    private Integer age;
}
