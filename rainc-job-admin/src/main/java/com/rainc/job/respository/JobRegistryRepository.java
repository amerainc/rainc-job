package com.rainc.job.respository;

import com.rainc.job.model.JobRegistryDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/21 21:46
 */
public interface JobRegistryRepository extends JpaRepository<JobRegistryDO, Long> {
    JobRegistryDO findByAddress(String address);

    List<JobRegistryDO> findAllByUpdateTimeBefore(Date date);

    int deleteByAddress(String address);
}
