package cn.fintecher.wf.dao;

import cn.fintecher.wf.entity.UserEntity;

/**
 * 用户
 * 
 */
public interface UserDao extends BaseDao<UserEntity> {

    UserEntity queryByMobile(String mobile);
}
