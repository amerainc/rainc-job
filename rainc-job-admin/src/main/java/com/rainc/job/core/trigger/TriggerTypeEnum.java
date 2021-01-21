package com.rainc.job.core.trigger;

public enum TriggerTypeEnum {
    /**
     * 手动触发
     */
    MANUAL("手动触发"),
    /**
     * cron触发
     */
    CRON("cron触发"),
    /**
     * 失败重新触发
     */
    RETRY("失败重新触发");

    private TriggerTypeEnum(String title) {
        this.title = title;
    }

    private final String title;

    public String getTitle() {
        return title;
    }

}
