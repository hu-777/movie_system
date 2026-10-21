INSERT INTO admin (id, username, name, password, role, avatar)
VALUES (1, 'admin', '管理员', '$2a$10$/EHoTKZnuk1IDwjInYVbuOleaTQ75rcBLr.3TmwF0WtToModLxl3G', 'ADMIN', NULL);

INSERT INTO category (id, name) VALUES
(1, '动画'),
(2, '剧情'),
(3, '纪录片');

INSERT INTO notice (id, title, content, time) VALUES
(1, '系统已启动', '本地开发数据库已自动初始化，可以直接体验登录、电影、场次和购票流程。', '2026-09-22 09:00:00');

INSERT INTO released_movies (id, name, type, actors, score, box_office, showtime, area, length, unit, store, price, category_id) VALUES
(1, '天气之子', '动画', '醍醐虎汰朗, 森七菜', '8.1', '10亿+', '2019-07-19', '日本', '112分钟', '张', 0, 35, 1),
(2, '我想吃掉你的胰脏', '剧情', '滨边美波, 北村匠海', '8.0', '3亿+', '2017-07-28', '日本', '115分钟', '张', 0, 32, 2),
(3, '蜘蛛侠：平行宇宙', '动画', '沙梅克·摩尔, 杰克·约翰逊, 海莉·斯坦菲尔德', '8.6', '4.27亿', '2018-12-21', '美国', '117分钟', '张', 0, 35, 1);

INSERT INTO upcoming_movies (id, name, comment, showtime, tickets, goods_id, img, price, type, position) VALUES
(1, '天气之子', '少年与少女在雨季中相遇。', '2026-09-22 19:30:00', 48, 1, 'http://localhost:9090/files/download/天气之子.jpg', 35, '动画', '1号厅'),
(2, '我想吃掉你的胰脏', '关于青春、告别与珍惜的故事。', '2026-09-22 20:00:00', 48, 2, 'http://localhost:9090/files/download/我想吃掉你的胰脏.jpg', 32, '剧情', '2号厅'),
(3, '蜘蛛侠：平行宇宙', '迈尔斯·莫拉莱斯踏上成为蜘蛛侠的成长之旅。', '2026-10-25 19:30:00', 48, 3, 'http://localhost:9090/files/download/蜘蛛侠：平行宇宙.jpg', 35, '动画', '3号厅');

INSERT INTO screening (id, goods_id, goods_name, hall_name, start_time, end_time, price, total_seats, available_seats, screen_type) VALUES
(1, 1, '天气之子', '1号厅', '2026-09-22 19:30:00', '2026-09-22 21:22:00', 35, 48, 48, '2D'),
(2, 2, '我想吃掉你的胰脏', '2号厅', '2026-09-22 20:00:00', '2026-09-22 21:55:00', 32, 48, 48, '2D'),
(3, 3, '蜘蛛侠：平行宇宙', '3号厅', '2026-10-25 19:30:00', '2026-10-25 21:27:00', 35, 48, 48, '2D');

INSERT INTO seat (screening_id, seat_row, seat_column, status, seat_name)
SELECT s.id, r.row_name, c.col_num, 'AVAILABLE', CONCAT(r.row_name, c.col_num)
FROM screening s
CROSS JOIN (
    SELECT 'A' row_name UNION ALL SELECT 'B' UNION ALL SELECT 'C' UNION ALL SELECT 'D' UNION ALL SELECT 'E' UNION ALL SELECT 'F'
) r
CROSS JOIN (
    SELECT 1 col_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
) c;

