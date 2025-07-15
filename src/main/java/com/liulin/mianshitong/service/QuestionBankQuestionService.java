package com.liulin.mianshitong.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liulin.mianshitong.model.dto.questionbankquestion.QuestionbankquestionQueryRequest;
import com.liulin.mianshitong.model.entity.QuestionBankQuestion;
import com.liulin.mianshitong.model.entity.User;
import com.liulin.mianshitong.model.vo.QuestionBankQuestionVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目题库关联表服务
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param questionbankquestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionbankquestion(QuestionBankQuestion questionbankquestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionbankquestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionbankquestionQueryRequest questionbankquestionQueryRequest);
    
    /**
     * 获取题目题库关联表封装
     *
     * @param questionbankquestion
     * @param request
     * @return
     */
    QuestionBankQuestionVO getQuestionbankquestionVO(QuestionBankQuestion questionbankquestion, HttpServletRequest request);

    /**
     * 分页获取题目题库关联表封装
     *
     * @param questionbankquestionPage
     * @param request
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionbankquestionVOPage(Page<QuestionBankQuestion> questionbankquestionPage, HttpServletRequest request);

    /**
     * 批量添加题目到题库
     *
     * @param questionIdList
     * @param questionBankId
     * @param loginUser
     */
    void batchAddQuestionsToBank(List<Long> questionIdList, long questionBankId, User loginUser);
    /**
     * 批量从题库移除题目
     *
     * @param questionIdList
     * @param questionBankId
     */
    void batchRemoveQuestionsFromBank(List<Long> questionIdList, long questionBankId);

    /**
     * 批量添加题目到题库
     * @param questionBankQuestionList
     */
    @Transactional(rollbackFor = Exception.class)
    void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestionList);
}
