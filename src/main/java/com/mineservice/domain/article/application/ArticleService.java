package com.mineservice.domain.article.application;

import com.mineservice.domain.article.domain.Article;
import com.mineservice.domain.article.domain.ArticleAlarm;
import com.mineservice.domain.article.dto.ArticleDTO;
import com.mineservice.domain.article.dto.ArticleReqDTO;
import com.mineservice.domain.article.dto.ArticleResDTO;
import com.mineservice.domain.article.repository.ArticleAlarmRepository;
import com.mineservice.domain.article.repository.ArticleRepository;
import com.mineservice.domain.article_tag.domain.ArticleTag;
import com.mineservice.domain.article_tag.repository.ArticleTagRepository;
import com.mineservice.domain.file_info.application.FileInfoService;
import com.mineservice.domain.tag.application.TagService;
import com.mineservice.domain.tag.domain.Tag;
import com.mineservice.domain.user.UserRepository;
import com.mineservice.login.entity.User;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagService tagService;
    private final FileInfoService fileInfoService;
    private final ArticleTagRepository articleTagRepository;
    private final UserRepository userRepository;
    private final ArticleAlarmRepository articleAlarmRepository;

    @Transactional
    public User createUserInfo(String userId) {
        return userRepository.save(User.builder()
                .id(userId)
                .name("TESET")
                .email("test@test.com")
                .provider("test")
                .build());
    }

    @Transactional
    public void createArticle(String userId, ArticleReqDTO articleReqDTO) {
        String articleType = getArticleType(articleReqDTO.getUrl());
        if ("image".equals(articleType)) {
            String title = "MINE_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            List<Article> sameTitleArticleList = articleRepository.findArticlesByUserIdAndTitleStartingWith(userId, title);
            if (sameTitleArticleList.isEmpty()) {
                articleReqDTO.setTitle(title);
            } else {
                articleReqDTO.setTitle(title + "(" + sameTitleArticleList.size() + ")");
            }
        } else {
            articleRepository.findArticleByUrl(articleReqDTO.getUrl())
                    .ifPresent(article -> {
                        throw new IllegalArgumentException("이미 등록된 링크입니다.");
                    });
        }
        log.info("articleTitle {}", articleReqDTO.getTitle());

        Article article = Article.builder()
                .userId(userId)
                .title(articleReqDTO.getTitle())
                .type(articleType)
                .url(articleReqDTO.getUrl())
                .favorite(articleReqDTO.getFavorite())
                .build();
        articleRepository.save(article);
        log.info("article {}", article.toString());

        List<ArticleTag> articleTagList = new ArrayList<>();
        for (String tagName : articleReqDTO.getTags()) {
            Tag tag = tagService.createTagByArticle(userId, tagName);
            articleTagList.add(ArticleTag.builder()
                    .article(article)
                    .tag(tag)
                    .build());
            log.info("tag {}", tag.toString());
        }

        fileInfoService.createFileInfo(userId, article.getId(), articleReqDTO.getImg());

        if (articleReqDTO.getAlarmTime() != null) {
            createArticleAlarm(article.getId(), articleReqDTO.getAlarmTime());
        }

        articleTagRepository.saveAll(articleTagList);
        log.info("articleTagList {}", articleTagList.toString());
    }

    @Transactional
    public void createArticleAlarm(Long articleId, LocalDateTime alarmTime) {
        ArticleAlarm articleAlarm = ArticleAlarm.builder()
                .articleId(articleId)
                .time(alarmTime)
                .build();
        articleAlarmRepository.save(articleAlarm);
        log.info("articleAlarm {}", articleAlarm.toString());
    }

    public ArticleResDTO findAllArticlesByUserId(String userId, Pageable pageable) {
        Page<Article> findByUserId = articleRepository.findAllByUserId(userId, pageable);
        Page<ArticleDTO> articleDTOPage = findByUserId.map(this::toDTO);

        return ArticleResDTO.builder()
                .totalArticleSize(articleDTOPage.getTotalElements())
                .totalPageSize(articleDTOPage.getTotalPages())
                .articleList(articleDTOPage.getContent())
                .build();
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        fileInfoService.deleteFileInfo(articleId);
        articleTagRepository.deleteByArticleId(articleId);
        articleAlarmRepository.deleteByArticleId(articleId);
        articleRepository.deleteById(articleId);
    }


    private ArticleDTO toDTO(Article article) {
        return ArticleDTO.builder()
                .articleId(article.getId())
                .type(article.getType())
                .title(article.getTitle())
                .favorite(article.getFavorite())
                .build();
    }

    private String getArticleType(String url) {
        if (url == null) {
            return "image";
        } else if (url.contains("youtube")) {
            return "youtube";
        } else {
            return "link";
        }
    }

}
