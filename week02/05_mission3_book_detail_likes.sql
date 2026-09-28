SELECT b.title, t.name AS tag_name, bl.user_id AS liked_user_id
FROM book b
         LEFT JOIN book_tag bt ON b.book_id = bt.book_id
         LEFT JOIN tag t ON bt.tag_id = t.tag_id
         LEFT JOIN book_like bl ON b.book_id = bl.book_id AND bl.user_id = 1
WHERE b.book_id = 1;