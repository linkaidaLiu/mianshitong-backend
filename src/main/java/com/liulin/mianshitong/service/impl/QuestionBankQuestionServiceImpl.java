package com.liulin.mianshitong.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liulin.mianshitong.common.ErrorCode;
import com.liulin.mianshitong.constant.CommonConstant;
import com.liulin.mianshitong.exception.BusinessException;
import com.liulin.mianshitong.exception.ThrowUtils;
import com.liulin.mianshitong.mapper.QuestionBankQuestionMapper;
import com.liulin.mianshitong.model.dto.questionbankquestion.QuestionbankquestionQueryRequest;
import com.liulin.mianshitong.model.entity.Question;
import com.liulin.mianshitong.model.entity.QuestionBank;
import com.liulin.mianshitong.model.entity.QuestionBankQuestion;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionBankQuestionVO;
import com.liulin.mianshitong.model.vo.UserVO;
import com.liulin.mianshitong.service.QuestionService;
import com.liulin.mianshitong.service.QuestionBankService;
import com.liulin.mianshitong.service.QuestionBankQuestionService;
import com.liulin.mianshitong.service.UserService;
import com.liulin.mianshitong.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题目题库关联表服务实现
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionbankService;

    /**
     * 校验数据
     *
     * @param questionbankquestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionbankquestion(QuestionBankQuestion questionbankquestion, boolean add) {
        ThrowUtils.throwIf(questionbankquestion == null, ErrorCode.PARAMS_ERROR);
        //题目和题库必须存在
        Long questionId = questionbankquestion.getQuestionId();
        if(questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long questionBankId = questionbankquestion.getQuestionBankId();
        if(questionBankId != null) {
            QuestionBank questionbank = questionbankService.getById(questionBankId);
            ThrowUtils.throwIf(questionbank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
        //不需要校验
//        // todo 从对象中取值
//        String title = questionbankquestion.getTitle();
//        // 创建数据时，参数不能为空
//        if (add) {
//            // todo 补充校验规则
//            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
//        }
//        // 修改数据时，有参数则校验
//        // todo 补充校验规则
//        if (StringUtils.isNotBlank(title)) {
//            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
//        }
    }

    /**
     * 获取查询条件
     *
     * @param questionbankquestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionbankquestionQueryRequest questionbankquestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionbankquestionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionbankquestionQueryRequest.getId();
        Long notId = questionbankquestionQueryRequest.getNotId();
        String sortField = questionbankquestionQueryRequest.getSortField();
        String sortOrder = questionbankquestionQueryRequest.getSortOrder();
        Long userId = questionbankquestionQueryRequest.getUserId();
        Long questionId = questionbankquestionQueryRequest.getQuestionId();
        Long questionBankId = questionbankquestionQueryRequest.getQuestionBankId();
        // todo 补充需要的查询条件
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目题库关联表封装
     *
     * @param questionbankquestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionbankquestionVO(QuestionBankQuestion questionbankquestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionbankquestionVO = QuestionBankQuestionVO.objToVo(questionbankquestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionbankquestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionbankquestionVO.setUser(userVO);

        // endregion

        return questionbankquestionVO;
    }

    /**
     * 分页获取题目题库关联表封装
     *
     * @param questionbankquestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionbankquestionVOPage(Page<QuestionBankQuestion> questionbankquestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionbankquestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionbankquestionVOPage = new Page<>(questionbankquestionPage.getCurrent(), questionbankquestionPage.getSize(), questionbankquestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionbankquestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionbankquestion -> {
            return QuestionBankQuestionVO.objToVo(questionbankquestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionbankquestionVOPage.setRecords(questionBankQuestionVOList);
        return questionbankquestionVOPage;
    }

    /**
     * 批量添加题目到题库
     *
     * @param questionIdList
     * @param questionBankId
     * @param loginUser
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBank(List<Long> questionIdList, long questionBankId, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表不能为空");
        ThrowUtils.throwIf(questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库id异常");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //检查题目id是否存在
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)
                .in(Question::getId, questionIdList);
        // 合法的题目 id 列表
        List<Long> validQuestionIdList = questionService.listObjs(questionLambdaQueryWrapper, obj -> (Long) obj);
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "合法的题目 id 列表为空");
        // 检查哪些题目还不存在于题库中，避免重复插入
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<QuestionBankQuestion> existQuestionList = this.list(lambdaQueryWrapper);
        // 已存在于题库中的题目 id
        Set<Long> existQuestionIdSet = existQuestionList.stream()
                .map(QuestionBankQuestion::getId)
                .collect(Collectors.toSet());
        // 已存在于题库中的题目 id，不需要再次添加
        validQuestionIdList = validQuestionIdList.stream().filter(questionId -> {
            return !existQuestionIdSet.contains(questionId);
        }).collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "所有题目均已存在于题库中");
        //检查题库id是否存在
        QuestionBank questionbank = questionbankService.getById(questionBankId);
        ThrowUtils.throwIf(questionbank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        // 自定义线程池,io密集型
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                4,                         // 核心线程数
                10,                        // 最大线程数
                60L,                       // 线程空闲存活时间
                TimeUnit.SECONDS,           // 存活时间单位
                new LinkedBlockingQueue<>(1000),  // 阻塞队列容量
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程处理任务
        );
        //用于保存所有批次的CompletableFuture对象
        List<CompletableFuture<Void>>futures=new ArrayList<>();
        //分批处理，避免长事务，假设每次处理1000条数据
        int batchSize = 1000;
        int totalQuestionListSize = validQuestionIdList.size();
        for(int i=0;i<totalQuestionListSize;i+=batchSize){
            List<Long> subList = validQuestionIdList.subList(i, Math.min(i + batchSize, totalQuestionListSize));
            List<QuestionBankQuestion> questionBankQuestions = subList.stream()
                    .map(questionId -> {
                        QuestionBankQuestion questionbankquestion = new QuestionBankQuestion();
                        questionbankquestion.setQuestionBankId(questionBankId);
                        questionbankquestion.setQuestionId(questionId);
                        questionbankquestion.setUserId(loginUser.getId());
                        return questionbankquestion;
                    })
                    .collect(Collectors.toList());
            //使用事务处理每批数据
            QuestionBankQuestionService questionbankquestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();
            //异步处理每批次数据并添加到futures列表
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionbankquestionService.batchAddQuestionsToBankInner(questionBankQuestions);
            }, customExecutor);
            futures.add(future);
        }
        //等待所有批次处理完
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        //关闭线程池
        customExecutor.shutdown();
    }

    /**
     * 批量添加题目到题库
     * @param questionBankQuestions
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions) {
        try {
            boolean result = this.saveBatch(questionBankQuestions);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        } catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误，错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }


    /**
     * 批量从题库移除题目
     *
     * @param questionIdList
     * @param questionBankId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIdList, long questionBankId) {
        //参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表不能为空");
        ThrowUtils.throwIf(questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库id异常");
        //执行插入
        for (Long questionId : questionIdList) {
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            boolean result = this.remove(lambdaQueryWrapper);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "从题库移除题目失败");
        }
    }

}
