-- Requirement: show the title and description of books available for rental.
-- This schema has no created_at column, so book_id DESC is used as the
-- available proxy for newest-first (IDs are assigned when books are inserted).
USE week02_library;

SELECT title, description
FROM book
WHERE is_available = TRUE
ORDER BY book_id DESC;
