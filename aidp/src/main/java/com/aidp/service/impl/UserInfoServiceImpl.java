package com.aidp.service.impl;

import com.aidp.entity.UserInfo;
import com.aidp.mapper.UserInfoMapper;
import com.aidp.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
