package com.rainc.job.core.model;

import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.model.JobGroupDO;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分组信息
 *
 * @Author rainc
 * @create 2020/12/13 21:14
 */
@Data
public class GroupInfo {
    private Long id;
    /**
     * 应用名称 与appInfo一致
     */
    private String appName;
    /**
     * 自定义命名
     */
    private String title;

    /**
     * 是否是自动注册
     */
    private boolean isAuto;

    /**
     * 执行器列表，有则手动，空则自动注册
     */
    private List<ExecutorInfo> executorList;

    public static GroupInfo castToInfo(JobGroupDO jobGroupDO) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(jobGroupDO.getId());
        groupInfo.setTitle(jobGroupDO.getTitle());
        groupInfo.setAppName(jobGroupDO.getAppName());
        //如果执行器列表非空，则转换
        if (jobGroupDO.getAddressList() != null && jobGroupDO.getAddressList().trim().length() > 0) {
            List<ExecutorInfo> list = Arrays.stream(jobGroupDO.getAddressList().split(","))
                    .map((s) -> RaincJobScheduler.getExecutor(groupInfo.getAppName(), s)).collect(Collectors.toList());
            groupInfo.setExecutorList(list);
            groupInfo.setAuto(false);
        } else {
            groupInfo.setAuto(true);
        }
        return groupInfo;
    }
}
