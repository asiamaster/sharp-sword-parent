package com.dili.ss.activiti.rpc;

import com.dili.ss.activiti.domain.ActForm;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.retrofitful.annotation.*;

@Restful("${bpmc.contextPath}")
public interface BpmcFormRpc {

	/**
	 * 根据表单key查询ActForm
	 * @param formKey   表单key，必填
	 * @return ActForm
	 */
	@GET("/api/form/getByKey")
    BaseOutput<ActForm> getByKey(@ReqParam(value = "formKey") String formKey);

	/**
	 * 新增ActForm
	 * @param actForm   表单, 必填
	 * @return 影响行数
	 */
	@POST("/api/form/insert")
    BaseOutput<Integer> insert(@VOBody ActForm actForm);

	/**
	 * 根据key修改ActForm
	 * @param actForm   表单, 必填
	 * @return 影响行数
	 */
	@POST("/api/form/updateByKey")
    BaseOutput<Integer> updateByKey(@VOBody ActForm actForm);

}
