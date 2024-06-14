create table "user"
(
    id         int GENERATED ALWAYS AS IDENTITY primary key,
    first_name varchar,
    last_name  varchar,
    email      varchar,
    image_url  varchar,
    is_admin   boolean
);

INSERT INTO "user" (first_name, last_name, email, is_admin)
VALUES
    ('Daud', 'Mohamed', 'daud.mohamed.adam@gmail.com', true),
    ('Joakim', 'Valand', 'joakim.valand@gmail.com', true);

create table workshop
(
    id          varchar(64) primary key,
    title       varchar                  not null,
    description varchar,
    start_time  timestamp with time zone not null,
    end_time    timestamp with time zone not null,
    capacity    int                      not null,
    active      boolean                  not null
);

create table speaker
(
    index       int,
    full_name        varchar,
    workshop_id varchar(64),
    bio         varchar,
    twitter     varchar,
    PRIMARY KEY (workshop_id, index),

    constraint speaker_fk_workshop foreign key (workshop_id) references workshop (id)
);



create table workshop_registration
(
    user_id     int,
    workshop_id varchar(64),
    status       varchar,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone not null default current_timestamp,

    constraint workshop_registration_fk_workshop foreign key (workshop_id) references workshop (id),
    constraint workshop_registration_fk_user foreign key (user_id) references "user" (id),
    constraint unique_user_workshop UNIQUE (user_id, workshop_id)
);

CREATE INDEX ON workshop_registration (workshop_id, updated_at);
CREATE INDEX ON workshop_registration (user_id, workshop_id);
