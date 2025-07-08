package com.liulin.mianshitong.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liulin.mianshitong.common.ErrorCode;
import com.liulin.mianshitong.constant.CommonConstant;
import com.liulin.mianshitong.exception.ThrowUtils;
import com.liulin.mianshitong.mapper.QuestionbankquestionMapper;
import com.liulin.mianshitong.model.dto.questionbankquestion.QuestionbankquestionQueryRequest;
import com.liulin.mianshitong.model.entity.Questionbankquestion;
import com.liulin.mianshitong.model.entity.QuestionbankquestionFavour;
import com.liulin.mianshitong.model.entity.QuestionbankquestionThumb;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionbankquestionVO;
import com.liulin.mianshitong.model.vo.UserVO;
import com.liulin.mianshitong.service.QuestionbankquestionService;
import com.liulin.mianshitong.service.UserService;
import com.liulin.mianshitong.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
public class QuestionbankquestionServiceImpl extends ServiceImpl<QuestionbankquestionMapper, Questionbankquestion> implements QuestionbankquestionService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param questionbankquestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionbankquestion(Questionbankquestion questionbankquestion, boolean add) {
        ThrowUtils.throwIf(questionbankquestion == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = questionbankquestion.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
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
        String title = questionbankquestionQueryRequest.getTitle();
        String content = questionbankquestionQueryRequest.getContent();
        String searchText = questionbankquestionQueryRequest.getSearchText();
        String sortField = questionbankquestionQueryRequest.getSortField();
        String sortOrder = questionbankquestionQueryRequest.getSortOrder();
        List<String> tagList = questionbankquestionQueryRequest.getTags();
        Long userId = questionbankquestionQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
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
        // 2. 已登录，获取用户点赞、收藏状态
        long questionbankquestionId = questionbankquestion.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<QuestionbankquestionThumb> questionbankquestionThumbQueryWrapper = new QueryWrapper<>();
            questionbankquestionThumbQueryWrapper.in("questionbankquestionId", questionbankquestionId);
            questionbankquestionThumbQueryWrapper.eq("userId", loginUser.getId());
            QuestionbankquestionThumb questionbankquestionThumb = questionbankquestionThumbMapper.selectOne(questionbankquestionThumbQueryWrapper);
            questionbankquestionVO.setHasThumb(questionbankquestionThumb != null);
            // 获取收藏
            QueryWrapper<QuestionbankquestionFavour> questionbankquestionFavourQueryWrapper = new QueryWrapper<>();
            questionbankquestionFavourQueryWrapper.in("questionbankquestionId", questionbankquestionId);
            questionbankquestionFavourQueryWrapper.eq("userId", loginUser.getId());
            QuestionbankquestionFavour questionbankquestionFavour = questionbankquestionFavourMapper.selectOne(questionbankquestionFavourQueryWrapper);
            questionbankquestionVO.setHasFavour(questionbankquestionFavour != null);
        }
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
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> questionbankquestionIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> questionbankquestionIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> questionbankquestionIdSet = questionbankquestionList.stream().map(Questionbankquestion::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<QuestionbankquestionThumb> questionbankquestionThumbQueryWrapper = new QueryWrapper<>();
            questionbankquestionThumbQueryWrapper.in("questionbankquestionId", questionbankquestionIdSet);
            questionbankquestionThumbQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionbankquestionThumb> questionbankquestionQuestionbankquestionThumbList = questionbankquestionThumbMapper.selectList(questionbankquestionThumbQueryWrapper);
            questionbankquestionQuestionbankquestionThumbList.forEach(questionbankquestionQuestionbankquestionThumb -> questionbankquestionIdHasThumbMap.put(questionbankquestionQuestionbankquestionThumb.getQuestionbankquestionId(), true));
            // 获取收藏
            QueryWrapper<QuestionbankquestionFavour> questionbankquestionFavourQueryWrapper = new QueryWrapper<>();
            questionbankquestionFavourQueryWrapper.in("questionbankquestionId", questionbankquestionIdSet);
            questionbankquestionFavourQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionbankquestionFavour> questionbankquestionFavourList = questionbankquestionFavourMapper.selectList(questionbankquestionFavourQueryWrapper);
            questionbankquestionFavourList.forEach(questionbankquestionFavour -> questionbankquestionIdHasFavourMap.put(questionbankquestionFavour.getQuestionbankquestionId(), true));
        }
        // 填充信息
        questionbankquestionVOList.forEach(questionbankquestionVO -> {
            Long userId = questionbankquestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionbankquestionVO.setUser(userService.getUserVO(user));
            questionbankquestionVO.setHasThumb(questionbankquestionIdHasThumbMap.getOrDefault(questionbankquestionVO.getId(), false));
            questionbankquestionVO.setHasFavour(questionbankquestionIdHasFavourMap.getOrDefault(questionbankquestionVO.getId(), false));
        });
        // endregion

        questionbankquestionVOPage.setRecords(questionbankquestionVOList);
        return questionbankquestionVOPage;
    }

}
