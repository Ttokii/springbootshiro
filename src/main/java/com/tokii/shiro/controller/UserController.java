package com.tokii.shiro.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    @ResponseBody
    @RequestMapping(value="/login")
    public Map<String,Object> login(@RequestParam("username") String username,
                                    @RequestParam("pwd") String pwd){
        Map<String,Object> resMap = new HashMap<>();
        Subject currentUser = SecurityUtils.getSubject();
        boolean isLogin = false;
        try{isLogin = currentUser.isAuthenticated(); }catch (Exception e){}
        if(!isLogin) {
            UsernamePasswordToken token = new UsernamePasswordToken(username,pwd);
            token.setRememberMe(true);
            try {
                currentUser.login(token);
                resMap.put("code", "000000");
                resMap.put("desc", "登录成功...");
            }catch(AuthenticationException e) {
                resMap.put("code", "999999");
                resMap.put("desc", "权限认证失败...");
            }
        }
        return resMap;
    }
}
