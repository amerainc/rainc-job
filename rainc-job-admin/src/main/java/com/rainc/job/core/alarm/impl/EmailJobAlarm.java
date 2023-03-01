package com.rainc.job.core.alarm.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.rainc.job.core.alarm.JobAlarm;
import com.rainc.job.core.biz.model.ReturnT;
import com.rainc.job.core.config.RaincJobAdminConfig;
import com.rainc.job.core.constant.JobLogPrefix;
import com.rainc.job.model.JobGroupDO;
import com.rainc.job.model.JobInfoDO;
import com.rainc.job.model.JobLogDO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 邮件告警
 *
 * @Author rainc
 * @create 2020/12/23 10:13
 */
@Component
@Log4j2
public class EmailJobAlarm implements JobAlarm {
    @Resource
    MailProperties mailProperties;

    /**
     * 失败告警
     *
     * @param info
     * @param jobLog
     * @return
     */
    @Override
    public boolean doAlarm(JobInfoDO info, JobLogDO jobLog) {
        boolean alarmResult = true;

        // 发送监控邮件
        if (info != null && info.getAlarmEmail() != null && info.getAlarmEmail().trim().length() > 0) {

            // 告警信息
            StringBuilder alarmContent = new StringBuilder();
            alarmContent.append("<br>Alarm Job LogId=");
            alarmContent.append(jobLog.getId());
            if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
                alarmContent.append( "<br><h5>TriggerMsg</h5>");
                alarmContent.append(jobLog.getTriggerMsg());
            }
            if (jobLog.getHandleCode() > 0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
                alarmContent.append("<br>HandleCode=");
                alarmContent.append(jobLog.getHandleMsg());
            }

            // 邮件信息
            Optional<JobGroupDO> optionalJobGroupDO = RaincJobAdminConfig.getAdminConfig().getJobGroupRepository().findById(info.getJobGroup());

            String personal = "rainc-job";
            String title = "任务调度中心监控报警";
            String content = MessageFormat.format(loadEmailJobAlarmTemplate(),
                    optionalJobGroupDO.isPresent() ? optionalJobGroupDO.get().getTitle() : "null",
                    info.getId(),
                    info.getJobDesc(),
                    alarmContent.toString());

            Set<String> emailSet = new HashSet<String>(Arrays.asList(info.getAlarmEmail().split(",")));
            for (String email : emailSet) {

                // 生成邮件
                try {
                    MimeMessage mimeMessage = RaincJobAdminConfig.getAdminConfig().getMailSender().createMimeMessage();

                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                    helper.setFrom(mailProperties.getUsername(), personal);
                    helper.setTo(email);
                    helper.setSubject(title);
                    helper.setText(content, true);
                    RaincJobAdminConfig.getAdminConfig().getMailSender().send(mimeMessage);
                } catch (Exception e) {
                    log.error(JobLogPrefix.PREFIX + "任务失败告警邮件发送失败, JobLogId:{}", jobLog.getId(), e);

                    alarmResult = false;
                }

            }
        }

        return alarmResult;
    }


    /**
     * 邮件模板
     *
     * @return
     */
    private String loadEmailJobAlarmTemplate()  {
        try ( InputStream inputStream=  EmailJobAlarm.class.getResourceAsStream("/template/alarmTemplate.html")){
           return IoUtil.readUtf8(inputStream);
        } catch (Exception e) {
            return "";
        }
    }
}

