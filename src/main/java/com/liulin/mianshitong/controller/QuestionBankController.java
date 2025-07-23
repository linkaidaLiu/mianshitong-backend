package com.liulin.mianshitong.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.liulin.mianshitong.annotation.AuthCheck;
import com.liulin.mianshitong.common.BaseResponse;
import com.liulin.mianshitong.common.DeleteRequest;
import com.liulin.mianshitong.common.ErrorCode;
import com.liulin.mianshitong.common.ResultUtils;
import com.liulin.mianshitong.constant.UserConstant;
import com.liulin.mianshitong.exception.BusinessException;
import com.liulin.mianshitong.exception.ThrowUtils;
import com.liulin.mianshitong.model.dto.question.QuestionQueryRequest;
import com.liulin.mianshitong.model.dto.questionbank.QuestionBankAddRequest;
import com.liulin.mianshitong.model.dto.questionbank.QuestionBankEditRequest;
import com.liulin.mianshitong.model.dto.questionbank.QuestionBankQueryRequest;
import com.liulin.mianshitong.model.dto.questionbank.QuestionBankUpdateRequest;
import com.liulin.mianshitong.model.entity.Question;
import com.liulin.mianshitong.model.entity.QuestionBank;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionVO;
import com.liulin.mianshitong.model.vo.QuestionBankVO;
import com.liulin.mianshitong.service.QuestionService;
import com.liulin.mianshitong.service.QuestionBankService;
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
public class QuestionBankController {

    @Resource
    private QuestionBankService questionbankService;

    @Resource
    private QuestionService questionService;
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionbank(@RequestBody QuestionBankAddRequest questionbankAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionbankAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBank questionbank = new QuestionBank();
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionbank(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBank oldQuestionBank = questionbankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBank.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
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
    public BaseResponse<Boolean> updateQuestionbank(@RequestBody QuestionBankUpdateRequest questionbankUpdateRequest) {
        if (questionbankUpdateRequest == null || questionbankUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBank questionbank = new QuestionBank();
        BeanUtils.copyProperties(questionbankUpdateRequest, questionbank);
        // 数据校验
        questionbankService.validQuestionbank(questionbank, false);
        // 判断是否存在
        long id = questionbankUpdateRequest.getId();
        QuestionBank oldQuestionBank = questionbankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionbankService.updateById(questionbank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题库（封装类）
     *
     * @param questionbankQueryRequest
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionBankVO> getQuestionbankVOById(QuestionBankQueryRequest questionbankQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionbankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = questionbankQueryRequest.getId();
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        //生成key
        String key="bank_detail_"+id;
        //如果是热key
        if(JdHotKeyStore.isHotKey(key)){
            //从本地缓存中获取缓存值
            Object cachedQuestionBankVo = JdHotKeyStore.get(key);
            if(cachedQuestionBankVo!=null){
                //如果缓存中有值，直接返回缓存的值
                return ResultUtils.success((QuestionBankVO) cachedQuestionBankVo);
            }
        }
        // 查询数据库
        QuestionBank questionbank = questionbankService.getById(id);
        ThrowUtils.throwIf(questionbank == null, ErrorCode.NOT_FOUND_ERROR);
        //查询题库分装类
        QuestionBankVO questionbankVO = questionbankService.getQuestionbankVO(questionbank, request);
        //是否需要关联查询题库下的题目列表
        boolean needQueryQuestionList = questionbankQueryRequest.isNeedQueryQuestionList();
        if (needQueryQuestionList) {
            QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
            questionQueryRequest.setQuestionBankId(id);
            // 可以按需支持更多的题目搜索参数，比如分页
            questionQueryRequest.setPageSize(questionbankQueryRequest.getPageSize());
            questionQueryRequest.setCurrent(questionbankQueryRequest.getCurrent());
            Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
            Page<QuestionVO> questionVOPage = questionService.getQuestionVOPage(questionPage, request);
            questionbankVO.setQuestionPage(questionVOPage);
        }

        //设置本地缓存(如果不是热key，该方法不会设置热key)
        JdHotKeyStore.smartSet(key, questionbankVO);
        // 获取封装类
        return ResultUtils.success(questionbankVO);
    }

    /**
     * 分页获取题库列表（仅管理员可用）
     *
     * @param questionbankQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBank>> listQuestionbankByPage(@RequestBody QuestionBankQueryRequest questionbankQueryRequest) {
        long current = questionbankQueryRequest.getCurrent();
        long size = questionbankQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionBank> questionbankPage = questionbankService.page(new Page<>(current, size),
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
    @SentinelResource(value = "listQuestionBankVOByPage",
            blockHandler = "handleBlockException",
            fallback = "handleFallback")
    public BaseResponse<Page<QuestionBankVO>> listQuestionbankVOByPage(@RequestBody QuestionBankQueryRequest questionbankQueryRequest,
                                                                       HttpServletRequest request) {
        long current = questionbankQueryRequest.getCurrent();
        long size = questionbankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBank> questionbankPage = questionbankService.page(new Page<>(current, size),
                questionbankService.getQueryWrapper(questionbankQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionbankService.getQuestionbankVOPage(questionbankPage, request));
    }

    /**
     * listQuestionBankVOByPage 降级操作：直接返回本地数据
     */
    public BaseResponse<Page<QuestionBankVO>> handleFallback(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                             HttpServletRequest request, Throwable ex) {
        // 可以返回本地数据或空数据
        return ResultUtils.success(null);
    }

    /**
     * listQuestionBankVOByPage 流控操作
     * 限流：提示“系统压力过大，请耐心等待”
     */
    public BaseResponse<Page<QuestionBankVO>> handleBlockException(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                                   HttpServletRequest request, BlockException ex) {
        //降级操作
        if(ex instanceof DegradeException){
            return handleFallback(questionBankQueryRequest, request, ex);
        }
        // 限流操作
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统压力过大，请耐心等待");
    }

    /**
     * 分页获取当前登录用户创建的题库列表
     *
     * @param questionbankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankVO>> listMyQuestionbankVOByPage(@RequestBody QuestionBankQueryRequest questionbankQueryRequest,
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
        Page<QuestionBank> questionbankPage = questionbankService.page(new Page<>(current, size),
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
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editQuestionbank(@RequestBody QuestionBankEditRequest questionbankEditRequest, HttpServletRequest request) {
        if (questionbankEditRequest == null || questionbankEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBank questionbank = new QuestionBank();
        BeanUtils.copyProperties(questionbankEditRequest, questionbank);
        // 数据校验
        questionbankService.validQuestionbank(questionbank, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionbankEditRequest.getId();
        QuestionBank oldQuestionBank = questionbankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestionBank.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionbankService.updateById(questionbank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
