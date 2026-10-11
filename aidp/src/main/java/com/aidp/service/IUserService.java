package com.aidp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aidp.dto.LoginFormDTO;
import com.aidp.dto.Result;
import com.aidp.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
