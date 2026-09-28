SELECT
    b.title,
    b.description,
    c.name AS category_name
FROM book AS b
JOIN category AS c
    ON b.category_id = c.category_id
WHERE c.name = '문학'
  AND b.is_available = TRUE
ORDER BY b.book_id DESC
LIMIT 10;


SELECT
    b.title,
    r.rented_at,
    r.due_at
FROM rental AS r
JOIN book AS b
    ON r.book_id = b.book_id
WHERE r.user_id = 1
  AND r.returned_at IS NULL
ORDER BY r.due_at ASC;

SELECT
    b.title,
    GROUP_CONCAT(
        DISTINCT t.name
        ORDER BY t.tag_id
        SEPARATOR ', '
    ) AS tags,
    EXISTS (
        SELECT 1
        FROM book_like AS bl
        WHERE bl.book_id = b.book_id
          AND bl.user_id = 1
    ) AS is_liked
FROM book AS b
LEFT JOIN book_tag AS bt
    ON b.book_id = bt.book_id
LEFT JOIN tag AS t
    ON bt.tag_id = t.tag_id
WHERE b.book_id = 1
GROUP BY b.book_id, b.title;

