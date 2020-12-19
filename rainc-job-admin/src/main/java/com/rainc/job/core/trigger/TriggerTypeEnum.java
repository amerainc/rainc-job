package com.rainc.job.core.trigger;

public enum TriggerTypeEnum {
    /**
     * 手动触发
     */
    MANUAL("Manual trigger"),
    /**
     * cron触发
     */
    CRON("Cron trigger"),
    /**
     * 失败重新触发
     */
    RETRY("Fail retry trigger");

    private TriggerTypeEnum(String title) {
        this.title = title;
    }

    private final String title;

    public String getTitle() {
        return title;
    }

}