package cn.mikezhu.mysqlparser.mapper;

import cn.mikezhu.mysqlparser.DemoApplication;
import cn.mikezhu.mysqlparser.bo.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    @Test
    public void testSelectTaskById() {
        Map map = new HashMap();
        map.put("taskId", 1L);
        Task task = taskMapper.selectTaskById(map);
        assertNotNull(task);
        assertEquals("test1", task.getTaskName());
    }

    @Test
    public void testSelectAllTasks() {
        List<Task> tasks = taskMapper.selectAllTasks();
        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());
    }

    @Test
    public void testInsertTask() {
        Task task = new Task();
        task.setTaskName("New Task");
        task.setLastStartTime(new Timestamp(System.currentTimeMillis()));
        task.setLastEndTime(new Timestamp(System.currentTimeMillis()));
        task.setLastHost("localhost");
        task.setNextScheduledTime(new Timestamp(System.currentTimeMillis()));
        task.setTaskEx("Example");
        task.setExecutionNum(0);

        int rows = taskMapper.insertTask(task);
        assertEquals(1, rows);
        assertNotNull(task.getTaskId());
    }

    @Test
    public void testUpdateTask() {
        Map map = new HashMap();
        map.put("taskId", 1L);
        Task task = taskMapper.selectTaskById(map);
        task.setTaskName("Updated Task");
        int rows = taskMapper.updateTask(task);
        assertEquals(1, rows);
    }

    @Test
    public void testDeleteTask() {
        int rows = taskMapper.deleteTask(1L);
        assertEquals(1, rows);
    }
}