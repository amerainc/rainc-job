package com.rainc.job.controller;

import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.enums.ExecutorBlockStrategyEnum;
import com.rainc.job.core.thread.JobTriggerPoolHelper;
import com.rainc.job.core.trigger.TriggerTypeEnum;
import com.rainc.job.model.JobInfoDO;
import com.rainc.job.core.router.ExecutorRouteStrategyEnum;
import com.rainc.job.service.JobInfoService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author rainc
 * @create 2020/12/25 10:36
 */
@RestController
@RequestMapping("/jobinfo")
public class JobInfoController {
    @Resource
    JobInfoService jobInfoService;

    @GetMapping("/list")
    public ReturnT<Page<JobInfoDO>> list(@RequestParam(required = false, defaultValue = "1") int page,
                                         @RequestParam(required = false, defaultValue = "10") int size,
                                         long jobGroup,
                                         int triggerStatus,
                                         String jobDesc,
                                         String executorHandler,
                                         String author) {
        //jpa分页默认从0开始
        if (page > 0) {
            page--;
        }
        Page<JobInfoDO> pageList = jobInfoService.pageList(page, size, jobGroup, triggerStatus, jobDesc, executorHandler, author);
        return new ReturnT<>(pageList);
    }

    @GetMapping("/")
    public ReturnT<List<JobInfoDO>> list(@RequestParam(required = false, defaultValue = "-1") long jobGroup) {
        return new ReturnT<>(jobInfoService.all(jobGroup));
    }


    @RequestMapping(value = "/", method = {RequestMethod.PUT, RequestMethod.POST})
    public ReturnT<String> save(@Validated @RequestBody JobInfoDO jobInfoDO) {
        String jobId = jobInfoService.save(jobInfoDO);
        return new ReturnT<>(jobId);
    }

    @GetMapping("/count")
    public ReturnT<Long> count() {
        return new ReturnT<>(jobInfoService.count());
    }

    @DeleteMapping("/{id}")
    public ReturnT<String> delete(@PathVariable long id) {
        jobInfoService.delete(id);
        return ReturnT.SUCCESS;
    }

    @GetMapping("/start/{id}")
    public ReturnT<String> start(@PathVariable long id) {
        jobInfoService.start(id);
        return ReturnT.SUCCESS;
    }

    @GetMapping("/stop/{id}")
    public ReturnT<String> pause(@PathVariable long id) {
        jobInfoService.stop(id);
        return ReturnT.SUCCESS;
    }

    @GetMapping("/trigger/{id}")
    public ReturnT<String> trigger(@PathVariable long id) {
        JobTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, null, null);
        return ReturnT.SUCCESS;
    }

    @GetMapping("/block_strategy")
    public ReturnT<Map<String, String>> executorBlockStrategy() {
        return new ReturnT<>(Arrays.stream(ExecutorBlockStrategyEnum.values()).
                collect(Collectors.toMap(Enum::name, ExecutorBlockStrategyEnum::getTitle)));
    }

    @GetMapping("/router_strategy")
    public ReturnT<Map<String, String>> executorRouteStrategy() {
        return new ReturnT<>(Arrays.stream(ExecutorRouteStrategyEnum.values()).
                collect(Collectors.toMap(Enum::name, ExecutorRouteStrategyEnum::getTitle)));
    }

    @GetMapping("/handlers/{groupId}")
    public ReturnT<List<String>> handlers(@PathVariable long groupId) {
        return new ReturnT<>(jobInfoService.handlers(groupId));
    }


}
