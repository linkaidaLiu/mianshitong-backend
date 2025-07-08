package com.liulin.mianshitong.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liulin.mianshitong.model.dto.questionbank.QuestionbankQueryRequest;
import com.liulin.mianshitong.model.entity.Questionbank;
import com.liulin.mianshitong.model.vo.QuestionbankVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 题库服务
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
public interface QuestionbankService extends IService<Questionbank> {

    /**
     * 校验数据
     *
     * @param questionbank
     * @param add 对创建的数据进行校验
     */
    void validQuestionbank(Questionbank questionbank, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionbankQueryRequest
     * @return
     */
    QueryWrapper<Questionbank> getQueryWrapper(QuestionbankQueryRequest questionbankQueryRequest);
    
    /**
     * 获取题库封装
     *
     * @param questionbank
     * @param request
     * @return
     */
    QuestionbankVO getQuestionbankVO(Questionbank questionbank, HttpServletRequest request);

    /**
     * 分页获取题库封装
     *
     * @param questionbankPage
     * @param request
     * @return
     */
    Page<QuestionbankVO> getQuestionbankVOPage(Page<Questionbank> questionbankPage, HttpServletRequest request);
}
