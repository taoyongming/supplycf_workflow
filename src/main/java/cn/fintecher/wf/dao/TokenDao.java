package cn.fintecher.wf.dao;

import cn.fintecher.wf.entity.TokenEntity;

/**
 * 用户Token
 * 
 */
public interface TokenDao extends BaseDao<TokenEntity> {
    
    TokenEntity queryByUserId(Long userId);

    TokenEntity queryByToken(String token);
	
}
