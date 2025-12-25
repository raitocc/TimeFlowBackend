package org.whu.timeflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.whu.timeflow.entity.Todo;
import org.whu.timeflow.mapper.TodoMapper;
import org.whu.timeflow.service.ITodoService;

@Service
public class TodoServiceImpl extends ServiceImpl<TodoMapper, Todo> implements ITodoService {
}