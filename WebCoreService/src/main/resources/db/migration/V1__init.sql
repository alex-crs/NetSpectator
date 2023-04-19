create table DeviceGroup
(
    id         bigserial primary key,
    title      varchar(255),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp
);

insert into DeviceGroup(title)

values ('main');

create table Device
(
    id           bigserial primary key,
    UUID         varchar(255),
    title        varchar(255),
    ip           varchar(255),
    description  varchar(1000),
    hddFreeSpace numeric(8, 2),
    onlineStatus numeric(1),
    deviceGroup  bigint references DeviceGroup (id),
    created_at   timestamp default current_timestamp,
    updated_at   timestamp default current_timestamp
);

