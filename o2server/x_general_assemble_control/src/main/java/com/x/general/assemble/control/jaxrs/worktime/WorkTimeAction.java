package com.x.general.assemble.control.jaxrs.worktime;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.x.base.core.project.annotation.JaxrsDescribe;
import com.x.base.core.project.annotation.JaxrsMethodDescribe;
import com.x.base.core.project.annotation.JaxrsParameterDescribe;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.http.HttpMediaType;
import com.x.base.core.project.jaxrs.ResponseFactory;
import com.x.base.core.project.jaxrs.StandardJaxrsAction;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;

@Path("worktime")
@JaxrsDescribe("工作时间")
public class WorkTimeAction extends StandardJaxrsAction {

	private static Logger logger = LoggerFactory.getLogger(WorkTimeAction.class);

	@JaxrsMethodDescribe(value = "计算开始时间和结束时间之间的工作时间间隔(分钟).", action = ActionBetweenMinutes.class)
	@GET
	@Path("betweenminutes/start/{start}/end/{end}")
	@Produces(HttpMediaType.APPLICATION_JSON_UTF_8)
	@Consumes(MediaType.APPLICATION_JSON)
	public void betweenMinutes(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request,
			@JaxrsParameterDescribe("开始时间") @PathParam("start") String start,
			@JaxrsParameterDescribe("结束时间") @PathParam("end") String end) {
		ActionResult<ActionBetweenMinutes.Wo> result = new ActionResult<>();
		EffectivePerson effectivePerson = this.effectivePerson(request);
		try {
			result = new ActionBetweenMinutes().execute(effectivePerson, start, end);
		} catch (Exception e) {
			logger.error(e, effectivePerson, request, null);
			result.error(e);
		}
		asyncResponse.resume(ResponseFactory.getEntityTagActionResultResponse(request, result));
	}

	@JaxrsMethodDescribe(value = "计算开始时间前进指定分钟数后的工作时间.", action = ActionForwardMinutes.class)
	@GET
	@Path("forwardminutes/start/{start}/minutes/{minutes}")
	@Produces(HttpMediaType.APPLICATION_JSON_UTF_8)
	@Consumes(MediaType.APPLICATION_JSON)
	public void forwardMinutes(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request,
			@JaxrsParameterDescribe("开始时间") @PathParam("start") String start,
			@JaxrsParameterDescribe("前进分钟数") @PathParam("minutes") int minutes) {
		ActionResult<ActionForwardMinutes.Wo> result = new ActionResult<>();
		EffectivePerson effectivePerson = this.effectivePerson(request);
		try {
			result = new ActionForwardMinutes().execute(effectivePerson, start, minutes);
		} catch (Exception e) {
			logger.error(e, effectivePerson, request, null);
			result.error(e);
		}
		asyncResponse.resume(ResponseFactory.getEntityTagActionResultResponse(request, result));
	}

	@JaxrsMethodDescribe(value = "返回一个工作日的工作分钟数.", action = ActionMinutesOfWorkDay.class)
	@GET
	@Path("minutesofworkday")
	@Produces(HttpMediaType.APPLICATION_JSON_UTF_8)
	@Consumes(MediaType.APPLICATION_JSON)
	public void minutesOfWorkDay(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request) {
		ActionResult<ActionMinutesOfWorkDay.Wo> result = new ActionResult<>();
		EffectivePerson effectivePerson = this.effectivePerson(request);
		try {
			result = new ActionMinutesOfWorkDay().execute(effectivePerson);
		} catch (Exception e) {
			logger.error(e, effectivePerson, request, null);
			result.error(e);
		}
		asyncResponse.resume(ResponseFactory.getEntityTagActionResultResponse(request, result));
	}

	@JaxrsMethodDescribe(value = "返回指定时间是否是工作时间.", action = ActionIsWorkTime.class)
	@GET
	@Path("isworktime/date/{date}")
	@Produces(HttpMediaType.APPLICATION_JSON_UTF_8)
	@Consumes(MediaType.APPLICATION_JSON)
	public void isWorkTime(@Suspended final AsyncResponse asyncResponse, @Context HttpServletRequest request,
			@JaxrsParameterDescribe("指定时间") @PathParam("date") String date) {
		ActionResult<ActionIsWorkTime.Wo> result = new ActionResult<>();
		EffectivePerson effectivePerson = this.effectivePerson(request);
		try {
			result = new ActionIsWorkTime().execute(effectivePerson, date);
		} catch (Exception e) {
			logger.error(e, effectivePerson, request, null);
			result.error(e);
		}
		asyncResponse.resume(ResponseFactory.getEntityTagActionResultResponse(request, result));
	}

}