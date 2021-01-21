package com.rainc.job.respository;

import com.rainc.job.model.JobLogDO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author rainc
 * @create 2020/12/19 18:15
 */
@Repository
public interface JobLogRepository extends JpaRepository<JobLogDO, Long>, JpaSpecificationExecutor<JobLogDO> {
    /**
     * 找到触发或执行失败，并且还未告警的消息
     *
     * @param pageable
     * @return
     */
    @Query(value = "select id from JobLogDO where not ((triggerCode in (0,200) and handleCode = 0) or (handleCode=200)) and alarmStatus=0 ORDER BY id ASC ")
    List<Long> findFailJobLogIds(Pageable pageable);

    /**
     * 查询列表
     *
     * @param startTime
     * @param endTime
     * @param example
     * @param pageable
     * @return
     */
    Page<JobLogDO> findAllByTriggerTimeBetween(Date startTime, Date endTime, Example<JobLogDO> example, Pageable pageable);

    /**
     * 更新执行器回调信息
     *
     * @param id
     * @param handleTime
     * @param handlerCode
     * @param handlerMsg
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update JobLogDO set handleTime=?2,handleCode=?3,handleMsg=?4 where id=?1")
    int upDateRecallJobLog(long id, Date handleTime, int handlerCode, String handlerMsg);


    /**
     * 更新触发信息
     *
     * @param id
     * @param executorAddress
     * @param executorHandler
     * @param executorParam
     * @param executorShardingParam
     * @param executorFailRetryCount
     * @param triggerTime
     * @param triggerCode
     * @param triggerMsg
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update JobLogDO set executorAddress=?2,executorHandler=?3,executorParam=?4,executorShardingParam=?5,executorFailRetryCount=?6,triggerTime=?7,triggerCode=?8,triggerMsg=?9 where id=?1")
    int upDateTriggerJobLog(long id, String executorAddress, String executorHandler, String executorParam, String executorShardingParam, int executorFailRetryCount, Date triggerTime, int triggerCode, String triggerMsg);

    /**
     * cas锁，锁日志
     *
     * @param id
     * @param oldAlarmStatus
     * @param newAlarmStatus
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update JobLogDO set alarmStatus=?3 where id=?1 and alarmStatus=?2")
    int updateAlarmStatus(long id, int oldAlarmStatus, int newAlarmStatus);

    void deleteAllByJobId(long id);


    @Query(value = "SELECT COUNT(handleCode) as triggerDayCount," +
            "SUM(CASE WHEN (triggerCode in (0, 200) and handleCode = 0) then 1 else 0 end) as triggerDayCountRunning," +
            "SUM(CASE WHEN handleCode = 200 then 1 else 0 end) as triggerDayCountSuc " +
            "FROM JobLogDO " +
            "WHERE triggerTime BETWEEN ?1 and ?2")
    Map<String, Long> findLogReport(Date from, Date to);
}
