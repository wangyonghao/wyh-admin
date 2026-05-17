package top.wyhao.admin.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.validation.ValidationUtil;
import cn.hutool.json.JSONUtil;
import cn.idev.excel.FastExcelFactory;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheUpdate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.admin.system.entity.SysDept;
import top.wyhao.admin.system.entity.SysFile;
import top.wyhao.admin.system.entity.SysRole;
import top.wyhao.admin.system.entity.SysUserRole;
import top.wyhao.admin.system.entity.user.SysUser;
import top.wyhao.admin.system.entity.user.SysUserPasswordHistory;
import top.wyhao.admin.system.mapper.DeptMapper;
import top.wyhao.admin.system.mapper.MenuMapper;
import top.wyhao.admin.system.mapper.UserRoleMapper;
import top.wyhao.admin.system.mapper.user.UserMapper;
import top.wyhao.admin.system.mapper.user.UserPasswordHistoryMapper;
import top.wyhao.admin.system.model.SystemConstants;
import top.wyhao.admin.system.model.bo.FileRequest;
import top.wyhao.admin.system.model.bo.user.*;
import top.wyhao.admin.system.model.query.FileQuery;
import top.wyhao.admin.system.model.query.UserQuery;
import top.wyhao.admin.system.model.vo.config.SecurityConfigVO;
import top.wyhao.admin.system.model.vo.user.UserDetailResult;
import top.wyhao.admin.system.model.vo.user.UserImportParseResp;
import top.wyhao.admin.system.model.vo.user.UserImportResp;
import top.wyhao.admin.system.model.vo.user.UserResult;
import top.wyhao.admin.system.service.*;
import top.wyhao.cmn.db.util.QueryWrapperUtil;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.core.constant.RegexConstants;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.enums.GenderEnum;
import top.wyhao.starter.core.enums.RoleCodeEnum;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.core.util.ExceptionUtils;
import top.wyhao.starter.core.util.RsaUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static top.wyhao.admin.system.model.enums.ImportPolicies.*;
import static top.wyhao.admin.system.model.enums.PasswordPolicies.*;

