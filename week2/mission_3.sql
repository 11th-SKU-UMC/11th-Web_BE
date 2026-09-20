SELECT b.title,
       t.name AS tag_name,
       CASE
           WHEN bl.user_id IS NOT NULL THEN TRUE
           ELSE FALSE
       END AS is_liked
FROM book b
JOIN book_tag bt ON b.book_id = bt.book_id
JOIN tag t ON bt.tag_id = t.tag_id
LEFT JOIN book_like bl
       ON b.book_id = bl.book_id
       AND bl.user_id = 1
WHERE b.book_id = 1;

/*
특정 책의 태그 목록과 특정 사용자의 좋아요 여부를 조회하는 요구사항입니다. 
조회할 책의 정보를 기준으로 태그와 좋아요 여부를 확인해야 하므로 `book` 테이블을 기준으로 시작했습니다.

책의 태그를 확인하기 위해 `book`과 `tag` 사이의 연결 테이블인 `book_tag`를 거쳐 `tag`를 JOIN했습니다. 
또한 특정 사용자의 좋아요 여부를 확인하기 위해 `book_like`를 LEFT JOIN했습니다.

먼저 조회하려는 책의 `book_id=1`과 사용자의 `user_id=1`이 올바르게 설정되어 있는지 확인했습니다. 
이후 `book → book_tag → tag`의 연결 관계와 `book_like`에 해당 사용자의 좋아요 기록이 존재하는지 확인했습니다.
*/