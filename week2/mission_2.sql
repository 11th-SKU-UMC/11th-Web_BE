SELECT b.title,
       r.rented_at,
       r.due_at
FROM rental r
JOIN book b ON r.book_id = b.book_id
WHERE r.user_id = 1
  AND r.returned_at IS NULL
ORDER BY r.due_at ASC;

/*
특정 사용자가 아직 반납하지 않은 책을 반납 예정일 순으로 조회하는 요구사항입니다. 
사용자의 대여 정보와 반납 여부를 확인해야 하므로 rental 테이블을 기준으로 시작했습니다.

rental 테이블에는 책의 제목이 없고 book_id만 있기 때문에 책 제목을 가져오기 위해 book 테이블을 JOIN했습니다. 
두 테이블은 book_id를 통해 연결되어 있어 r.book_id = b.book_id를 JOIN 조건으로 사용했습니다.


먼저 user_id = 1로 특정 사용자가 올바르게 지정되어 있는지와 returned_at IS NULL 조건으로 아직 반납하지 않은 책만 조회되는지 확인했습니다. 
이후 rental과 book이 book_id를 기준으로 올바르게 JOIN되어 있는지 확인했습니다.
*/