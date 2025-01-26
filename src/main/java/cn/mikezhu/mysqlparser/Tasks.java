package cn.mikezhu.mysqlparser;

import cn.mikezhu.mysqlparser.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Async
public class Tasks {
    private static final Logger logger = LoggerFactory.getLogger(Tasks.class);
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private TaskMapper taskMapper;

    private void startRun(String taskName) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
        logger.info("{} start run {}", taskName, currentTime);
    }

    @Scheduled(cron = "2,12,22,32,42,52 * * * * ?")
    public void testTask1() {
        logger.info(taskMapper.selectAllTasks().size() + "");
    }
}