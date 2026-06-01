DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN SELECT id FROM boards WHERE id NOT IN (SELECT DISTINCT board_id FROM columns) LOOP
        INSERT INTO columns (board_id, name, position) VALUES (rec.id, 'To Do', 0);
        INSERT INTO columns (board_id, name, position) VALUES (rec.id, 'In Progress', 1);
        INSERT INTO columns (board_id, name, position) VALUES (rec.id, 'Done', 2);
    END LOOP;
END $$;
