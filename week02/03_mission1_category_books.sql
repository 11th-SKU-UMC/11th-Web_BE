SELECT b.title, b.description, c.name AS category_name
FROM book b
JOIN category c ON b.category_id = c.category_id
WHERE c.name = '문학'
  AND b.is_available = TRUE
ORDER BY b.book_id DESC LIMIT 10;