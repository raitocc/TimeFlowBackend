package org.whu.timeflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.whu.timeflow.entity.Bill;
import org.whu.timeflow.mapper.BillMapper;
import org.whu.timeflow.service.IBillService;

@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements IBillService {
}