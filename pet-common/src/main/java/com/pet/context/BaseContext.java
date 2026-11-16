package com.pet.context;

/**
 * 基于 ThreadLocal 的当前登录用户上下文。
 *
 * <p>拦截器在请求进入时写入用户 ID，业务层可直接获取；请求结束后必须调用
 * {@link #removeCurrentId()} 清理，避免线程复用导致用户串号。</p>
 */
public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 保存当前登录用户 ID。
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前登录用户 ID。
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 清理当前线程的用户 ID。
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
