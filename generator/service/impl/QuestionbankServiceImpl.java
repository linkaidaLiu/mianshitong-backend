package com.liulin.mianshitong.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liulin.mianshitong.common.ErrorCode;
import com.liulin.mianshitong.constant.CommonConstant;
import com.liulin.mianshitong.exception.ThrowUtils;
import com.liulin.mianshitong.mapper.QuestionbankMapper;
import com.liulin.mianshitong.model.dto.questionbank.QuestionbankQueryRequest;
import com.liulin.mianshitong.model.entity.Questionbank;
import com.liulin.mianshitong.model.entity.QuestionbankFavour;
import com.liulin.mianshitong.model.entity.QuestionbankThumb;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionbankVO;
import com.liulin.mianshitong.model.vo.UserVO;
import com.liulin.mianshitong.service.QuestionbankService;
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
 * 题库服务实现
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@Service
@Slf4j
public class QuestionbankServiceImpl extends ServiceImpl<QuestionbankMapper, Questionbank> implements QuestionbankService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param questionbank
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionbank(Questionbank questionbank, boolean add) {
        ThrowUtils.throwIf(questionbank == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = questionbank.getTitle();
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
     * @param questionbankQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Questionbank> getQueryWrapper(QuestionbankQueryRequest questionbankQueryRequest) {
        QueryWrapper<Questionbank> queryWrapper = new QueryWrapper<>();
        if (questionbankQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionbankQueryRequest.getId();
        Long notId = questionbankQueryRequest.getNotId();
        String title = questionbankQueryRequest.getTitle();
        String content = questionbankQueryRequest.getContent();
        String searchText = questionbankQueryRequest.getSearchText();
        String sortField = questionbankQueryRequest.getSortField();
        String sortOrder = questionbankQueryRequest.getSortOrder();
        List<String> tagList = questionbankQueryRequest.getTags();
        Long userId = questionbankQueryRequest.getUserId();
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
     * 获取题库封装
     *
     * @param questionbank
     * @param request
     * @return
     */
    @Override
    public QuestionbankVO getQuestionbankVO(Questionbank questionbank, HttpServletRequest request) {
        // 对象转封装类
        QuestionbankVO questionbankVO = QuestionbankVO.objToVo(questionbank);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionbank.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionbankVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long questionbankId = questionbank.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<QuestionbankThumb> questionbankThumbQueryWrapper = new QueryWrapper<>();
            questionbankThumbQueryWrapper.in("questionbankId", questionbankId);
            questionbankThumbQueryWrapper.eq("userId", loginUser.getId());
            QuestionbankThumb questionbankThumb = questionbankThumbMapper.selectOne(questionbankThumbQueryWrapper);
            questionbankVO.setHasThumb(questionbankThumb != null);
            // 获取收藏
            QueryWrapper<QuestionbankFavour> questionbankFavourQueryWrapper = new QueryWrapper<>();
            questionbankFavourQueryWrapper.in("questionbankId", questionbankId);
            questionbankFavourQueryWrapper.eq("userId", loginUser.getId());
            QuestionbankFavour questionbankFavour = questionbankFavourMapper.selectOne(questionbankFavourQueryWrapper);
            questionbankVO.setHasFavour(questionbankFavour != null);
        }
        // endregion

        return questionbankVO;
    }

    /**
     * 分页获取题库封装
     *
     * @param questionbankPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionbankVO> getQuestionbankVOPage(Page<Questionbank> questionbankPage, HttpServletRequest request) {
        List<Questionbank> questionbankList = questionbankPage.getRecords();
        Page<QuestionbankVO> questionbankVOPage = new Page<>(questionbankPage.getCurrent(), questionbankPage.getSize(), questionbankPage.getTotal());
        if (CollUtil.isEmpty(questionbankList)) {
            return questionbankVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionbankVO> questionbankVOList = questionbankList.stream().map(questionbank -> {
            return QuestionbankVO.objToVo(questionbank);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionbankList.stream().map(Questionbank::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> questionbankIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> questionbankIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> questionbankIdSet = questionbankList.stream().map(Questionbank::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<QuestionbankThumb> questionbankThumbQueryWrapper = new QueryWrapper<>();
            questionbankThumbQueryWrapper.in("questionbankId", questionbankIdSet);
            questionbankThumbQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionbankThumb> questionbankQuestionbankThumbList = questionbankThumbMapper.selectList(questionbankThumbQueryWrapper);
            questionbankQuestionbankThumbList.forEach(questionbankQuestionbankThumb -> questionbankIdHasThumbMap.put(questionbankQuestionbankThumb.getQuestionbankId(), true));
            // 获取收藏
            QueryWrapper<QuestionbankFavour> questionbankFavourQueryWrapper = new QueryWrapper<>();
            questionbankFavourQueryWrapper.in("questionbankId", questionbankIdSet);
            questionbankFavourQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionbankFavour> questionbankFavourList = questionbankFavourMapper.selectList(questionbankFavourQueryWrapper);
            questionbankFavourList.forEach(questionbankFavour -> questionbankIdHasFavourMap.put(questionbankFavour.getQuestionbankId(), true));
        }
        // 填充信息
        questionbankVOList.forEach(questionbankVO -> {
            Long userId = questionbankVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionbankVO.setUser(userService.getUserVO(user));
            questionbankVO.setHasThumb(questionbankIdHasThumbMap.getOrDefault(questionbankVO.getId(), false));
            questionbankVO.setHasFavour(questionbankIdHasFavourMap.getOrDefault(questionbankVO.getId(), false));
        });
        // endregion

        questionbankVOPage.setRecords(questionbankVOList);
        return questionbankVOPage;
    }

}
