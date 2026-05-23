package com.hongshu.web.service.impl;

import com.hongshu.web.domain.entity.WebOrder;
import com.hongshu.web.mapper.WebOrderMapper;
import com.hongshu.web.service.IWebOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WebOrderServiceImpl implements IWebOrderService {

    @Autowired
    private WebOrderMapper webOrderMapper;

    @Override
    public List<WebOrder> selectList(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return webOrderMapper.selectList(offset, pageSize);
    }

    @Override
    public int selectCount() {
        return webOrderMapper.selectCount();
    }

    @Override
    public int deleteById(Integer id) {
        return webOrderMapper.deleteById(id);
    }

    @Override
    public int update(WebOrder webOrder) {
        return webOrderMapper.update(webOrder);
    }
}