package com.hongshu.framework.security.service;

import com.hongshu.common.core.domain.entity.SysUser;
import com.hongshu.common.core.domain.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.hongshu.system.mapper.SysUserMapper;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 直接查 admin 用户（假设 admin 的 userId 是 1）
        SysUser user = userMapper.selectUserById(1L);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 权限列表
        Set<String> permissions = new HashSet<>();
        permissions.add("*:*:*");

        return new LoginUser(user, permissions);
    }
}