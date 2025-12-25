package org.whu.timeflow.dto;

import lombok.Data;
import org.whu.timeflow.entity.Bill;
import org.whu.timeflow.entity.Diary;
import org.whu.timeflow.entity.Todo;
import java.util.List;

@Data
public class SyncDTO {
    // 上传用
    private List<Diary> diaries;
    private List<Bill> bills;
    private List<Todo> todos;

    // 下载用 (告诉后端我上次同步的时间)
    private Long lastSyncTime;
}