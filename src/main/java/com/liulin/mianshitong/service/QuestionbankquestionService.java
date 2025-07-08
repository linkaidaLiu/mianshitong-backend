package com.liulin.mianshitong.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liulin.mianshitong.model.dto.questionbankquestion.QuestionbankquestionQueryRequest;
import com.liulin.mianshitong.model.entity.Questionbankquestion;
import com.liulin.mianshitong.model.vo.QuestionbankquestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 题目题库关联表服务
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
public interface QuestionbankquestionService extends IService<Questionbankquestion> {

    /**
     * 校验数据
     *
     * @param questionbankquestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionbankquestion(Questionbankquestion questionbankquestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionbankquestionQueryRequest
     * @return
     */
    QueryWrapper<Questionbankquestion> getQueryWrapper(QuestionbankquestionQueryRequest questionbankquestionQueryRequest);
    
    /**
     * 获取题目题库关联表封装
     *
     * @param questionbankquestion
     * @param request
     * @return
     */
    QuestionbankquestionVO getQuestionbankquestionVO(Questionbankquestion questionbankquestion, HttpServletRequest request);

    /**
     * 分页获取题目题库关联表封装
     *
     * @param questionbankquestionPage
     * @param request
     * @return
     */
    Page<QuestionbankquestionVO> getQuestionbankquestionVOPage(Page<Questionbankquestion> questionbankquestionPage, HttpServletRequest request);
}
