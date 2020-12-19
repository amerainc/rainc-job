package com.rainc.job.core.biz;

import com.rainc.job.core.biz.factory.AdminBizFactory;
import com.rainc.job.core.biz.model.RegistryParam;
import org.junit.Test;

/**
 * @Author rainc
 * @create 2020/12/12 12:02
 */
public class AdminBizTest {
    @Test
    public void testRegister() {
        AdminBiz adminBiz = AdminBizFactory.createAdminBiz("http://localhost:8081/rainc-job-admin/", null);
        adminBiz.registry(new RegistryParam("aaa", "sasdas"));
    }
}
