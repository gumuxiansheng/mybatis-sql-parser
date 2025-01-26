package cn.mikezhu.mysqlparser.mapper;
import cn.mikezhu.mysqlparser.bo.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper {
    Task selectTaskById(Map taskId);

    List<Task> selectAllTasks();

    int insertTask(Task task);

    int updateTask(Task task);

    int deleteTask(Long taskId);
}
