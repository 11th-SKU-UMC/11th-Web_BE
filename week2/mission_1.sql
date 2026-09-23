SELECT b.title, b.description, c.name AS category_name
FROM book b
JOIN category c ON b.category_id = c.category_id
WHERE c.name = '문학'
  AND b.is_available = TRUE
ORDER BY b.book_id DESC
LIMIT 10;

/* 
문학 카테고리에서 대여 가능한 도서를 최신순으로 10개 조회하는 요구사항입니다. 
책의 제목과 설명을 조회해야 하므로 `book` 테이블을 기준으로 시작했습니다.

`book` 테이블에는 카테고리 이름이 없고 `category_id`만 있기 때문에 카테고리 이름을 가져오기 위해 `category` 테이블과 JOIN했습니다. 
두 테이블은 `category_id`를 통해 연결되어 있어 `b.category_id = c.category_id`를 JOIN 조건으로 사용했습니다.

먼저 `WHERE`절의 카테고리 이름이 `문학`으로 설정되어 있는지와 `is_available`이 `TRUE`인지 확인했습니다. 
이후 `book`과 `category`가 `category_id`를 기준으로 올바르게 JOIN되어 있는지 확인했습니다.
*/