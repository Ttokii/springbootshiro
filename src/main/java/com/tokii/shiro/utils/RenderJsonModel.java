package com.tokii.shiro.utils;

import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RenderJsonModel extends MappingJackson2JsonView {
	
	/**
	 * 将Map<String,Object>对象转换为json并输出到前端
	 * @param model 需要返回前端的数据模型
	 * @param response HttpServletResponse
	 */
	public void renderToFg(Map<String, Object> model,HttpServletRequest request,HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		try {
			this.renderMergedOutputModel(model,request,response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
