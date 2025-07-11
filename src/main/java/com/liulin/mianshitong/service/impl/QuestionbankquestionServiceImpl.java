package com.liulin.mianshitong.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liulin.mianshitong.common.ErrorCode;
import com.liulin.mianshitong.constant.CommonConstant;
import com.liulin.mianshitong.exception.ThrowUtils;
import com.liulin.mianshitong.mapper.QuestionBankQuestionMapper;
import com.liulin.mianshitong.model.dto.questionbankquestion.QuestionbankquestionQueryRequest;
import com.liulin.mianshitong.model.entity.Question;
import com.liulin.mianshitong.model.entity.Questionbank;
import com.liulin.mianshitong.model.entity.Questionbankquestion;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionbankquestionVO;
import com.liulin.mianshitong.model.vo.UserVO;
import com.liulin.mianshitong.service.QuestionService;
import com.liulin.mianshitong.service.QuestionbankService;
import com.liulin.mianshitong.service.QuestionbankquestionService;
import com.liulin.mianshitong.service.UserService;
import com.liulin.mianshitong.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目题库关联表服务实现
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@Service
@Slf4j
public class QuestionbankquestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, Questionbankquestion> implements QuestionbankquestionService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    private QuestionbankService questionbankService;

    /**
     * 校验数据
     *
     * @param questionbankquestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionbankquestion(Questionbankquestion questionbankquestion, boolean add) {
        ThrowUtils.throwIf(questionbankquestion == null, ErrorCode.PARAMS_ERROR);
        //题目和题库必须存在
        Long questionId = questionbankquestion.getQuestionId();
        if(questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long questionBankId = questionbankquestion.getQuestionBankId();
        if(questionBankId != null) {
            Questionbank questionbank = questionbankService.getById(questionBankId);
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
    public QueryWrapper<Questionbankquestion> getQueryWrapper(QuestionbankquestionQueryRequest questionbankquestionQueryRequest) {
        QueryWrapper<Questionbankquestion> queryWrapper = new QueryWrapper<>();
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
    public QuestionbankquestionVO getQuestionbankquestionVO(Questionbankquestion questionbankquestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionbankquestionVO questionbankquestionVO = QuestionbankquestionVO.objToVo(questionbankquestion);

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
    public Page<QuestionbankquestionVO> getQuestionbankquestionVOPage(Page<Questionbankquestion> questionbankquestionPage, HttpServletRequest request) {
        List<Questionbankquestion> questionbankquestionList = questionbankquestionPage.getRecords();
        Page<QuestionbankquestionVO> questionbankquestionVOPage = new Page<>(questionbankquestionPage.getCurrent(), questionbankquestionPage.getSize(), questionbankquestionPage.getTotal());
        if (CollUtil.isEmpty(questionbankquestionList)) {
            return questionbankquestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionbankquestionVO> questionbankquestionVOList = questionbankquestionList.stream().map(questionbankquestion -> {
            return QuestionbankquestionVO.objToVo(questionbankquestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionbankquestionList.stream().map(Questionbankquestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionbankquestionVOList.forEach(questionbankquestionVO -> {
            Long userId = questionbankquestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionbankquestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionbankquestionVOPage.setRecords(questionbankquestionVOList);
        return questionbankquestionVOPage;
    }

}
