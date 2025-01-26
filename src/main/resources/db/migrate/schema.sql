create schema if not exists dev_distributed_task;

drop table if exists dev_distributed_task.task;
create table dev_distributed_task.task(
    task_id serial primary key,
    task_name varchar(50),
    last_start_time timestamp,
    last_end_time timestamp,
    last_host varchar(20),
    next_scheduled_time timestamp,
    task_ex varchar(20),
    execution_num int
);

comment on table dev_distributed_task.task is '分布式作业';
comment on column dev_distributed_task.task.task_id is '自增主键';
comment on column dev_distributed_task.task.task_name is '任务名称';
comment on column dev_distributed_task.task.last_start_time is '上次执行开始时间';
comment on column dev_distributed_task.task.last_end_time is '上次执行结束时间';
comment on column dev_distributed_task.task.last_host is '上次执行主机';
comment on column dev_distributed_task.task.next_scheduled_time is '下次执行时间';