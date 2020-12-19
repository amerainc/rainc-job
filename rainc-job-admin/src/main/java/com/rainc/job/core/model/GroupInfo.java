package com.rainc.job.core.model;

import com.rainc.job.model.JobGroupDO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
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
     * 执行器列表，有则手动，空则自动注册
     */
    private List<String> addressList;

    public static GroupInfo castToInfo(JobGroupDO jobGroupDO) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(jobGroupDO.getId());
        groupInfo.setTitle(jobGroupDO.getTitle());
        groupInfo.setAppName(jobGroupDO.getAppName());
        if (jobGroupDO.getAddressList() != null && jobGroupDO.getAddressList().trim().length() > 0) {
            groupInfo.setAddressList(new ArrayList<String>(Arrays.asList(jobGroupDO.getAddressList().split(","))));
        }
        return groupInfo;
    }
}
