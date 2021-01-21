package com.rainc.job.service.impl;

import cn.hutool.core.util.StrUtil;
import com.rainc.job.core.biz.AdminBiz;
import com.rainc.job.core.biz.model.HandleCallbackParam;
import com.rainc.job.core.biz.model.RegistryParam;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.scheduler.RaincJobScheduler;
import com.rainc.job.exception.RaincJobException;
import com.rainc.job.model.JobLogDO;
import com.rainc.job.model.JobRegistryDO;
import com.rainc.job.respository.JobLogRepository;
import com.rainc.job.respository.JobRegistryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author rainc
 * @create 2020/12/11 21:43
 */
@Service
@Log4j2
public class AdminBizImpl implements AdminBiz {
    @Resource
    JobLogRepository jobLogRepository;
    @Resource
    JobRegistryRepository jobRegistryRepository;

    /**
     * 注册执行器具体实现
     *
     * @param registryParam 注册参数
     * @return
     */
    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        //如果注册地址或appName为空则注册失败
        if (!StrUtil.isAllNotBlank(registryParam.getAddress(), registryParam.getAppName())) {
            throw new RaincJobException("非法参数");
        }
        //查找是否有注册信息
        JobRegistryDO jobRegistryDO = jobRegistryRepository.findByAddress(registryParam.getAddress())
                .orElseGet(() -> JobRegistryDO.builder()
                        .address(registryParam.getAddress())
                        .appName(registryParam.getAppName())
                        .build()
                );
        //更新更新时间
        jobRegistryDO.setUpdateTime(new Date());
        //存储至数据库
        jobRegistryRepository.save(jobRegistryDO);
        //存储注册信息至appInfoRepository
        RaincJobScheduler.registerExecutor(registryParam.getAppName(), registryParam.getAddress(), true);
        return ReturnT.SUCCESS;
    }

    /**
     * 移除注册参数
     *
     * @param registryParam 注册参数
     * @return
     */
    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        if (!StrUtil.isAllNotBlank(registryParam.getAddress(), registryParam.getAppName())) {
            throw new RaincJobException("非法参数");
        }
        //从数据库中移除
        jobRegistryRepository.deleteByAddress(registryParam.getAddress());
        //从appInfoRepository中移除该执行器
        RaincJobScheduler.removeExecutor(registryParam.getAppName(), registryParam.getAddress());
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        for (HandleCallbackParam handleCallbackParam : callbackParamList) {
            ReturnT<String> callbackResult = callback(handleCallbackParam);
            log.debug(">>>>>>>>> JobApiController.callback {}, handleCallbackParam={}, callbackResult={}",
                    (callbackResult.getCode() == ReturnT.SUCCESS.getCode() ? "success" : "fail"), handleCallbackParam, callbackResult);
        }

        return ReturnT.SUCCESS;
    }

    /**
     * 回调任务
     *
     * @param handleCallbackParam 回调参数
     * @return
     */
    private ReturnT<String> callback(HandleCallbackParam handleCallbackParam) {
        // 查询日志
        JobLogDO jobLogDO = jobLogRepository.findById(handleCallbackParam.getLogId())
                .orElseThrow(() -> new RaincJobException("没有找到该日志"));
        if (jobLogDO.getHandleCode() > 0) {
            //避免重复回调
            return new ReturnT<>(ReturnT.FAIL_CODE, "log repeate callback.");
        }

        // handle msg
        StringBuffer handleMsg = new StringBuffer();
        if (jobLogDO.getHandleMsg() != null) {
            handleMsg.append(jobLogDO.getHandleMsg()).append("<br>");
        }
        if (handleCallbackParam.getExecuteResult().getMsg() != null) {
            handleMsg.append(handleCallbackParam.getExecuteResult().getMsg());
        }

        if (handleMsg.length() > 15000) {
            handleMsg = new StringBuffer(handleMsg.substring(0, 15000));  // text最大64kb 避免长度过长
        }

        // 成功，保存日志
        jobLogDO.setHandleTime(new Date());
        jobLogDO.setHandleCode(handleCallbackParam.getExecuteResult().getCode());
        jobLogDO.setHandleMsg(handleMsg.toString());
        jobLogRepository.upDateRecallJobLog(jobLogDO.getId(), new Date(), handleCallbackParam.getExecuteResult().getCode(), handleMsg.toString());
        return ReturnT.SUCCESS;
    }
}
