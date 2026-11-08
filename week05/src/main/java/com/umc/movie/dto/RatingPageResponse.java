package com.umc.movie.dto;
import org.springframework.data.domain.Page;
import java.util.List;
public record RatingPageResponse(List<RatingResponse> content,int page,int size,long totalElements,int totalPages,boolean last) {
 public static RatingPageResponse from(Page<RatingResponse> p){return new RatingPageResponse(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.isLast());}
}
