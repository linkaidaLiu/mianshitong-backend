package com.liulin.mianshitong.model.dto.questionbankquestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量创建题目题库关联表请求
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@Data
public class QuestionbankquestionBatchAddRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id列表
     */
    private List<Long> questionIdList;
    private static final long serialVersionUID = 1L;
}