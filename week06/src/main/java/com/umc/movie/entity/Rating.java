package com.umc.movie.entity;
import jakarta.persistence.*;
@Entity @Table(name="rating",uniqueConstraints=@UniqueConstraint(name="uk_rating_member_movie",columnNames={"member_id","movie_id"}))
public class Rating extends BaseTimeEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="rating_id") private Long ratingId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="member_id",nullable=false) private Member member;
 @Column(name="movie_id",nullable=false) private Long movieId;
 @Column(nullable=false,columnDefinition="TINYINT") private Integer score;
 @Column(columnDefinition="TEXT") private String comment;
 protected Rating(){}
 public Rating(Member member,Long movieId,Integer score,String comment){this.member=member;this.movieId=movieId;this.score=score;this.comment=comment;}
 public void update(Integer score,String comment){this.score=score;this.comment=comment;}
 public Long getRatingId(){return ratingId;}
 public Long getMovieId(){return movieId;}
 public Integer getScore(){return score;}
 public String getComment(){return comment;}
 public Member getMember(){return member;}
}
