package org.whu.timeflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.whu.timeflow.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}