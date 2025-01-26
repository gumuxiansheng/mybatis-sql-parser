package cn.mikezhu.mysqlparser.bo;

import java.sql.Timestamp;

public class Task {
    private Long taskId;
    private String taskName;
    private Timestamp lastStartTime;
    private Timestamp lastEndTime;
    private String lastHost;
    private Timestamp nextScheduledTime;
    private String taskEx;
    private Integer executionNum;

    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Timestamp getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(Timestamp lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    public Timestamp getLastEndTime() {
        return lastEndTime;
    }

    public void setLastEndTime(Timestamp lastEndTime) {
        this.lastEndTime = lastEndTime;
    }

    public String getLastHost() {
        return lastHost;
    }

    public void setLastHost(String lastHost) {
        this.lastHost = lastHost;
    }

    public Timestamp getNextScheduledTime() {
        return nextScheduledTime;
    }

    public void setNextScheduledTime(Timestamp nextScheduledTime) {
        this.nextScheduledTime = nextScheduledTime;
    }

    public String getTaskEx() {
        return taskEx;
    }

    public void setTaskEx(String taskEx) {
        this.taskEx = taskEx;
    }

    public Integer getExecutionNum() {
        return executionNum;
    }

    public void setExecutionNum(Integer executionNum) {
        this.executionNum = executionNum;
    }
}
