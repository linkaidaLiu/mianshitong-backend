package com.liulin.mianshitong.esdao;

import com.liulin.mianshitong.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 题目 ES 操作
 *
 * @author <a href="https://github.com/liliulin"></a>
 * @from <a href="https://liulin.icu"></a>
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

    List<QuestionEsDTO> findByUserId(Long userId);
}