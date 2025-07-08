package com.liulin.mianshitong.model.dto.questionbankquestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新题目题库关联表请求
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@Data
public class QuestionbankquestionUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}