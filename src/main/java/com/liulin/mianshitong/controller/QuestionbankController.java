package com.liulin.mianshitong.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liulin.mianshitong.annotation.AuthCheck;
import com.liulin.mianshitong.common.BaseResponse;
import com.liulin.mianshitong.common.DeleteRequest;
import com.liulin.mianshitong.common.ErrorCode;
import com.liulin.mianshitong.common.ResultUtils;
import com.liulin.mianshitong.constant.UserConstant;
import com.liulin.mianshitong.exception.BusinessException;
import com.liulin.mianshitong.exception.ThrowUtils;
import com.liulin.mianshitong.model.dto.questionbank.QuestionbankAddRequest;
import com.liulin.mianshitong.model.dto.questionbank.QuestionbankEditRequest;
import com.liulin.mianshitong.model.dto.questionbank.QuestionbankQueryRequest;
import com.liulin.mianshitong.model.dto.questionbank.QuestionbankUpdateRequest;
import com.liulin.mianshitong.model.entity.Questionbank;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionbankVO;
import com.liulin.mianshitong.service.QuestionbankService;
import com.liulin.mianshitong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题库接口
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@RestController
@RequestMapping("/questionbank")
@Slf4j
public class QuestionbankController {

    @Resource
    private QuestionbankService questionbankService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题库
     *
     * @param questionbankAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionbank(@RequestBody QuestionbankAddRequest questionbankAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionbankAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Questionbank questionbank = new Questionbank();
        BeanUtils.copyProperties(questionbankAddRequest, questionbank);
        // 数据校验
        questionbankService.validQuestionbank(questionbank, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        questionbank.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionbankService.save(questionbank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionbankId = questionbank.getId();
        return ResultUtils.success(newQuestionbankId);
    }

    /**
     * 删除题库
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionbank(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Questionbank oldQuestionbank = questionbankService.getById(id);
        ThrowUtils.throwIf(oldQuestionbank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionbank.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionbankService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题库（仅管理员可用）
     *
     * @param questionbankUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionbank(@RequestBody QuestionbankUpdateRequest questionbankUpdateRequest) {
        if (questionbankUpdateRequest == null || questionbankUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Questionbank questionbank = new Questionbank();
        BeanUtils.copyProperties(questionbankUpdateRequest, questionbank);
        // 数据校验
        questionbankService.validQuestionbank(questionbank, false);
        // 判断是否存在
        long id = questionbankUpdateRequest.getId();
        Questionbank oldQuestionbank = questionbankService.getById(id);
        ThrowUtils.throwIf(oldQuestionbank == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionbankService.updateById(questionbank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题库（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionbankVO> getQuestionbankVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Questionbank questionbank = questionbankService.getById(id);
        ThrowUtils.throwIf(questionbank == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionbankService.getQuestionbankVO(questionbank, request));
    }

    /**
     * 分页获取题库列表（仅管理员可用）
     *
     * @param questionbankQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Questionbank>> listQuestionbankByPage(@RequestBody QuestionbankQueryRequest questionbankQueryRequest) {
        long current = questionbankQueryRequest.getCurrent();
        long size = questionbankQueryRequest.getPageSize();
        // 查询数据库
        Page<Questionbank> questionbankPage = questionbankService.page(new Page<>(current, size),
                questionbankService.getQueryWrapper(questionbankQueryRequest));
        return ResultUtils.success(questionbankPage);
    }

    /**
     * 分页获取题库列表（封装类）
     *
     * @param questionbankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionbankVO>> listQuestionbankVOByPage(@RequestBody QuestionbankQueryRequest questionbankQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionbankQueryRequest.getCurrent();
        long size = questionbankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Questionbank> questionbankPage = questionbankService.page(new Page<>(current, size),
                questionbankService.getQueryWrapper(questionbankQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionbankService.getQuestionbankVOPage(questionbankPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题库列表
     *
     * @param questionbankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionbankVO>> listMyQuestionbankVOByPage(@RequestBody QuestionbankQueryRequest questionbankQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(questionbankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionbankQueryRequest.setUserId(loginUser.getId());
        long current = questionbankQueryRequest.getCurrent();
        long size = questionbankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Questionbank> questionbankPage = questionbankService.page(new Page<>(current, size),
                questionbankService.getQueryWrapper(questionbankQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionbankService.getQuestionbankVOPage(questionbankPage, request));
    }

    /**
     * 编辑题库（给用户使用）
     *
     * @param questionbankEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestionbank(@RequestBody QuestionbankEditRequest questionbankEditRequest, HttpServletRequest request) {
        if (questionbankEditRequest == null || questionbankEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Questionbank questionbank = new Questionbank();
        BeanUtils.copyProperties(questionbankEditRequest, questionbank);
        // 数据校验
        questionbankService.validQuestionbank(questionbank, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionbankEditRequest.getId();
        Questionbank oldQuestionbank = questionbankService.getById(id);
        ThrowUtils.throwIf(oldQuestionbank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestionbank.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionbankService.updateById(questionbank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
