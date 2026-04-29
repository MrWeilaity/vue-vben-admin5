package com.vben.system.dto.system.menu;

import cn.hutool.core.util.IdUtil;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 新增菜单请求。
 */
@Data
public class MenuCreateRequest {
    /**
     * 父菜单ID
     */
    private Long pid;
    /**
     * 菜单名称
     */
    @NotBlank
    private String name;
    /**
     * 路由路径
     */
    private String path;
    /**
     * 菜单类型
     */
    @NotBlank
    private String type;
    /**
     * 组件路径
     */
    private String component;
    /**
     * 权限标识
     */
    private String authCode;
    /**
     * 状态
     */
    @NotNull
    private Integer status;
    /**
     * 菜单元数据
     */
    private Map<String, Object> meta;

    /**
     * 非 link 类型时，path 不能为空（button 例外）
     */
    @AssertTrue(message = "非 link/button 类型时，path 不能为空")
    public boolean isPathValid() {
        if (!StringUtils.hasText(type)) {
            return true;
        }

        String t = type.trim().toLowerCase();

        // link 或 button 都允许没有 path
        if ("link".equals(t) || "button".equals(t)) {
            return true;
        }

        return StringUtils.hasText(path);
    }

    /**
     * 统一处理 path 逻辑
     */
    public String getPath() {
        if (!StringUtils.hasText(type)) {
            return path;
        }

        String t = type.trim().toLowerCase();

        // 1️⃣ link → 自动生成随机 path
        if ("link".equals(t)) {
            if (!StringUtils.hasText(this.path)) {
                this.path = "/link_" + IdUtil.simpleUUID();
            }
        }

        // 2️⃣ button → 强制为 null
        if ("button".equals(t)) {
            this.path = null;
        }

        return this.path;
    }
}
