package com.liulin.mianshitong.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liulin.mianshitong.model.entity.Question;
import com.liulin.mianshitong.model.entity.Questionbank;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 题库视图
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://www.code-nav.cn"></a>
 */
@Data
public class QuestionbankVO implements Serializable {

    /**
     * id
     */
    private Long id;

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

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 题库里的题目列表（分页）
     */
    Page<Question> questionPage;

    /**
     * 封装类转对象
     *
     * @param questionbankVO
     * @return
     */
    public static Questionbank voToObj(QuestionbankVO questionbankVO) {
        if (questionbankVO == null) {
            return null;
        }
        Questionbank questionbank = new Questionbank();
        BeanUtils.copyProperties(questionbankVO, questionbank);
        return questionbank;
    }

    /**
     * 对象转封装类
     *
     * @param questionbank
     * @return
     */
    public static QuestionbankVO objToVo(Questionbank questionbank) {
        if (questionbank == null) {
            return null;
        }
        QuestionbankVO questionbankVO = new QuestionbankVO();
        BeanUtils.copyProperties(questionbank, questionbankVO);
        return questionbankVO;
    }
}
