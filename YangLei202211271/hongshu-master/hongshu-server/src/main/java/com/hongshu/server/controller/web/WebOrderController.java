package com.hongshu.server.controller.web;

import com.hongshu.common.core.page.TableDataInfo;
import com.hongshu.common.core.domain.AjaxResult;
import com.hongshu.web.domain.entity.WebOrder;
import com.hongshu.web.service.IWebOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/web/order")
public class WebOrderController {

    @Autowired
    private IWebOrderService webOrderService;

    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        List<WebOrder> list = webOrderService.selectList(pageNum, pageSize);
        int total = webOrderService.selectCount();
        return new TableDataInfo(list, total);
    }

    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Integer id) {
        return AjaxResult.success(webOrderService.deleteById(id));
    }
    @PutMapping("/update")
    public AjaxResult update(@RequestBody WebOrder webOrder) {
        return AjaxResult.success(webOrderService.update(webOrder));
    }
}