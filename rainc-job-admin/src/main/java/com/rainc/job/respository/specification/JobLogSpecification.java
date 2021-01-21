package com.rainc.job.respository.specification;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.model.JobLogDO;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author rainc
 * @create 2021/1/2 19:41
 */
public class JobLogSpecification {
    /**
     * 查询规则
     *
     * @param jobGroup  -1查询所有
     * @param jobId     -1查询所有
     * @param logStatus -1查询所有 0 成功 1失败 2进行中
     * @return
     */
    public static Specification<JobLogDO> pageListSpec(final long jobGroup, final long jobId, final int logStatus, final String filterTime) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //执行器查找
            if (jobGroup > 0) {
                predicateList.add(criteriaBuilder.equal(root.get("jobGroup"), jobGroup));
            }
            //任务id查找
            if (jobId > 0) {
                predicateList.add(criteriaBuilder.equal(root.get("jobId"), jobId));
            }
            //任务状态
            switch (logStatus) {
                case 0://成功
                    predicateList.add(criteriaBuilder.equal(root.get("handleCode"), 200));
                    break;
                case 1://失败
                    Predicate handleCode = criteriaBuilder.not(criteriaBuilder.in(root.get("handleCode"))
                            .value(200)
                            .value(0));
                    Predicate triggerCode = criteriaBuilder.not(criteriaBuilder.in(root.get("triggerCode"))
                            .value(200)
                            .value(0));
                    predicateList.add(criteriaBuilder.or(handleCode, triggerCode));
                    break;
                case 2://进行中
                    predicateList.add(criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("triggerCode"), 200),
                            criteriaBuilder.equal(root.get("handleCode"), 0)));
                    break;
                default:
                    break;
            }
            if (StrUtil.isNotBlank(filterTime)) {
                String[] temp = filterTime.split("-");
                if (temp.length == 2) {
                    Date triggerTimeStart = new Date(Long.parseLong(temp[0]));
                    Date triggerTimeEnd = new Date(Long.parseLong(temp[1]));
                    predicateList.add(criteriaBuilder.between(root.get("triggerTime"), triggerTimeStart, triggerTimeEnd));
                }
                //排序
            }
            Order order = criteriaBuilder.desc(root.get("triggerTime"));
            return criteriaQuery.orderBy(order).where(predicateList.toArray(predicateList.toArray(new Predicate[0]))).getRestriction();
        };
    }

    public static Specification<JobLogDO> findClearLogIdsSpec(final long jobGroup, final long jobId, final Date clearBeforeTime, final int clearBeforeNum) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (jobGroup > 0) {
                predicateList.add(criteriaBuilder.equal(root.get("jobGroup"), jobGroup));
            }
            if (jobId > 0) {
                predicateList.add(criteriaBuilder.equal(root.get("jobId"), jobId));
            }
            if (clearBeforeTime != null) {
                predicateList.add(criteriaBuilder.lessThan(root.get("triggerTime"), clearBeforeTime));
            }
            if (clearBeforeNum > 0) {
                predicateList.add(criteriaBuilder.not(criteriaBuilder.in(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))));
            }
            Order order = criteriaBuilder.desc(root.get("triggerTime"));
            return criteriaQuery.orderBy(order).where(predicateList.toArray(new Predicate[0])).getRestriction();
        };
    }
}
