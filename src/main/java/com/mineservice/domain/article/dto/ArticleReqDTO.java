package com.mineservice.domain.article.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString(exclude = "img")
@ApiModel(value = "링크 등록 요청 객체")
public class ArticleReqDTO {

    @ApiModelProperty(value = "제목", example = "테스트")
    private String title = null;

    @ApiModelProperty(value = "링크", example = "https://www.naver.com")
    private String url = null;

    @ApiModelProperty(value = "이미지", example = "image.png")
    private MultipartFile img;

    @ApiModelProperty(value = "즐겨찾기(Y/N)", example = "Y")
    private String favorite = "N";

    @ApiModelProperty(value = "태그리스트", example = "[태그1, 태그2]")
    private List<String> tags = new ArrayList<>();

    @ApiModelProperty(value = "알람시각(yyyy-MM-dd HH:mm)", example = "2023-03-15 10:30")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime alarmTime = null;

}
