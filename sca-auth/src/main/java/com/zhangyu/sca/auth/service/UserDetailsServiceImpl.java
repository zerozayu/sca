package com.zhangyu.sca.auth.service;

import jakarta.annotation.Resource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author zhangyu
 * @date 2024/5/20 13:55
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //1.基于用户名查询用户信息(暂时先给假数据)
        //Userinfo info = userMapper.selectUserByUserName(username);
        String encodePassword = passwordEncoder.encode("123456"); //假设这个密码来自数据库
        //2.封装用户相关信息并返回
        return new User(username,
                encodePassword,//必须是已经加密的密码
                AuthorityUtils.createAuthorityList("sys:res:create", "sys:res:retrieve")); //权限
    }
}
