package com.liulin.mianshitong.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liulin.mianshitong.annotation.AuthCheck;
import com.liulin.mianshitong.common.BaseResponse;
import com.liulin.mianshitong.common.DeleteRequest;
import com.liulin.mianshitong.common.ErrorCode;
import com.liulin.mianshitong.common.ResultUtils;
import com.liulin.mianshitong.constant.UserConstant;
import com.liulin.mianshitong.exception.BusinessException;
import com.liulin.mianshitong.exception.ThrowUtils;
import com.liulin.mianshitong.model.dto.questionbankquestion.*;
import com.liulin.mianshitong.model.entity.QuestionBankQuestion;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionBankQuestionVO;
import com.liulin.mianshitong.service.QuestionBankQuestionService;
import com.liulin.mianshitong.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目题库关联表接口
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@RestController
@RequestMapping("/questionbankquestion")
@Slf4j
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService questionbankquestionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题目题库关联表
     *
     * @param questionbankquestionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionbankquestion(
            @RequestBody QuestionbankquestionAddRequest questionbankquestionAddRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(questionbankquestionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion questionbankquestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(questionbankquestionAddRequest, questionbankquestion);
        // 数据校验
        questionbankquestionService.validQuestionbankquestion(questionbankquestion, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        questionbankquestion.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionbankquestionService.save(questionbankquestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionbankquestionId = questionbankquestion.getId();
        return ResultUtils.success(newQuestionbankquestionId);
    }

    /**
     * 删除题目题库关联表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionbankquestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBankQuestion oldQuestionBankQuestion = questionbankquestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBankQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionbankquestionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 移除题目题库关联表
     *
     * @param removeRequest
     * @return
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> removeQuestionbankquestion(@RequestBody QuestionBankQuestionRemoveRequest removeRequest) {
        ThrowUtils.throwIf(removeRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = removeRequest.getQuestionBankId();
        Long questionId = removeRequest.getQuestionId();
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .eq(QuestionBankQuestion::getQuestionId, questionId);
        boolean result = questionbankquestionService.remove(lambdaQueryWrapper);
        return ResultUtils.success(result);
    }


    /**
     * 更新题目题库关联表（仅管理员可用）
     *
     * @param questionbankquestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionbankquestion(@RequestBody QuestionbankquestionUpdateRequest questionbankquestionUpdateRequest) {
        if (questionbankquestionUpdateRequest == null || questionbankquestionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion questionbankquestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(questionbankquestionUpdateRequest, questionbankquestion);
        // 数据校验
        questionbankquestionService.validQuestionbankquestion(questionbankquestion, false);
        // 判断是否存在
        long id = questionbankquestionUpdateRequest.getId();
        QuestionBankQuestion oldQuestionBankQuestion = questionbankquestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionbankquestionService.updateById(questionbankquestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目题库关联表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionBankQuestionVO> getQuestionbankquestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QuestionBankQuestion questionbankquestion = questionbankquestionService.getById(id);
        ThrowUtils.throwIf(questionbankquestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionbankquestionService.getQuestionbankquestionVO(questionbankquestion, request));
    }

    /**
     * 分页获取题目题库关联表列表（仅管理员可用）
     *
     * @param questionbankquestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankQuestion>> listQuestionbankquestionByPage(@RequestBody QuestionbankquestionQueryRequest questionbankquestionQueryRequest) {
        long current = questionbankquestionQueryRequest.getCurrent();
        long size = questionbankquestionQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionBankQuestion> questionbankquestionPage = questionbankquestionService.page(new Page<>(current, size),
                questionbankquestionService.getQueryWrapper(questionbankquestionQueryRequest));
        return ResultUtils.success(questionbankquestionPage);
    }

    /**
     * 分页获取题目题库关联表列表（封装类）
     *
     * @param questionbankquestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listQuestionbankquestionVOByPage(@RequestBody QuestionbankquestionQueryRequest questionbankquestionQueryRequest,
                                                                                       HttpServletRequest request) {
        long current = questionbankquestionQueryRequest.getCurrent();
        long size = questionbankquestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBankQuestion> questionbankquestionPage = questionbankquestionService.page(new Page<>(current, size),
                questionbankquestionService.getQueryWrapper(questionbankquestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionbankquestionService.getQuestionbankquestionVOPage(questionbankquestionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题目题库关联表列表
     *
     * @param questionbankquestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listMyQuestionbankquestionVOByPage(@RequestBody QuestionbankquestionQueryRequest questionbankquestionQueryRequest,
                                                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(questionbankquestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionbankquestionQueryRequest.setUserId(loginUser.getId());
        long current = questionbankquestionQueryRequest.getCurrent();
        long size = questionbankquestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBankQuestion> questionbankquestionPage = questionbankquestionService.page(new Page<>(current, size),
                questionbankquestionService.getQueryWrapper(questionbankquestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionbankquestionService.getQuestionbankquestionVOPage(questionbankquestionPage, request));
    }

    /**
     * 批量添加题目到题库
     *
     * @param questionBankQuestionBatchAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add/batch")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchAddQuestionsToBank(
            @RequestBody QuestionbankquestionBatchAddRequest questionBankQuestionBatchAddRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(questionBankQuestionBatchAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long questionBankId = questionBankQuestionBatchAddRequest.getQuestionBankId();
        List<Long> questionIdList = questionBankQuestionBatchAddRequest.getQuestionIdList();
        questionbankquestionService.batchAddQuestionsToBank(questionIdList, questionBankId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量从题库移除题目
     *
     * @param questionbankquestionBatchRemoveRequest
     * @param request
     * @return
     */
    @PostMapping("/remove/batch")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> batchAddQuestionsToBank(
            @RequestBody QuestionbankquestionBatchRemoveRequest questionbankquestionBatchRemoveRequest,
            HttpServletRequest request
    ) {
        // 参数校验
        ThrowUtils.throwIf(questionbankquestionBatchRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = questionbankquestionBatchRemoveRequest.getQuestionBankId();
        List<Long> questionIdList = questionbankquestionBatchRemoveRequest.getQuestionIdList();
        questionbankquestionService.batchRemoveQuestionsFromBank(questionIdList, questionBankId);
        return ResultUtils.success(true);
    }

    // endregion
}
