package com.rainc.job.respository;

import com.rainc.job.model.JobGroupDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/13 11:04
 */
@Repository
public interface JobGroupRepository extends JpaRepository<JobGroupDO, Long> {
}
