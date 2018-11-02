package com.tokii.shiro.dao.impl;

import com.tokii.shiro.dao.UserDao;
import com.tokii.shiro.pojo.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
@PropertySource({"classpath:sql/user.properties"})
public class UserDaoImpl extends BaseDao implements UserDao {
    /**
     * ### 查询用户信息
     */
    @Value("${user.sql1}")
    private String sql1 ;
    /**
     * ### 查询用户的角色
     */
    @Value("${user.sql2}")
    private String sql2;

    /**
     * 获取用户信息
     * @param email
     * @return
     */
    @Override
    public User findUserByEmail(String email) {
        return jdbcTemplate.queryForObject(sql1,new RowMapper<User>(){
            @Nullable
            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setNickName(resultSet.getString("nickname"));
                user.setEmail(resultSet.getString("email"));
                user.setPswd(resultSet.getString("pswd"));
                user.setCreateTime(resultSet.getDate("create_time"));
                user.setLastLoginTime(resultSet.getDate("last_login_time"));
                user.setStatus(resultSet.getInt("status"));
                return user;
            }
        },email);
    }

    /**
     * 获取用户角色
     * @param userId
     * @return
     */
    @Override
    public Set<String> getRoles(int userId) {
        return jdbcTemplate.queryForObject(sql2,new RowMapper<Set<String>>(){
            @Nullable
            @Override
            public Set<String> mapRow(ResultSet resultSet, int i) throws SQLException {
                Set<String> roles = new HashSet<String>();
                roles.add(resultSet.getString("type"));
                return roles;
            }
        },userId);
    }


}
