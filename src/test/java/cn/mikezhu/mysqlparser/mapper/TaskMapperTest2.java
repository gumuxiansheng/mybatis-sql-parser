package cn.mikezhu.mysqlparser.mapper;

import cn.mikezhu.mysqlparser.bo.Task;
import cn.mikezhu.mysqlparser.utils.MyBatisMapperUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TaskMapperTest2 {

    @Test
    public void test() {
        TaskMapper taskMapper = MyBatisMapperUtil.getMapper(TaskMapper.class);
        Map map = new HashMap();
        map.put("taskId", 1L);
        Task task = taskMapper.selectTaskById(map);
        System.out.println(task);
    }

    @Test
    public void testUpdateTask() {
        TaskMapper taskMapper = MyBatisMapperUtil.getMapper(TaskMapper.class);
        Map map = new HashMap();
        map.put("taskId", 1L);
        Task task = taskMapper.selectTaskById(map);
        task.setTaskName("Updated Task");
        int rows = taskMapper.updateTask(task);
        assertEquals(1, rows);
    }
}
