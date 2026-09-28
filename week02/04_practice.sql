USE week02_library;

SELECT
  book.title,
  book.description,
  category.name AS category_name
FROM book
JOIN category ON book.category_id = category.category_id
WHERE category.name = '문학'
  AND book.is_available = TRUE
ORDER BY book.book_id DESC
LIMIT 10;

SET @current_user_id = 1;

SELECT
  book.title,
  rental.rented_at,
  rental.due_at
FROM rental
JOIN book ON rental.book_id = book.book_id
WHERE rental.user_id = @current_user_id
  AND rental.returned_at IS NULL
ORDER BY rental.due_at ASC;

SET @selected_book_id = 1;
SET @current_user_id = 1;

SELECT
  book.title,
  tag.name AS tag_name,
  CASE
    WHEN book_like.user_id IS NULL THEN '아니요'
    ELSE '예'
  END AS is_liked
FROM book
JOIN book_tag ON book.book_id = book_tag.book_id
JOIN tag ON book_tag.tag_id = tag.tag_id
LEFT JOIN book_like
  ON book.book_id = book_like.book_id
  AND book_like.user_id = @current_user_id
WHERE book.book_id = @selected_book_id;
