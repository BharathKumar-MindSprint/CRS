package com.olam.digital.ofis.model;

public class TasksDTO {
    private String taskName;
    private String taskDate;
    private String taskCreator;
    private String taskCompletedStatus;
    private int taskId;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
    }

    public String getTaskCreator() {
        return taskCreator;
    }

    public void setTaskCreator(String taskCreator) {
        this.taskCreator = taskCreator;
    }

    public String getTaskCompletedStatus() {
        return taskCompletedStatus;
    }

    public void setTaskCompletedStatus(String taskCompletedStatus) {
        this.taskCompletedStatus = taskCompletedStatus;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }
}
