package com.ps.druiddemo;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.ps.druiddemo.dao.dto.UserInfo;
import com.ps.druiddemo.dao.dto.UserInfoCriteria;
import com.ps.druiddemo.dao.mapper.UserInfoMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DaoTest {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Test
    public void testGetUserList2(){
        UserInfoCriteria criteria = new UserInfoCriteria();
        criteria.createCriteria();
        List<UserInfo> userList = userInfoMapper.selectByExample(criteria);
        for (UserInfo user : userList) {
            System.out.println(user);
        }
    } 
}
