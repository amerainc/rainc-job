package com.rainc.job.respository;

import com.rainc.job.model.JobInfoDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/13 13:24
 */
@Repository
public interface JobInfoRepository extends JpaRepository<JobInfoDO, Long> {
    List<JobInfoDO> findAllByTriggerNextTimeIsLessThan(long maxNextTime);
}
