create table work_readiness
(
    id           SERIAL PRIMARY KEY,
    offender_id  varchar(6)               not null,
    booking_id   bigint                   not null,
    created_date_time timestamp with time zone not null default now(),
    modified_date_time timestamp with time zone not null default now(),
    author       varchar(32)              not null,
    schema_version  varchar(16)           not null,
    profile_data jsonb                    not null,
    unique (offender_id)
);

create index wr_offender_fk_idx on work_readiness (offender_id);
