package com.rainc.job.controller;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.model.AppInfo;
import com.rainc.job.core.model.ExecutorInfo;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.model.JobGroupDO;
import com.rainc.job.model.JobUserDO;
import com.rainc.job.service.JobGroupService;
import com.rainc.job.util.QueryUtil;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author rainc
 * @create 2020/12/26 18:21
 */
@RestController
@RequestMapping("/jobgroup")
public class JobGroupController {
    @Resource
    JobGroupService jobGroupService;

    @GetMapping("/list")
    public ReturnT<Page<JobGroupDO>> list(@RequestParam(required = false, defaultValue = "1") int page,
                                          @RequestParam(required = false, defaultValue = "10") int size,
                                          @RequestParam(required = false, defaultValue = "") String appName,
                                          @RequestParam(required = false, defaultValue = "") String title) {
        //jpa分页默认从0开始
        if (page > 0) {
            page--;
        }
        Page<JobGroupDO> pageList = jobGroupService.pageList(QueryUtil.castToLike(appName), QueryUtil.castToLike(title), page, size);
        return new ReturnT<>(pageList);
    }

    @GetMapping("/")
    public ReturnT<List<JobGroupDO>> list(@RequestAttribute JobUserDO jobUserDO) {
        //jpa分页默认从0开始
        List<JobGroupDO> allList = jobGroupService.all();
        List<JobGroupDO> list = filterGroupByRole(allList, jobUserDO);
        return new ReturnT<>(list);
    }

    @RequestMapping(value = "/", method = {RequestMethod.PUT, RequestMethod.POST})
    public ReturnT<String> save(@RequestBody JobGroupDO jobGroupDO) {
        return jobGroupService.save(jobGroupDO);
    }

    @DeleteMapping("/{id}")
    public ReturnT<String> delete(@PathVariable long id) {

        return jobGroupService.delete(id);
    }

    @GetMapping("/appname")
    public ReturnT<List<String>> appNameList() {
        return new ReturnT<>(RaincJobScheduler.getAllAppInfo()
                .stream()
                .map((AppInfo::getAppName))
                .collect(Collectors.toList()));
    }

    @GetMapping("/executor/count")
    public ReturnT<Integer> executorCount() {
        return new ReturnT<>(jobGroupService.executorCount());
    }

    @GetMapping("/address/{appName}")
    public ReturnT<List<String>> addressList(@PathVariable String appName) {
        return new ReturnT<>(RaincJobScheduler
                .getAppInfo(appName)
                .getAddressMap()
                .values()
                .stream()
                .map(ExecutorInfo::getAddress)
                .collect(Collectors.toList()));
    }

    /**
     * 通过权限过滤执行器列表
     *
     * @param allList
     * @param jobUserDO
     * @return
     */
    private List<JobGroupDO> filterGroupByRole(List<JobGroupDO> allList, JobUserDO jobUserDO) {
        if (jobUserDO.getRole() == 1) {
            return allList;
        } else {
            List<String> permissions = Arrays.asList(jobUserDO.getPermission().trim().split(","));
            return allList.stream()
                    .filter((jobGroupDO -> permissions.contains(jobGroupDO.getId().toString())))
                    .collect(Collectors.toList());
        }
    }
}
