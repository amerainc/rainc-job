package com.rainc.job.respository;

import com.rainc.job.model.JobInfoDO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/13 13:24
 */
@Repository
public interface JobInfoRepository extends JpaRepository<JobInfoDO, Long> {
    /**
     * 查询小于最大时间的任务,且运行状态为执行的任务
     *
     * @param maxNextTime 最大时间
     * @param pageable    分页
     * @return page
     */
    List<JobInfoDO> findAllByTriggerNextTimeIsLessThanAndTriggerStatusTrue(long maxNextTime, Pageable pageable);

    /**
     * cas锁，锁本次任务
     *
     * @param jobId              任务id
     * @param oldTriggerNextTime 旧触发时间
     * @param newTriggerNextTime 新触发时间
     * @return
     */
    @Modifying
    @Transactional
    @Query(value = "update JobInfoDO set triggerNextTime=?3,triggerLastTime=?2 where triggerNextTime=?2 and id=?1")
    int upDateNextTriggerTime(long jobId, long oldTriggerNextTime, long newTriggerNextTime);

    List<JobInfoDO> findAllByJobGroup(long jobGroup);

}
