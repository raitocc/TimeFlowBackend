package org.whu.timeflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.whu.timeflow.entity.Bill;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}