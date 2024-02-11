-- Вложенные множества - принцип, описывающий один из возможных вариантов хранения иерархических структур в реляционной
-- базе дынных. Вложенные множества основаны на узлах, представляющих собой отдельно взятый элемент иерархии, который
-- может содержать в себе вложенные узлы. Каждый узел состоит из левого и правого ключей, обозначающих принадлежность
-- элемента иерархии к узлам.

-- Комплектующие        (1,  10, 0)
-- - Процессоры         (2,  7,  1)
-- - - Intel            (3,  4,  2)
-- - - AMD              (5,  6,  2)
-- - ОЗУ                (8,  9,  1)
-- Аудиотехника         (11, 20, 0)
-- - Наушники           (12, 17, 1)
-- - - С микрофоном     (13, 14, 2)
-- - - Без микрофона    (15, 16, 2)
-- - Колонки            (18, 19, 1)


drop table Categories;

create table Categories
(
    id              serial8 primary key,
    name            varchar(30) not null,
    left_key        int8,
    right_key       int8,
    hierarchy_level int8
);
insert into Categories (name, left_key, right_key, hierarchy_level) VALUES ('Комплектующие', 1, 10, 0),
                                                                           ('Процессоры', 2, 7, 1),
                                                                           ('Intel', 3, 4, 2),
                                                                           ('AMD', 5, 6, 2),
                                                                           ('ОЗУ', 8, 9, 1),
                                                                           ('Аудиотехника', 11, 20, 0),
                                                                           ('Наушники', 12, 17, 1),
                                                                           ('С микрофоном', 13, 14, 2),
                                                                           ('Без микрофона', 15, 16, 2),
                                                                           ('Колонки', 18, 19, 1);



