package com.tokii.shiro.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 异步渲染工具类
 */
public class AsyncRenderUtils {
	private static final Logger log = LoggerFactory.getLogger(AsyncRenderUtils.class);

	/**
	 * 判断该请求是否为异步请求
	 * @param request 待判断的请求
	 * @return true/false --> 是/否
	 */
	public static boolean isAsyncRequest(HttpServletRequest request) {
		return !StringUtils.isBlank(request.getHeader("x-requested-with")) && request.getHeader("x-requested-with").equals("XMLHttpRequest");
	}

	/**
	 * 渲染异步请求到的数据至前端
	 * @param model 待渲染的数据
	 * @param request
	 * @param response
	 */
	public static void asyncRender(Map<String, Object> model,HttpServletRequest request,HttpServletResponse response) {
		RenderJsonModel rjm = new RenderJsonModel();
		try {
			rjm.renderToFg(model, request, response);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("asyncRender exception ...");
		}
	}
	
    
}
