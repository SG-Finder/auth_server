package com.finder.genie_ai.model;

import lombok.Data;

import java.util.List;

@Data
public class BaseListModel<T> {

    private List<T> datas;
    private int cursor;
    private int count;

}