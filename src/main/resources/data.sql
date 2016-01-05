INSERT INTO user (email, password_hash, role) VALUES ('admin@gmail.com', '$2a$10$OZaDLv3Jxa6/kXe.Kb9sfO6cQB1wwrii.JBg78QBjg7.D/UHYIzEK', 'ADMIN');
INSERT INTO user (email, password_hash, role) VALUES ('user1@gmail.com', '$2a$10$OZaDLv3Jxa6/kXe.Kb9sfO6cQB1wwrii.JBg78QBjg7.D/UHYIzEK', 'REGULAR');
INSERT INTO user (email, password_hash, role) VALUES ('user2@gmail.com', '$2a$10$OZaDLv3Jxa6/kXe.Kb9sfO6cQB1wwrii.JBg78QBjg7.D/UHYIzEK', 'REGULAR');
insert into restaurant (id_restaurant, address, id_by_authorities, lunch_end_hour, phone, title) values (1, 'Via Sopeno Gatve, 10, Vilnius 03211', '00000001', 16, '+370 674 41922', 'Dublis Restoranas');
insert into restaurant (id_restaurant, address, id_by_authorities, lunch_end_hour, phone, title) values (default, 'Traku G. 4, Vilnius 01132', '00000002', 15, '+370 5 212 6874', 'Alaus Biblioteka');
insert into restaurant (id_restaurant, address, id_by_authorities, lunch_end_hour, phone, title) values (default, 'Verkiu G. 29 | Ogmios Miestas, Seimos Aikste 6, Vilnius 09108', '00000003', 17, '+370 645 52250', 'Jurgis ir Drakonas');
insert into lunch_menu (id_lunch_menu, the_restaurant, the_day, state) values (1, 1, DATE '2015-12-26', 'PUBLISHED');
insert into lunch_menu (id_lunch_menu, the_restaurant, the_day, state) values (2, 2, DATE '2015-12-26', 'PUBLISHED');
insert into lunch_menu (id_lunch_menu, the_restaurant, the_day, state) values (3, 1, DATE '2015-12-27', 'PUBLISHED');
insert into lunch_menu (id_lunch_menu, the_restaurant, the_day, state) values (4, 3, DATE '2015-12-26', 'CREATED');
insert into dish (id_dish,  the_menu, name, price) values (1, 1, 'dish #1', 11.11);
insert into dish (id_dish,  the_menu, name, price) values (2, 1, 'dish #2', 22.22);
insert into dish (id_dish,  the_menu, name, price) values (3, 2, 'dish #3', 33.33);
insert into dish (id_dish,  the_menu, name, price) values (4, 2, 'dish #4', 44.44);
insert into dish (id_dish,  the_menu, name, price) values (5, 3, 'dish #5', 55.55);
insert into dish (id_dish,  the_menu, name, price) values (6, 3, 'dish #6', 66.66);
insert into dish (id_dish,  the_menu, name, price) values (7, 4, 'dish #7', 77.77);
insert into dish (id_dish,  the_menu, name, price) values (8, 4, 'dish #8', 88.88);




