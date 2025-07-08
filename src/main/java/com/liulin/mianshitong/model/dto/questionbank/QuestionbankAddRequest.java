package com.liulin.mianshitong.model.dto.questionbank;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建题库请求
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@Data
public class QuestionbankAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;
    /**
     * 图片
     */
    private String picture;


    private static final long serialVersionUID = 1L;
}