package com.ps.druiddemo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ps.druiddemo.dao.dto.UserInfo;
import com.ps.druiddemo.dao.dto.UserInfoCriteria;
import com.ps.druiddemo.dao.mapper.UserInfoMapper;

@Controller
@RequestMapping("/user")
public class UserInfoController {
    @Autowired
    UserInfoMapper userInfoMapper;

    @ResponseBody
    @RequestMapping("/getAll")
    public List<UserInfo> getAllUser() {
        return userInfoMapper.selectByExample(new UserInfoCriteria());
    }
}
