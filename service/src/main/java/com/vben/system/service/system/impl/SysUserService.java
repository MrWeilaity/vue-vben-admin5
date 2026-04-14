package com.vben.system.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.common.PageResult;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.params.UserParams;
import com.vben.system.dto.system.user.UserCreateRequest;
import com.vben.system.dto.system.user.UserResponse;
import com.vben.system.dto.system.user.UserSessionResponse;
import com.vben.system.dto.system.user.UserUpdateRequest;
import com.vben.system.dto.user.UserPasswordResetRequest;
import com.vben.system.entity.SysDept;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserDept;
import com.vben.system.entity.SysUserPost;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysDeptMapper;
import com.vben.system.mapper.SysUserDeptMapper;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.mapper.SysUserPostMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.service.AuthService;
import com.vben.system.service.system.ISysUserService;
import com.vben.system.security.LoginUserService;
import com.vben.system.security.datascope.DataScope;
import com.vben.system.security.datascope.DataScopeService;
import com.vben.system.security.datascope.DataScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户业务服务，封装用户增删改查与下线控制。
 */
@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;
    private final SysUserDeptMapper userDeptMapper;
    private final AuthService authService;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginUserService loginUserService;
    private final DataScopeService dataScopeService;
    @Value("${system.security.protected-user-id:1}")
    private Long protectedUserId;

    /**
     * 查询用户列表（按 ID 倒序）。
     * 用户管理页展示的是用户记录本身，当前表没有 create_by 字段，
     * 所以“仅本人数据”这里按用户表 id 判断。
     *
     * @return 用户列表
     */
    @DataScope(mapper = SysUserMapper.class, deptColumn = "dept_id", userColumn = "id")
    public PageResult<UserResponse> list(UserParams userParams) {
        Page<SysUser> page = new Page<>(userParams.getPage(), userParams.getPageSize());
        Page<SysUser> result = lambdaQuery()
                .eq(userParams.getStatus() != null, SysUser::getStatus, userParams.getStatus())
                .like(StrUtil.isNotBlank(userParams.getUsername()), SysUser::getUsername, userParams.getUsername())
                .like(StrUtil.isNotBlank(userParams.getNickname()), SysUser::getNickname, userParams.getNickname())
                .orderByDesc(SysUser::getId)
                .page(page);
        List<Long> userIds = result.getRecords().stream().map(SysUser::getId).toList();
        Map<Long, List<Long>> roleIdsByUserId = getRoleIdsByUserIds(userIds);
        Map<Long, List<Long>> postIdsByUserId = getPostIdsByUserIds(userIds);
        Map<Long, List<Long>> dataScopeDeptIdsByUserId = getDataScopeDeptIdsByUserIds(userIds);
        Map<Long, String> deptMap = getDeptNamesByDeptIds();
        List<UserResponse> list = result.getRecords().stream()
                .map(user -> user.toUserResponse(
                        roleIdsByUserId.getOrDefault(user.getId(), List.of()),
                        postIdsByUserId.getOrDefault(user.getId(), List.of()),
                        dataScopeDeptIdsByUserId.getOrDefault(user.getId(), List.of()),
                        deptMap.get(user.getDeptId())
                ))
                .toList();
        return new PageResult<>(result.getTotal(), list);
    }

    /**
     * 新增用户。
     *
     * @param request 用户新增数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(UserCreateRequest request) {
        validateDataScope(request.getDataScope(), request.getDataScopeDeptIds());
        dataScopeService.assertAccessible(request.getDeptId(), null);
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDeptId(request.getDeptId());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setStatus(request.getStatus());
        user.setDataScope(request.getDataScope());
        user.setRemark(request.getRemark());
        List<Long> roleIds = request.getRoleIds() == null ? List.of() : request.getRoleIds();
        List<Long> postIds = request.getPostIds() == null ? List.of() : request.getPostIds();
        userMapper.insert(user);
        saveUserRoles(user.getId(), roleIds);
        saveUserPosts(user.getId(), postIds);
        saveUserDataScopeDepts(user.getId(), request.getDataScope(), request.getDataScopeDeptIds());
    }

    /**
     * 更新用户。
     *
     * @param id         用户 ID
     * @param updateUser 用户更新请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UserUpdateRequest updateUser) {
        SysUser existingUser = assertUserExists(id);
        dataScopeService.assertAccessible(existingUser.getDeptId(), existingUser.getId());
        Integer dataScope = updateUser.getDataScope() == null ? existingUser.getDataScope() : updateUser.getDataScope();
        if (updateUser.getDataScope() != null || updateUser.getDataScopeDeptIds() != null) {
            validateDataScope(dataScope, updateUser.getDataScopeDeptIds());
        }
        if (updateUser.getDeptId() != null) {
            dataScopeService.assertAccessible(updateUser.getDeptId(), null);
        }
        lambdaUpdate().eq(SysUser::getId, id)
                .set(updateUser.getNickname() != null, SysUser::getNickname, updateUser.getNickname())
                .set(updateUser.getDeptId() != null, SysUser::getDeptId, updateUser.getDeptId())
                .set(updateUser.getEmail() != null, SysUser::getEmail, updateUser.getEmail())
                .set(updateUser.getMobile() != null, SysUser::getMobile, updateUser.getMobile())
                .set(updateUser.getStatus() != null, SysUser::getStatus, updateUser.getStatus())
                .set(updateUser.getDataScope() != null, SysUser::getDataScope, updateUser.getDataScope())
                .set(updateUser.getRemark() != null, SysUser::getRemark, updateUser.getRemark())
                .update();
        if (updateUser.getRoleIds() != null) {
            List<Long> roleIds = updateUser.getRoleIds();
            saveUserRoles(id, roleIds);
        }
        if (updateUser.getPostIds() != null) {
            List<Long> postIds = updateUser.getPostIds();
            saveUserPosts(id, postIds);
        }
        if (updateUser.getDataScope() != null || updateUser.getDataScopeDeptIds() != null) {
            saveUserDataScopeDepts(id, dataScope, updateUser.getDataScopeDeptIds());
        }
        if (updateUser.getStatus() != null && updateUser.getStatus() != 1
                && (existingUser.getStatus() == null || existingUser.getStatus() == 1)) {
            authService.forceOffline(id);
        }
    }

    /**
     * 删除用户并强制该用户 token 失效。
     *
     * @param id 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在或已被删除");
        }
        if (protectedUserId != null && protectedUserId.equals(user.getId())) {
            throw new ServiceException("系统管理员账号不允许删除");
        }
        dataScopeService.assertAccessible(user.getDeptId(), user.getId());
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, id));
        userDeptMapper.delete(new LambdaQueryWrapper<SysUserDept>().eq(SysUserDept::getUserId, id));
        authService.forceOffline(id);
    }

    /**
     * 强制下线用户。
     *
     * @param id 用户 ID
     */
    public void forceOffline(Long id) {
        SysUser user = assertUserExists(id);
        dataScopeService.assertAccessible(user.getDeptId(), user.getId());
        authService.forceOffline(id);
    }

    public List<UserSessionResponse> listSessions(Long id) {
        SysUser user = assertUserExists(id);
        dataScopeService.assertAccessible(user.getDeptId(), user.getId());
        String currentSessionId = null;
        try {
            if (id.equals(loginUserService.getCurrentUserId())) {
                currentSessionId = loginUserService.getCurrentSessionId();
            }
        } catch (Exception ignored) {
        }
        return authService.listUserSessions(user.getId(), currentSessionId);
    }

    public void offlineSession(Long id, String sessionId) {
        SysUser user = assertUserExists(id);
        dataScopeService.assertAccessible(user.getDeptId(), user.getId());
        authService.forceOffline(id, sessionId);
    }

    public Map<Long, List<Long>> getRoleIdsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId, Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
    }

    public Map<Long, List<Long>> getPostIdsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userPostMapper.selectList(new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(SysUserPost::getUserId, Collectors.mapping(SysUserPost::getPostId, Collectors.toList())));
    }

    /**
     * 批量查询用户自身配置的自定义数据权限部门，用于用户列表和编辑回显。
     *
     * @param userIds 用户 ID 集合
     * @return key 为用户 ID，value 为该用户配置的自定义部门 ID 集合
     */
    public Map<Long, List<Long>> getDataScopeDeptIdsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userDeptMapper.selectList(new LambdaQueryWrapper<SysUserDept>().in(SysUserDept::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(SysUserDept::getUserId, Collectors.mapping(SysUserDept::getDeptId, Collectors.toList())));
    }

    /**
     * 获取部门的键值对组合 键：部门id 值：部门名称
     *
     * @return 部门id和部门名称的键值对组合
     */
    private Map<Long, String> getDeptNamesByDeptIds() {
        return deptMapper.selectList(new LambdaQueryWrapper<SysDept>().select(SysDept::getId, SysDept::getName))
                .stream().collect(Collectors.toMap(SysDept::getId, SysDept::getName));
    }

    /**
     * 保存用户和角色的关系
     *
     * @param userId  用户id
     * @param roleIds 角色id组合
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        Set<Long> distinctRoleIds = new LinkedHashSet<>(roleIds);
        List<SysUserRole> relations = new ArrayList<>();
        for (Long roleId : distinctRoleIds) {
            if (roleId == null) {
                continue;
            }
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relations.add(relation);
        }
        relations.forEach(userRoleMapper::insert);
    }

    /**
     * 保存用户和岗位的关系
     *
     * @param userId  用户id
     * @param postIds 岗位id集合
     */
    private void saveUserPosts(Long userId, List<Long> postIds) {
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        Set<Long> distinctPostIds = new LinkedHashSet<>(postIds);
        List<SysUserPost> relations = new ArrayList<>();
        for (Long postId : distinctPostIds) {
            if (postId == null) {
                continue;
            }
            SysUserPost relation = new SysUserPost();
            relation.setUserId(userId);
            relation.setPostId(postId);
            relations.add(relation);
        }
        relations.forEach(userPostMapper::insert);
    }

    /**
     * 同步用户自身和自定义数据权限部门的关系。
     * <p>
     * 每次保存都先删除旧关系；只有 {@code dataScope=2}（自定义数据）时才写入新部门集合，
     * 其它数据权限类型会清空原有自定义部门，避免历史配置残留。
     *
     * @param userId    用户 ID
     * @param dataScope 数据权限类型
     * @param deptIds   自定义部门 ID 集合
     */
    private void saveUserDataScopeDepts(Long userId, Integer dataScope, List<Long> deptIds) {
        userDeptMapper.delete(new LambdaQueryWrapper<SysUserDept>().eq(SysUserDept::getUserId, userId));
        if (!java.util.Objects.equals(DataScopeType.CUSTOM.getValue(), dataScope)) {
            return;
        }
        Set<Long> distinctDeptIds = new LinkedHashSet<>(deptIds);
        List<SysUserDept> relations = new ArrayList<>();
        for (Long deptId : distinctDeptIds) {
            if (deptId == null) {
                continue;
            }
            SysUserDept relation = new SysUserDept();
            relation.setUserId(userId);
            relation.setDeptId(deptId);
            relations.add(relation);
        }
        relations.forEach(userDeptMapper::insert);
    }

    /**
     * 重置用户密码
     *
     * @param id      用户id
     * @param request 重置密码请求体
     */
    public void resetPassword(Long id, UserPasswordResetRequest request) {
        SysUser user = assertUserExists(id);
        dataScopeService.assertAccessible(user.getDeptId(), user.getId());
        lambdaUpdate().eq(SysUser::getId, id)
                .set(SysUser::getPassword, passwordEncoder.encode(request.getNewPassword()))
                .update();
        authService.forceOffline(id);
    }

    private SysUser assertUserExists(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在或已被删除");
        }
        return user;
    }

    /**
     * 校验用户数据权限配置是否有效。
     * <p>
     * 数据权限必须是 1-5；选择自定义数据时必须至少选择一个部门。
     */
    private void validateDataScope(Integer dataScope, List<Long> deptIds) {
        if (!DataScopeType.isValid(dataScope)) {
            throw new ServiceException("数据权限范围不合法");
        }
        if (java.util.Objects.equals(DataScopeType.CUSTOM.getValue(), dataScope) && (deptIds == null || deptIds.isEmpty())) {
            throw new ServiceException("自定义数据权限必须选择部门");
        }
    }
}
