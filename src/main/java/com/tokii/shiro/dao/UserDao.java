package com.tokii.shiro.dao;

import com.tokii.shiro.pojo.User;

import java.util.Set;

public interface UserDao {
    User findUserByEmail(String email);
    Set<String> getRoles(int userId);
}
