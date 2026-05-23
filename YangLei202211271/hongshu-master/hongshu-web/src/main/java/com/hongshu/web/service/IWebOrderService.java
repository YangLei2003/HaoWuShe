package com.hongshu.web.service;

import com.hongshu.web.domain.entity.WebOrder;
import java.util.List;

public interface IWebOrderService {
    List<WebOrder> selectList(Integer pageNum, Integer pageSize);
    int selectCount();
    int deleteById(Integer id);

    int update(WebOrder webOrder);
}