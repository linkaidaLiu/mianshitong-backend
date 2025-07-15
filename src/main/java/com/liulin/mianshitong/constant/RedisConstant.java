package com.liulin.mianshitong.constant;

/**
 * Redis常量
 *
 * @author liulin
 */
public interface RedisConstant {

    /**
     * 用户签到记录的redis key前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX="user:signins:";

    /**
     * 获取用户签到记录的redis key
     * @param year
     * @param userid
     * @return 拼接好的redis key
     */
    static String getUserSignInRedisKey(int year,long userid){
        return String.format("%s:%s:%s",USER_SIGN_IN_REDIS_KEY_PREFIX,year,userid);
    }

}
