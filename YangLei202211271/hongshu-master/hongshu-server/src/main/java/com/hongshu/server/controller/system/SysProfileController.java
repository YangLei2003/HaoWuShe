package com.hongshu.server.controller.system;

import com.hongshu.common.annotation.Log;
import com.hongshu.common.config.HongshuConfig;
import com.hongshu.common.core.controller.BaseController;
import com.hongshu.common.core.domain.AjaxResult;
import com.hongshu.common.core.domain.entity.SysUser;
import com.hongshu.common.core.domain.model.LoginUser;
import com.hongshu.common.enums.BusinessType;
import com.hongshu.common.utils.SecurityUtils;
import com.hongshu.common.utils.StringUtils;
import com.hongshu.common.utils.file.FileUploadUtils;
import com.hongshu.common.utils.file.MimeTypeUtils;
import com.hongshu.framework.web.service.TokenService;
import com.hongshu.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人信息 业务处理
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private TokenService tokenService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile() {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        return AjaxResult.success(user);
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user) {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getUser();

        // 更新字段
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());

        // 直接使用 Mapper 更新数据库
        if (userMapper.updateUser(currentUser) > 0) {
            // 更新缓存
            tokenService.setLoginUser(loginUser);
            return success("修改成功");
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(String oldPassword, String newPassword) {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();

        // 验证旧密码
        if (!SecurityUtils.matchesPassword(oldPassword, user.getPassword())) {
            return error("修改密码失败，旧密码错误");
        }

        // 新密码不能与旧密码相同
        if (SecurityUtils.matchesPassword(newPassword, user.getPassword())) {
            return error("新密码不能与旧密码相同");
        }

        // 加密新密码
        String encryptPassword = SecurityUtils.encryptPassword(newPassword);
        user.setPassword(encryptPassword);

        // 更新数据库
        if (userMapper.updateUser(user) > 0) {
            // 更新缓存
            tokenService.setLoginUser(loginUser);
            return success("密码修改成功");
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return error("上传图片不能为空");
        }

        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();

        // 上传头像
        String avatar = FileUploadUtils.upload(HongshuConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);

        // 更新用户头像
        user.setAvatar(avatar);
        if (userMapper.updateUser(user) > 0) {
            // 更新缓存
            tokenService.setLoginUser(loginUser);

            AjaxResult ajax = AjaxResult.success("头像上传成功");
            ajax.put("imgUrl", avatar);
            return ajax;
        }

        return error("上传图片异常，请联系管理员");
    }
}