
package com.peng.zuul.server.config;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 动态路由配置表
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZuulRoute implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * router Id
     */
    private Integer id;
    /**
     * 路由路径
     */
    private String path;
    /**
     * 服务名称
     */
    private String serviceId;
    /**
     * url代理
     */
    private String url;
    /**
     * 转发去掉前缀
     */
    private String stripPrefix;
    /**
     * 是否重试
     */
    private String retryable;
    /**
     * 是否启用
     */
    private String enabled;
    /**
     * 敏感请求头
     */
    private String sensitiveheadersList;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标识（0-正常,1-删除）
     */
    private String delFlag;


}
