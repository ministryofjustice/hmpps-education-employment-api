create table work_readiness
(
    offender_id  varchar(7) PRIMARY KEY   not null,
    booking_id   bigint                   not null,
    created_by       varchar(32)              not null,
    created_date_time timestamp with time zone not null default now(),
    modified_by       varchar(32)              not null,
    modified_date_time timestamp with time zone not null default now(),
    schema_version  varchar(16)           not null,
    profile_data jsonb                    not null,
    notes_data jsonb,
    unique (offender_id)
);