/**
 * 用户业务实现
 *
 * @author Charles7c
 * @since 2022/12/21 21:49
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserPasswordHistoryService userPasswordHistoryService;
    private final UserSocialService userSocialService;
    private final RoleService roleService;
    private final FileService fileService;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final UserPasswordHistoryMapper passwordHistoryMapper;
    private final UserRoleMapper userRoleMapper;
    private final MenuMapper menuMapper;
    private final ConfigService configService;

    @Value("${avatar.support-suffix}")
    private String[] avatarSupportSuffix;


    @Override
    public UserDetailResult detail(Long id) {
        SysUser userDO = userMapper.selectById(id);
        BizAssert.notNull(userDO, "用户不存在");
        return BeanUtil.copyProperties(userDO, UserDetailResult.class);
    }

    @Override
    public PageResult<UserResult> page(UserQuery query, PageQuery pageQuery) {
        QueryWrapper<SysUser> queryWrapper = this.buildQueryWrapper(query);
        QueryWrapperUtil.applySort(queryWrapper, query.getSort(), SysUser.class);
        IPage<UserResult> page = userMapper.selectUserPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper);
        return PageResult.build(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(UserRequest request) {
        /* 入参格式校验 */
        String rawPassword = ExceptionUtils.exToNull(() -> RsaUtils.decryptByRsaPrivateKey(request.getPassword()));
        BizAssert.notBlank(rawPassword, "密码解密失败");
        BizAssert.isTrue(ReUtil.isMatch(RegexConstants.PASSWORD, rawPassword), "密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字");
        this.checkEmailUnique(request.getEmail(), null);
        this.checkPhoneUnique(request.getPhone(), null);
        this.checkUsernameUnique(request.getUsername());

        SysUser newUser = BeanUtil.copyProperties(request, SysUser.class);
        /* 业务逻辑校验 */



        /* 执行业务 */
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        SecurityConfigVO loginConfig = configService.getSecurityConfig();
        newUser.setPwdExpireDate(LocalDate.now().plusDays(loginConfig.getPasswordExpireDays()));
        userMapper.insert(newUser);

        // 保存用户和角色关联
        roleService.assignRolesToUser(request.getRoleIds(), newUser.getId());
        return newUser.getId();
    }

    @Override
    public Long save(SysUser user) {
        userMapper.insert(user);
        return user.getId();
    }


    @Transactional(rollbackFor = Exception.class)
    @CacheUpdate(key = "#userId", value = "#userBO.nickname", name = CacheConstants.USER_KEY_PREFIX)
    public void update(Long userId, UserRequest userRequest) {
        SysUser oldUser = this.getById(userId);

        if (StatusEnum.DISABLE.getValue().equals(oldUser.getStatus())) {
            if (oldUser.getIsBuiltin()) {
                throw new BusinessException("USER_UPDATE_NOT_ALLOWED", "系统内置用户不允许禁用");
            }
        }
        if (CollUtil.isNotEmpty(userRequest.getRoleIds())) {
            throw new BusinessException("USER_UPDATE_NOT_ALLOWED", "系统内置用户不允许变更角色");
        }
        if (StrUtil.isNotBlank(userRequest.getEmail())) {
            this.checkEmailUnique(userRequest.getEmail(), userId);
        }
        if (StrUtil.isNotBlank(userRequest.getPhone())) {
            this.checkPhoneUnique(userRequest.getPhone(), userId);
        }

        SysUser updateUser = BeanUtil.toBean(userRequest, SysUser.class);
        updateUser.setId(userId);
        userMapper.updateById(updateUser);
        // 保存用户和角色关联
        roleService.assignRolesToUser(userRequest.getRoleIds(), userId);

        // 用户被禁用，则踢出在线用户
        if (StatusEnum.DISABLE.equals(userRequest.getStatus())) {
            LoginUtil.kickout(userId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(key = "#ids", name = CacheConstants.USER_KEY_PREFIX, multi = true)
    public void delete(List<Long> ids) {
        BizAssert.isTrue(CollUtil.contains(ids, LoginUtil.getUserId()), "不允许删除当前用户");
        List<SysUser> list = userMapper.lambdaQuery()
                .select(SysUser::getId, SysUser::getNickname, SysUser::getIsBuiltin)
                .in(SysUser::getId, ids)
                .list();
        List<Long> idList = CollUtils.mapToList(list, SysUser::getId);
        Collection<Long> subtractIds = CollUtil.subtract(ids, idList);
        BizAssert.throwIfNotEmpty(subtractIds, "所选用户 [{}] 不存在", CollUtil.join(subtractIds, StringConstants.COMMA));
        Optional<SysUser> builtinUser = list.stream().filter(SysUser::getIsBuiltin).findFirst();
        BizAssert.isTrue(builtinUser::isPresent, "所选用户 [{}] 是系统内置用户，不允许删除", builtinUser.orElseGet(SysUser::new)
                .getNickname());
        // 删除用户和角色关联
        userRoleMapper.lambdaUpdate().in(SysUserRole::getUserId, ids).remove();
        // 删除历史密码
        userPasswordHistoryService.deleteByUserIds(ids);
        // 删除用户绑定的第三方账号信息
        userSocialService.deleteByUserIds(ids);
        // 删除用户
        userMapper.deleteByIds(ids);
        // 踢出在线用户
        ids.forEach(LoginUtil::kickout);
    }

    @Override
    public UserImportParseResp parseImport(MultipartFile file) {
        UserImportParseResp userImportResp = new UserImportParseResp();
        List<UserImportRowReq> importRowList;
        // 读取表格数据
        try {
            importRowList = FastExcelFactory.read(file.getInputStream())
                    .head(UserImportRowReq.class)
                    .sheet()
                    .headRowNumber(1)
                    .doReadSync();
        } catch (Exception e) {
            log.error("用户导入数据文件解析异常：{}", e.getMessage(), e);
            throw new BusinessException("IMPORT_FORMAT_ERROR", "数据文件解析异常");
        }
        // 总计行数
        userImportResp.setTotalRows(importRowList.size());
        BizAssert.throwIfEmpty(importRowList, "数据文件格式不正确");
        // 有效行数：过滤无效数据
        List<UserImportRowReq> validRowList = this.filterImportData(importRowList);
        userImportResp.setValidRows(validRowList.size());
        BizAssert.throwIfEmpty(validRowList, "数据文件格式不正确");

        // 检测表格内数据是否合法
        Set<String> seenEmails = new HashSet<>();
        boolean hasDuplicateEmail = validRowList.stream()
                .map(UserImportRowReq::getEmail)
                .anyMatch(email -> email != null && !seenEmails.add(email));
        BizAssert.isTrue(hasDuplicateEmail, "存在重复邮箱，请检测数据");
        Set<String> seenPhones = new HashSet<>();
        boolean hasDuplicatePhone = validRowList.stream()
                .map(UserImportRowReq::getPhone)
                .anyMatch(phone -> phone != null && !seenPhones.add(phone));
        BizAssert.isTrue(hasDuplicatePhone, "存在重复手机，请检测数据");

        // 校验是否存在无效角色
        List<String> roleNames = validRowList.stream().map(UserImportRowReq::getRoleName).distinct().toList();
        int existRoleCount = roleService.countByNames(roleNames);
        BizAssert.isTrue(existRoleCount < roleNames.size(), "存在无效角色，请检查数据");
        // 校验是否存在无效部门（支持多级部门解析）
        Set<String> deptNames = CollUtils.mapToSet(validRowList, UserImportRowReq::getDeptName);
        int existDeptCount = countValidMultiLevelDepts(deptNames);
        BizAssert.isTrue(existDeptCount < deptNames.size(), "存在无效部门，请检查部门名称或部门层级是否正确");

        // 查询重复用户
        userImportResp
                .setDuplicateUserRows(countExistByField(validRowList, UserImportRowReq::getUsername, SysUser::getUsername));
        // 查询重复邮箱
        userImportResp.setDuplicateEmailRows(countExistByField(validRowList, row -> row
                .getEmail(), SysUser::getEmail));
        // 查询重复手机
        userImportResp.setDuplicatePhoneRows(countExistByField(validRowList, row -> row
                .getPhone(), SysUser::getPhone));

        // 设置导入会话并缓存数据，有效期10分钟
        String importKey = UUID.fastUUID().toString(true);
        RedisUtils.set(CacheConstants.DATA_IMPORT_KEY + importKey, JSONUtil.toJsonStr(validRowList), Duration
                .ofMinutes(10));
        userImportResp.setImportKey(importKey);
        return userImportResp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserImportResp importUser(UserImportRequest req) {
        // 校验导入会话是否过期
        List<UserImportRowReq> importUserList;
        try {
            String data = RedisUtils.get(CacheConstants.DATA_IMPORT_KEY + req.getImportKey());
            importUserList = JSONUtil.toList(data, UserImportRowReq.class);
            BizAssert.isTrue(CollUtil.isEmpty(importUserList), "导入已过期，请重新上传");
        } catch (Exception e) {
            log.error("导入异常:", e);
            throw new BusinessException("IMPORTATION_EXPIRED", "导入已过期，请重新上传");
        }
        // 已存在数据查询
        List<String> existEmails = listExistByField(importUserList, row -> row.getEmail(), SysUser::getEmail);
        List<String> existPhones = listExistByField(importUserList, row -> row.getPhone(), SysUser::getPhone);
        List<SysUser> existUserList = listByUsernames(CollUtils
                .mapToList(importUserList, UserImportRowReq::getUsername));
        List<String> existUsernames = CollUtils.mapToList(existUserList, SysUser::getUsername);
        BizAssert
                .isTrue(isExitImportUser(req, importUserList, existUsernames, existEmails, existPhones), "数据不符合导入策略，已退出导入");

        // 基础数据准备
        Map<String, Long> userMap = existUserList.stream()
                .collect(Collectors.toMap(SysUser::getUsername, SysUser::getId));
        List<SysRole> roleList = roleService.listByNames(importUserList.stream()
                .map(UserImportRowReq::getRoleName)
                .distinct()
                .toList());
        Map<String, Long> roleMap = roleList.stream().collect(Collectors.toMap(SysRole::getName, SysRole::getId));
        // 获取多级部门映射
        Map<String, Long> deptMap = buildMultiLevelDeptMapping(importUserList.stream()
                .map(UserImportRowReq::getDeptName)
                .distinct()
                .toList());

        // 批量操作数据库集合
        List<SysUser> insertList = new ArrayList<>();
        List<SysUser> updateList = new ArrayList<>();
        List<SysUserRole> userRoleDOList = new ArrayList<>();
        // ID生成器
        IdGenerator idGenerator = DefaultIdGeneratorProvider.INSTANCE.getShare();
        for (UserImportRowReq row : importUserList) {
            if (isSkipUserImport(req, row, existUsernames, existPhones, existEmails)) {
                // 按规则跳过该行
                continue;
            }
            SysUser userDO = BeanUtil.toBeanIgnoreError(row, SysUser.class);
            userDO.setStatus(req.getDefaultStatus().getValue());
            userDO.setPwdUpdateTime(LocalDateTime.now());
            userDO.setGender(GenderEnum.getByValue(Integer.parseInt(row.getGender())).getValue());
            userDO.setDeptId(deptMap.get(row.getDeptName()));
            // 修改 or 新增
            if (UPDATE.validate(req.getDuplicateUser(), row.getUsername(), existUsernames)) {
                userDO.setId(userMap.get(row.getUsername()));
                updateList.add(userDO);
            } else {
                userDO.setId(idGenerator.generate());
                userDO.setIsBuiltin(false);
                insertList.add(userDO);
            }
            userRoleDOList.add(new SysUserRole(userDO.getId(), roleMap.get(row.getRoleName())));
        }
        doImportUser(insertList, updateList, userRoleDOList);
        RedisUtils.delete(CacheConstants.DATA_IMPORT_KEY + req.getImportKey());
        return new UserImportResp(insertList.size() + updateList.size(), insertList.size(), updateList.size());
    }


    @Override
    public void export(UserQuery query, SortQuery sortQuery, HttpServletResponse response) {
        // 构建查询条件
        QueryWrapper<SysUser> queryWrapper = this.buildQueryWrapper(query);
        QueryWrapperUtil.applySort(queryWrapper, sortQuery.getSort(), SysUser.class);

        // 查询用户列表
        List<UserDetailResult> userList = userMapper.selectUserList(queryWrapper);

        // 导出Excel
        ExcelUtils.export(userList, "用户数据", UserDetailResult.class, response);
    }

    @Override
    public void resetPassword(UserPasswordResetRequest resetRequest, Long id) {
        String rawPassword = resetRequest.getNewPassword();

        SecurityConfigVO loginConfig = configService.getSecurityConfig();
        userMapper.lambdaUpdate()
                .set(SysUser::getPassword, passwordEncoder.encode(rawPassword))
                .set(SysUser::getPwdExpireDate, LocalDateTime.now().plusDays(loginConfig.getPasswordExpireDays()))
                .eq(SysUser::getId, id)
                .update();
    }

    @Override
    public String resetPassword(Long id) {
        SysUser userDO = userMapper.selectById(id);
        if (userDO == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }
        // 生成12位安全随机密码
        String newPassword = generateSecurePassword(12);

        // 重置密码
        UserPasswordResetRequest req = new UserPasswordResetRequest();
        req.setNewPassword(newPassword);
        this.resetPassword(req, id);

        String retryKey = "login:retry:" + userDO.getUsername() + ":*";
        RedisUtils.deleteByPattern(retryKey);
        return newPassword;
    }

    /**
     * 生成安全的随机密码
     *
     * @param length 密码长度
     * @return 随机密码
     */
    private String generateSecurePassword(int length) {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*";
        String allChars = upperCase + lowerCase + digits + specialChars;

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // 确保至少包含一个大写字母、小写字母、数字和特殊字符
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // 填充剩余字符
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱字符顺序
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    @Override
    public void updateRole(UserRoleUpdateReq updateReq, Long id) {
        this.getById(id);
        List<Long> roleIds = updateReq.getRoleIds();
        // 保存用户和角色关联
        roleService.assignRolesToUser(roleIds, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateAvatar(MultipartFile avatarFile, Long userId) {

        // 校验头像文件类型和大小
        checkAvatar(avatarFile);

        UserDetailResult user = this.detail(userId);

        // 上传新头像
        SysFile avatar = uploadAvatarFile(avatarFile, userId);

        // 更新用户头像
        userMapper.lambdaUpdate().set(SysUser::getAvatar, avatar.getOssUrl()).eq(SysUser::getId, userId).update();

        // 删除旧头像文件
//        deleteOldAvatarFile(user);

        return avatar.getOssUrl();
    }


    private void deleteOldAvatarFile(UserDetailResult user) {
        String oldAvatar = user.getAvatar();
        if (CharSequenceUtil.isNotBlank(oldAvatar)) {
            fileService.delete(user.getId(), "user_avatar");
        }
    }

    private SysFile uploadAvatarFile(MultipartFile avatarFile, Long userId) {
        String avatarPath = "/user/avatar";
        FileRequest fileRequest = new FileRequest();
        fileRequest.setBizId(userId);
        fileRequest.setBizType("user_avatar");
        fileRequest.setPath(avatarPath);
        return fileService.upload(avatarFile, avatarPath);
    }

    private Long getAvatarFileId(String bizId, String bizType) {
        FileQuery fileQuery = new FileQuery();
        fileQuery.setBizId(bizId);
        fileQuery.setBizType(bizType);
        List<SysFile> files = fileService.list(fileQuery);
        if (CollUtil.isEmpty(files)) {
            return null;
        }
        return files.get(0).getId();
    }

    /**
     * 校验头像文件类型和大小
     *
     * @param avatarFile
     */
    private void checkAvatar(MultipartFile avatarFile) {
        String avatarImageType = FileNameUtil.extName(avatarFile.getOriginalFilename());
        BizAssert.isTrue(!CharSequenceUtil.equalsAnyIgnoreCase(avatarImageType, avatarSupportSuffix), "头像仅支持 {} 格式的图片", String
                .join(StringConstants.COMMA, avatarSupportSuffix));

        long avatarMaxSize = 1024 * 1024 * 2; // 2MB
        long avatarSize = avatarFile.getSize();
        if (avatarSize > avatarMaxSize) {
            throw new BusinessException("FILE_SIZE_EXCEEDED", StrUtil.format("头像大小不能超过 {} MB", avatarMaxSize / 1024 / 1024));
        }
    }

    @Override
    @CacheUpdate(key = "#id", value = "#req.nickname", name = CacheConstants.USER_KEY_PREFIX)
    public void updateBasicInfo(UserBasicInfoUpdateReq req, Long id) {
        this.detail(id);
        userMapper.lambdaUpdate()
                .set(SysUser::getNickname, req.getNickname())
                .set(SysUser::getGender, req.getGender())
                .eq(SysUser::getId, id)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPassword, String newPassword, Long id) {
        BizAssert.throwIfEqual(newPassword, oldPassword, "新密码不能与当前密码相同");
        SysUser oldUser = this.getById(id);
        if (CharSequenceUtil.isNotBlank(oldUser.getPassword())) {
            BizAssert.isTrue(!passwordEncoder.matches(oldPassword, oldUser.getPassword()), "当前密码不正确");
        }
        // 校验密码合法性
        int passwordRepetitionTimes = this.checkPassword(newPassword, oldUser);
        // 更新密码和密码重置时间
        userMapper.lambdaUpdate()
                .set(SysUser::getPassword, newPassword)
                .set(SysUser::getPwdUpdateTime, LocalDateTime.now())
                .eq(SysUser::getId, id)
                .update();
        // 保存历史密码
        addPasswordHistory(id, oldUser.getPassword(), passwordRepetitionTimes);
        // 修改后登出
        StpUtil.logout();
    }

    private void addPasswordHistory(Long userId, String password, int passwordRepetitionTimes) {
        passwordHistoryMapper.insert(new SysUserPasswordHistory(userId, password));
        // 删除过期历史密码
        passwordHistoryMapper.deleteExpired(userId, passwordRepetitionTimes);
    }


    @Override
    public void updatePhone(String newPhone, String oldPassword, Long id) {
        SysUser user = userMapper.selectById(id);
        BizAssert.isTrue(!passwordEncoder.matches(oldPassword, user.getPassword()), "当前密码不正确");
        BizAssert.throwIfEqual(newPhone, user.getPhone(), "新手机号不能与当前手机号相同");
        this.checkPhoneUnique(newPhone, id);
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPhone(newPhone);
        // 更新
        userMapper.lambdaUpdate().update(updateUser);
    }

    @Override
    public void updateEmail(String newEmail, String oldPassword, Long id) {
        SysUser user = this.getById(id);
        BizAssert.isTrue(!passwordEncoder.matches(oldPassword, user.getPassword()), "当前密码不正确");
        BizAssert.throwIfEqual(newEmail, user.getEmail(), "新邮箱不能与当前邮箱相同");
        this.checkEmailUnique(newEmail, id);

        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setEmail(newEmail);

        // 更新
        userMapper.lambdaUpdate().update(updateUser);
    }

    @Override
    public SysUser getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public SysUser getByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public SysUser getByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public Long countByDeptIds(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return 0L;
        }
        return userMapper.lambdaQuery().in(SysUser::getDeptId, deptIds).count();
    }

    @Override
    public List<String> findUserPermissions(Long userId) {
        List<String> roleCodeSet = this.findUserRoles(userId);
        // 超级管理员赋予全部权限
        if (roleCodeSet.contains(RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return List.of("*:*:*");
        }
        return menuMapper.selectPermissionByUserId(userId);
    }

    @Override
    public List<String> findUserRoles(Long userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }

    private QueryWrapper<SysUser> buildQueryWrapper(UserQuery query) {
        String description = query.getKeyword();
        StatusEnum status = query.getStatus();
        List<LocalDateTime> createTimeList = query.getCreateTime();
        Long deptId = query.getDeptId();
        List<Long> userIdList = query.getUserIds();
        // 获取排除用户 ID 列表
        List<Long> excludeUserIdList = null;
        if (query.getRoleId() != null) {
            excludeUserIdList = roleService.listMemberIds(query.getRoleId());
        }
        return new QueryWrapper<SysUser>().and(CharSequenceUtil.isNotBlank(description),
                        q -> q.like("t1.username", description)
                                .or()
                                .like("t1.nickname", description)
                                .or()
                                .like("t1.description", description))
                .eq(status != null, "t1.status", status)
                .between(CollUtil.isNotEmpty(createTimeList), "t1.create_time", CollUtil.getFirst(createTimeList), CollUtil
                        .getLast(createTimeList))
                .and(deptId != null && !SystemConstants.SUPER_DEPT_ID.equals(deptId), q -> {
                    List<Long> deptIdList = CollUtils.mapToList(deptMapper.listChildren(deptId), SysDept::getId);
                    deptIdList.add(deptId);
                    q.in("t1.dept_id", deptIdList);
                })
                .in(CollUtil.isNotEmpty(userIdList), "t1.id", userIdList)
                .notIn(CollUtil.isNotEmpty(excludeUserIdList), "t1.id", excludeUserIdList);
    }

    /**
     * 导入用户
     *
     * @param insertList     新增用户
     * @param updateList     修改用户
     * @param userRoleList 用户角色关联
     */
    private void doImportUser(List<SysUser> insertList, List<SysUser> updateList, List<SysUserRole> userRoleList) {
        if (CollUtil.isNotEmpty(insertList)) {
            userMapper.insert(insertList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            userMapper.updateBatchById(updateList);
            userRoleMapper.lambdaUpdate().in(SysUserRole::getUserId, CollUtils.mapToList(updateList, SysUser::getId)).remove();
        }
        if (CollUtil.isNotEmpty(userRoleList)) {
            userRoleMapper.insert(userRoleList);
        }
    }

    /**
     * 判断是否跳过导入
     *
     * @param req            导入参数
     * @param row            导入数据
     * @param existUsernames 导入数据中已存在的用户名
     * @param existEmails    导入数据中已存在的邮箱
     * @param existPhones    导入数据中已存在的手机号
     * @return 是否跳过
     */
    private boolean isSkipUserImport(UserImportRequest req,
                                     UserImportRowReq row,
                                     List<String> existUsernames,
                                     List<String> existEmails,
                                     List<String> existPhones) {
        return SKIP.validate(req.getDuplicateUser(), row.getUsername(), existUsernames) || SKIP.validate(req
                .getDuplicateEmail(), row.getEmail(), existEmails) || SKIP.validate(req.getDuplicatePhone(), row
                .getPhone(), existPhones);
    }

    /**
     * 判断是否退出导入
     *
     * @param req            导入参数
     * @param list           导入数据
     * @param existUsernames 导入数据中已存在的用户名
     * @param existEmails    导入数据中已存在的邮箱
     * @param existPhones    导入数据中已存在的手机号
     * @return 是否退出
     */
    private boolean isExitImportUser(UserImportRequest req,
                                     List<UserImportRowReq> list,
                                     List<String> existUsernames,
                                     List<String> existEmails,
                                     List<String> existPhones) {
        return list.stream()
                .anyMatch(row -> EXIT.validate(req.getDuplicateUser(), row.getUsername(), existUsernames) || EXIT
                        .validate(req.getDuplicateEmail(), row.getEmail(), existEmails) || EXIT.validate(req
                        .getDuplicatePhone(), row.getPhone(), existPhones));
    }

    /**
     * 按指定数据集获取数据库已存在的数量
     *
     * @param userRowList 导入的数据源
     * @param rowField    导入数据的字段
     * @param dbField     对比数据库的字段
     * @return 存在的数量
     */
    private int countExistByField(List<UserImportRowReq> userRowList,
                                  Function<UserImportRowReq, String> rowField,
                                  SFunction<SysUser, ?> dbField) {
        List<String> fieldValues = CollUtils.mapToList(userRowList, rowField);
        if (fieldValues.isEmpty()) {
            return 0;
        }
        return Math.toIntExact(userMapper.lambdaQuery().in(dbField, fieldValues).count());
    }

    /**
     * 按指定数据集获取数据库已存在内容
     *
     * @param userRowList 导入的数据源
     * @param rowField    导入数据的字段
     * @param dbField     对比数据库的字段
     * @return 存在的内容
     */
    private List<String> listExistByField(List<UserImportRowReq> userRowList,
                                          Function<UserImportRowReq, String> rowField,
                                          SFunction<SysUser, String> dbField) {
        List<String> fieldValues = CollUtils.mapToList(userRowList, rowField);
        if (fieldValues.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysUser> userList = userMapper.lambdaQuery().select(dbField).in(dbField, fieldValues).list();
        return CollUtils.mapToList(userList, dbField);
    }

    /**
     * 过滤无效的导入用户数据（批量导入不严格校验数据）
     *
     * @param importRowList 导入数据
     */
    private List<UserImportRowReq> filterImportData(List<UserImportRowReq> importRowList) {
        // 校验过滤
        List<UserImportRowReq> list = importRowList.stream()
                .filter(row -> ValidationUtil.validate(row).isEmpty())
                .toList();
        // 用户名去重
        return list.stream()
                .collect(Collectors.toMap(UserImportRowReq::getUsername, user -> user, (existing, replacement) -> existing))
                .values()
                .stream()
                .toList();
    }

    /**
     * 检测密码合法性
     *
     * @param password 密码
     * @param user     用户信息
     * @return 密码允许重复使用次数
     */
    private int checkPassword(String password, SysUser user) {
        SecurityConfigVO securityConfig = configService.getSecurityConfig();
        // 密码最小长度
        PASSWORD_MIN_LENGTH.validate(password, securityConfig.getPasswordMinLength(), user);
        // 密码是否必须包含特殊字符
        PASSWORD_REQUIRE_SYMBOLS.validate(password, securityConfig.getPasswordRequireSpecial() ? 1 : 0, user);
        // 密码是否允许包含正反序账号名
        PASSWORD_ALLOW_CONTAIN_USERNAME.validate(password, securityConfig.getPasswordAllowContainUsername() ? 1 : 0, user);
        // 密码重复使用次数
        int passwordRepetitionTimes = securityConfig.getPasswordRepetitionTimes();
        PASSWORD_REPETITION_TIMES.validate(password, securityConfig.getPasswordRepetitionTimes(), user);
        return passwordRepetitionTimes;
    }

    /**
     * 检查用户名是否重复
     *
     * @param username 用户名
     */
    private void checkUsernameUnique(String username) {
        boolean isExists = userMapper.lambdaQuery()
                .eq(SysUser::getUsername, username)
                .exists();
        if (isExists) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已被占用");
        }
    }

    /**
     * 检查邮箱是否重复
     */
    private void checkEmailUnique(String email, Long selfUserId) {
        boolean isEmailExists = userMapper.lambdaQuery()
                .eq(SysUser::getEmail, email)
                .ne(ObjectUtil.isNotNull(selfUserId), SysUser::getId, selfUserId)
                .exists();
        if (isEmailExists) {
            throw new BusinessException("EMAIL_EXISTS", "邮箱已被占用");
        }
    }

    /**
     * 检查手机号码是否重复
     *
     * @param phone      手机号
     * @param selfUserId 自身用户ID
     */
    private void checkPhoneUnique(String phone, Long selfUserId) {
        boolean isExists = userMapper.lambdaQuery()
                .eq(SysUser::getPhone, phone)
                .ne(ObjectUtil.isNotNull(selfUserId), SysUser::getId, selfUserId)
                .exists();
        if (isExists) {
            throw new BusinessException("PHONE_EXISTS", "手机号已被占用");
        }
    }

    /**
     * 根据用户名获取用户列表
     *
     * @param usernames 用户名列表
     * @return 用户列表
     */
    private List<SysUser> listByUsernames(List<String> usernames) {
        return userMapper.lambdaQuery()
                .in(SysUser::getUsername, usernames)
                .select(SysUser::getId, SysUser::getUsername)
                .list();
    }

    /**
     * 根据 ID 获取用户信息（数据权限）
     *
     * @param id ID
     * @return 用户信息
     */
    private SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        BizAssert.isNull(user, "用户不存在");
        return user;
    }

    /**
     * 统计有效的多级部门数量
     * <p>
     * 支持多级部门路径解析，使用冒号(:)作为层级分隔符
     * 例如：公司A:研发部:前端组 或 研发部
     * </p>
     *
     * @param deptNames 部门名称集合
     * @return 有效部门数量
     */
    private int countValidMultiLevelDepts(Set<String> deptNames) {
        BizAssert.throwIfEmpty(deptNames, "部门名称集合不能为空");

        int validCount = 0;
        List<String> invalidDepts = new ArrayList<>();

        for (String deptName : deptNames) {
            try {
                findDeptByHierarchicalPath(deptName);
                validCount++;
            } catch (Exception e) {
                invalidDepts.add(deptName);
            }
        }

        BizAssert.isTrue(CollUtil.isNotEmpty(invalidDepts), "以下部门无效或存在歧义：{}", String.join(", ", invalidDepts));

        return validCount;
    }

    /**
     * 构建多级部门映射关系
     * <p>
     * 将部门名称列表转换为部门名称到ID的映射，支持多级部门路径解析
     * </p>
     *
     * @param deptNames 部门名称列表
     * @return 部门名称到ID的映射
     */
    private Map<String, Long> buildMultiLevelDeptMapping(List<String> deptNames) {
        BizAssert.throwIfEmpty(deptNames, "部门名称列表不能为空");

        Map<String, Long> deptMap = new HashMap<>();
        for (String deptName : deptNames) {
            SysDept dept = findDeptByHierarchicalPath(deptName);
            BizAssert.isNull(dept, "部门 [{}] 不存在或存在歧义", deptName);
            deptMap.put(deptName, dept.getId());
        }
        return deptMap;
    }

    /**
     * 根据层级路径查找部门
     * <p>
     * 支持两种格式：
     * <ul>
     * <li>多级部门：公司A/研发部/前端组</li>
     * <li>单级部门：研发部</li>
     * </ul>
     * 使用左斜杠/作为层级分隔符，会逐级查找对应的部门
     * </p>
     *
     * @param deptPath 部门路径
     * @return 部门信息，未找到时返回null
     */
    private SysDept findDeptByHierarchicalPath(String deptPath) {
        BizAssert.notBlank(deptPath, "部门路径不能为空");
        return deptPath.contains(StringConstants.SLASH)
                ? findMultiLevelDept(deptPath)
                : findSingleLevelDept(deptPath.trim());
    }

    /**
     * 查找多级部门
     * <p>
     * 从根部门开始逐级查找，确保部门层级关系正确
     * </p>
     *
     * @param deptPath 多级部门路径
     * @return 部门信息，未找到时返回null
     */
    private SysDept findMultiLevelDept(String deptPath) {
        String[] pathParts = deptPath.split(StringConstants.SLASH);
        BizAssert.isTrue(pathParts.length == 0, "部门路径格式错误：{}", deptPath);

        // 从根部门开始逐级查找
        SysDept currentDept = null;
        Long parentId = 0L; // 根部门的parentId为null

        for (String part : pathParts) {
            String trimmedPart = part.trim();
            BizAssert.notBlank(trimmedPart, "部门路径包含空名称：{}", deptPath);

            // 查找当前层级下指定名称的部门
            currentDept = deptMapper.lambdaQuery()
                    .eq(SysDept::getName, trimmedPart)
                    .eq(SysDept::getParentId, parentId)
                    .one();

            BizAssert.isNull(currentDept, "找不到部门 [{}] 在路径 [{}] 中", trimmedPart, deptPath);
            parentId = currentDept.getId(); // 更新父级ID为当前部门ID
        }

        return currentDept;
    }

    /**
     * 查找单级部门
     * <p>
     * 当只提供部门名称时，检查是否存在多个同名部门
     * 如果存在多个同名部门，则要求用户提供完整的层级路径
     * </p>
     *
     * @param deptName 部门名称
     * @return 部门信息，未找到或存在歧义时返回null
     */
    private SysDept findSingleLevelDept(String deptName) {
        // 查找所有同名部门
        List<SysDept> deptList = deptMapper.lambdaQuery().eq(SysDept::getName, deptName).list();

        BizAssert.throwIfEmpty(deptList, "部门 [{}] 不存在", deptName);
        BizAssert.isTrue(deptList.size() > 1, "存在多个同名部门 [{}]，请使用完整层级路径，如：公司名:{}", deptName, deptName);

        return deptList.get(0);
    }

}