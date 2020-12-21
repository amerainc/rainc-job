package com.rainc.job.respository;

import com.rainc.job.model.JobLogDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author rainc
 * @create 2020/12/19 18:15
 */
@Repository
public interface JobLogRepository extends JpaRepository<JobLogDO, Long> {

}
