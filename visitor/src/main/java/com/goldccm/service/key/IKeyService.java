package com.goldccm.service.key;

/**
 * 密钥
 * Created by LZ on 2017/5/23.
 */
public interface IKeyService {
    /**
     * 作用：通过 状态 获取密钥
     * @param status 状态
     * @return
     * @throws Exception
     */
   public String findKeyByStatus(String status) throws  Exception;
}
