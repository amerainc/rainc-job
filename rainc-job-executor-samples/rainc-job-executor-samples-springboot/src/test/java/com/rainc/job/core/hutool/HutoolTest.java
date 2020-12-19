package com.rainc.job.core.hutool;

import cn.hutool.core.net.NetUtil;
import org.junit.Test;

/**
 * @Author rainc
 * @create 2020/12/12 14:01
 */
public class HutoolTest {
    @Test
    public void addressTest(){
        System.out.println(NetUtil.getLocalhostStr());
    }
}
