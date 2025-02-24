/*
 * Copyright (c) 杭州橙浩科技有限公司. 2022-2023. All rights reserved.
 */

package com.xxl.job.admin.controller;

import cn.orange.common.json.utils.JsonUtils;
import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.xxl.job.admin.constant.JobConstants.*;

@Controller
@RequestMapping("/jobApi")
public class JobLocalApiController {

    @Resource
    private XxlJobService xxlJobService;

    /**
     * api
     *
     * @param uri
     * @param data
     * @return
     */
    @RequestMapping("/{uri}")
    @ResponseBody
    @PermissionLimit(limit=false)
    public ReturnT<String> api(HttpServletRequest request, @PathVariable("uri") String uri,
                               @RequestBody(required = false) String data) {

        // valid
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, HttpMethod not support.");
        }
        if (uri == null || uri.trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping empty.");
        }
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
            && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
            && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN))) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "The access token is wrong.");
        }
        XxlJobInfo jobInfo = JsonUtils.parseObject(data, XxlJobInfo.class);
        // services mapping
        if (ADD.equals(uri)) {
            return xxlJobService.add(jobInfo, null);
        } else if (UPDATE.equals(uri)) {
            return xxlJobService.update(jobInfo, null);
        } else if (REMOVE.equals(uri)) {
            return xxlJobService.remove(jobInfo.getId());
        } else if (STOP.equals(uri)) {
            return xxlJobService.stop(jobInfo.getId());
        } else if (START.equals(uri)) {
            return xxlJobService.start(jobInfo.getId());
        } else if (TRIGGER.equals(uri)) {
            JobTriggerPoolHelper.trigger(jobInfo.getId(), TriggerTypeEnum.MANUAL, -1, null, jobInfo.getExecutorParam(), null);
            return ReturnT.SUCCESS;
        } else {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping(" + uri + ") not found.");
        }
    }

}
