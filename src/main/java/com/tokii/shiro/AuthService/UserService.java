package com.tokii.shiro.AuthService;

import com.tokii.shiro.dao.UserDao;
import com.tokii.shiro.pojo.User;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    /**
     * 身份验证
     * @param authenticationToken
     * @return
     */
    public User getAuthentication(AuthenticationToken authenticationToken){
        //UsernamePasswordToken用于存放提交的登录信息
        UsernamePasswordToken token = (UsernamePasswordToken)authenticationToken;
        logger.info("登录认证,验证当前Subject时获取到token为：" + ReflectionToStringBuilder.toString(token, ToStringStyle.MULTI_LINE_STYLE));
        return userDao.findUserByEmail(token.getUsername());
    }

    /**
     * 获取用户角色
     * @param userId
     * @return
     */
    public Set<String> getRoles(int userId){
        return userDao.getRoles(userId);
    }
}
