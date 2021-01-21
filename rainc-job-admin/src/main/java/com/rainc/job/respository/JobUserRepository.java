package com.rainc.job.respository;

import com.rainc.job.model.JobUserDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @Author rainc
 * @create 2020/12/23 21:47
 */
@Repository
public interface JobUserRepository extends JpaRepository<JobUserDO, Long> {
    Optional<JobUserDO> findByUsername(String username);
    Page<JobUserDO> findAllByUsernameLike(String username, Pageable pageable);
    Page<JobUserDO> findAllByUsernameLikeAndRole(String username,int role, Pageable pageable);
}
