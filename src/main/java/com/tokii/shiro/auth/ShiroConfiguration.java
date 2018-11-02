package com.tokii.shiro.auth;

import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfiguration {
    private final Logger log = LoggerFactory.getLogger(ShiroConfiguration.class);

    /**
     * 使用本地EhCache管理用户数据
     */
    @Bean
    public EhCacheManager getEhCacheManager(){
        EhCacheManager ehcacheManager = new EhCacheManager();
//        ehcacheManager.setCacheManagerConfigFile("classpath:config/ehcache-shiro.xml");
        return ehcacheManager;
    }

    /**
     * 设定安正认证、访问控制
     */
    @Bean(name="myShiroRealm")
    public MyShiroRealm myShiroRealm(EhCacheManager ehCacheManager){
        MyShiroRealm realm = new MyShiroRealm();
        realm.setCacheManager(ehCacheManager);
        //
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashAlgorithmName("MD5");
        credentialsMatcher.setHashIterations(1024);
        //使用realm所加密的hex,如果为false就使用base64 hex
        credentialsMatcher.setStoredCredentialsHexEncoded(true);

        realm.setCredentialsMatcher(credentialsMatcher);
        return realm;
    }

    /**
     * DestructionAwareBeanPostProcessor的子类，
     * 负责org.apache.shiro.util.Initializable类型bean的生命周期的，初始化和销毁。
     * 主要是AuthorizingRealm类的子类，以及EhCacheManager类。
     */
    @Bean(name="lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }

    /**
     * Spring的一个bean，由Advisor决定对哪些类的方法进行AOP代理。
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    /**
     * 权限管理，这个类组合了登陆，登出，权限，session的处理，是个比较重要的类。
     */
    @Bean(name="securityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(MyShiroRealm realm){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //设置realm
        securityManager.setRealm(realm);
        securityManager.setCacheManager(getEhCacheManager());
        return securityManager;
    }

    /**
     * shiro里实现的Advisor类，内部使用AopAllianceAnnotationsAuthorizingMethodInterceptor来拦截用以下注解的方法。
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager){
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        Map<String,Filter> filterMap = new HashMap<>();
        filterMap.put("authc",new MySpringShiroFilter());
        factoryBean.setFilters(filterMap);

        factoryBean.setSecurityManager(securityManager);
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        factoryBean.setLoginUrl("/login.html");
        // 登录成功后要跳转的连接
        factoryBean.setSuccessUrl("/index.html");
        factoryBean.setUnauthorizedUrl("/error/403.html");
        loadShiroFilterChain(factoryBean);
        log.info("shiro拦截器工厂类注入成功");
        return factoryBean;
    }

    /**
     * 加载ShiroFilter权限控制规则
     */
    private void loadShiroFilterChain(ShiroFilterFactoryBean factoryBean) {
        /** 下面这些规则配置最好配置到配置文件中 */
        Map<String, String> filterChainMap = new LinkedHashMap<>();
        /** authc：该过滤器下的页面必须验证后才能访问，它是Shiro内置的一个拦截器
         * org.apache.shiro.web.filter.authc.FormAuthenticationFilter */
        // anon：它对应的过滤器里面是空的,什么都没做,可以理解为不拦截
        //authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问
        filterChainMap.put("/shiro/**", "authc");
        filterChainMap.put("/**","anon");
        factoryBean.setFilterChainDefinitionMap(filterChainMap);
    }

    /**
        1.LifecycleBeanPostProcessor，这是个DestructionAwareBeanPostProcessor的子类，负责org.apache.shiro.util.Initializable类型bean的生命周期的，初始化和销毁。主要是AuthorizingRealm类的子类，以及EhCacheManager类。
        2.HashedCredentialsMatcher，这个类是为了对密码进行编码的，防止密码在数据库里明码保存，当然在登陆认证的生活，这个类也负责对form里输入的密码进行编码。
        3.ShiroRealm，这是个自定义的认证类，继承自AuthorizingRealm，负责用户的认证和权限的处理，可以参考JdbcRealm的实现。
        4.EhCacheManager，缓存管理，用户登陆成功后，把用户信息和权限信息缓存起来，然后每次用户请求时，放入用户的session中，如果不设置这个bean，每个请求都会查询一次数据库。
        5.SecurityManager，权限管理，这个类组合了登陆，登出，权限，session的处理，是个比较重要的类。
        6.ShiroFilterFactoryBean，是个factorybean，为了生成ShiroFilter。它主要保持了三项数据，securityManager，filters，filterChainDefinitionManager。
        7.DefaultAdvisorAutoProxyCreator，Spring的一个bean，由Advisor决定对哪些类的方法进行AOP代理。
        8.AuthorizationAttributeSourceAdvisor，shiro里实现的Advisor类，内部使用AopAllianceAnnotationsAuthorizingMethodInterceptor来拦截用以下注解的方法。
    */

    private class MySpringShiroFilter extends FormAuthenticationFilter {

        /**
         * 在访问controller前判断是否登录，返回json，不进行重定向。
         * @param request
         * @param response
         * @return true-继续往下执行，false-该filter过滤器已经处理，不继续执行其他过滤器
         * @throws Exception
         */
        @Override
        protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            if (isAjax(request)) {
                httpServletResponse.setCharacterEncoding("UTF-8");
                httpServletResponse.setContentType("application/json");
//                ResultData resultData = new ResultData();
//                resultData.setResult(1);
//                resultData.setCode(403);
//                resultData.setMessage("登录认证失效，请重新登录!");
                httpServletResponse.getWriter().write("权限认证失败...");
            } else {
                //saveRequestAndRedirectToLogin(request, response);
                /**
                 * @Mark 非ajax请求重定向为登录页面
                 */
                httpServletResponse.sendRedirect(getLoginUrl());
            }
            return false;
        }

        private boolean isAjax(ServletRequest request){
            String header = ((HttpServletRequest) request).getHeader("X-Requested-With");
            if("XMLHttpRequest".equalsIgnoreCase(header)){
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }

    }
}
