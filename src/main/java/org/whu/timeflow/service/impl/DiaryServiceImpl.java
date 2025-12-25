package org.whu.timeflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.whu.timeflow.entity.Diary;
import org.whu.timeflow.mapper.DiaryMapper;
import org.whu.timeflow.service.IDiaryService;

@Service
public class DiaryServiceImpl extends ServiceImpl<DiaryMapper, Diary> implements IDiaryService {
}