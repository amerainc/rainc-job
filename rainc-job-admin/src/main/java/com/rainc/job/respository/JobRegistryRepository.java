package com.rainc.job.respository;

import com.rainc.job.model.JobRegistryDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author rainc
 * @create 2020/12/21 21:46
 */
public interface JobRegistryRepository extends JpaRepository<JobRegistryDO, Long> {
    Optional<JobRegistryDO> findByAddress(String address);

    @Transactional
    @Modifying
    int deleteAllByUpdateTimeBefore(Date date);

    @Transactional
    @Modifying
    int deleteByAddress(String address);

}
