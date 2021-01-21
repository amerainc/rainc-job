package com.rainc.job.respository;

import com.rainc.job.model.JobLogReportDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author rainc
 * @create 2021/1/2 18:58
 */
public interface JobLogReportRepository extends JpaRepository<JobLogReportDO, Long> {
    Optional<JobLogReportDO> findByTriggerDay(Date triggerDay);

    @Query(value = "SELECT SUM(runningCount) as runningCount," +
            "SUM(sucCount) as sucCount, " +
            "SUM(failCount) as failCount " +
            "FROM JobLogReportDO ")
    Map<String, Long> findLogReportTotal();

    List<JobLogReportDO> findAllByTriggerDayBetweenOrderByTriggerDayAsc(Date startDate, Date endDate);
}
