package com.rainc.job.core.biz;

import com.rainc.job.core.biz.factory.BizFactory;
import com.rainc.job.core.biz.model.RegistryParam;
import org.junit.Test;

/**
 * @Author rainc
 * @create 2020/12/12 12:02
 */
public class AdminBizTest {
    @Test
    public void testRegister() {
        AdminBiz adminBiz = BizFactory.createBiz("http://localhost:8081/rainc-job-admin/", null, AdminBiz.class);
        adminBiz.registry(new RegistryParam("aaa", "sasdas"));
    }
}
