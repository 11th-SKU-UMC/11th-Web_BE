package com.umc.movie;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="ratings",uniqueConstraints=@UniqueConstraint(name="uk_rating_member_movie",columnNames={"member_id","movie_id"}))
public class Rating {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long ratingId;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="member_id",nullable=false) Member member;
 @Column(name="movie_id",nullable=false) Long movieId;
 @Column(nullable=false,precision=2,scale=1) BigDecimal score;
 @Column(length=1000) String comment;
 @Column(nullable=false,updatable=false) LocalDateTime createdAt;
 protected Rating() {}
 Rating(Member m,Requests.CreateRating d) { member=m; movieId=d.movieId(); score=d.score(); comment=d.comment(); createdAt=LocalDateTime.now(); }
 void update(Requests.UpdateRating d) { score=d.score(); comment=d.comment(); }
}
