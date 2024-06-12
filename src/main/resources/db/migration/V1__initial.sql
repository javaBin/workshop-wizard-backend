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
    ('Joakim', 'Valand', 'joakim.valand@gmail.com', true),
    ('Elise', 'Bråtveit', 'elise@test.com', false);

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

INSERT INTO workshop (id, title, description, start_time, end_time, capacity, active)
VALUES ('18aaf407-ff02-4f49-b11d-29877a9de906', 'Moderne brukergrensesnitt i en legacy kodebase',
        'NAV sin pensjonsløsning, Pesys, har i mange år vært blant Norges største og mest komplekse kodebaser. Opprinnelig skrevet med teknologi, som i dag, ville fått utviklere til å gråte. Vi skal i dette foredraget fortelle hvordan vi har modernisert det grafiske brukergrensesnittet. Vi skal rett og slett fortelle deg en ærlig historie om hvordan vår erfaring med dette var. Vi kommer til å snakke om den moderne retningen som premissgiver, hvor vi måtte ta tak i både menneskelige og tekniske utfordringer. På veien skal vi også prate om hvordan vi organiserte teamene og folkene, og fortelle hvordan vi tok tak i arkitekturen og det sosiotekniske aspektet av det. Du skal på den måten lære hvorfor vi mener dette økte selvsikkerheten og eierskapet til alle involverte.  Videre deler vi hvordan vi har løst de tekniske utfordringene omkring implementasjon; integrasjon og sikkerhet ved å blant annet bruke api gateways og micro-frontends. Etter å ha hørt foredraget vil du være i stand til å bedre ta tak i modernisering av legacy-kodebase i din organisasjon.',
        '2023-09-06T10:20', '2023-09-06T11:20', 30, true),
       ('37cdf4dd-4f9a-4d93-ad9f-eb4994cb2f52', 'A software developer''s guide to licenses and other legalities',
        'In the fast-paced world of software development, it is critical for developers to have a clear understanding of the legal landscape surrounding the software that they develop. This presentation will look into the different branches of software licenses and open source. In this context, the presentation will discuss common terms of commercial agreements and what pitfalls to avoid. Finally, this presentation will look into the OpenJDK project and its formal organization. Attendees will gain a solid understanding of the legal and technical considerations of choosing licensed software, enabling them to make informed decisions about their projects.',
        '2023-09-06T13:00', '2023-09-06T14:00', 100, true),
       ('3ba51b2d-c986-4678-8b74-c40257576cb6', 'Free enterprise search with OpenSearch',
        'Elasticsearch is nowdays a preferred solution for enterprise search. You may be aware that not all of its features are available for free but did you know there is a solid open source alternative created by Amazon called OpenSearch ? And in fact this free alternative is based on a fork of Elasticsearch ? There are however certain pitfalls if you prefer to go the OpenSearch route. In this session we will make a comparison between traditional Elasticsearch and OpenSearch to understand is going completely free and open source a save path.',
        '2023-09-07T09:00', '2023-09-07T09:45', 30, true),
       ('88d67125-288c-499c-85af-e0b6bab2316f', 'Welcome to the Jungle - A safari through the JVM landscape',
        'OpenJDK with it’s Java Virtual Machine is great but there is not only one flavour but many. There is Oracle OpenJDK, Eclipse Temurin, IBM Semeru, Amazon Corretto, Azul Zulu, Alibaba Dragonwell, Huawei Bi Sheng, Tencent Kona and many more. Did you ever ask yourself which one is better, faster, free or something similar? Or do you want to know where the differences are in those distributions, well then this session might bring some answers to your questions. It will give you an idea about what the JVM is and will cover all the available distributions not only of OpenJDK but also of GraalVM and will try to explain the differences and features of the available distributions. It will also try to give you an idea what JVM to use for specific use cases.',
        '2023-09-06T13:00', '2023-09-06T14:00', 30, true),
       ('3a06f863-c1ee-4d15-be2e-3cc53ae473a9', 'Stop breaking stuff all the time!',
        'Verifying behaviors of the cloud-native applications and ensuring that all of the services in the system work correctly together is both crucial and challenging. Manually maintaining environments to test the correctness of the entire system is undevops-like and fragile.\n\nLuckily, modern tools can help you to build automated, reliable test pipelines, and in this session, we explore how using Spring Cloud Contract and Testcontainers together can improve your testing and deployment processes.\n\nSpring Cloud Contract is an implementation of Consumer-Driven Contracts, an approach that provides a way to easily describe and verify APIs, at the same time allowing building API backward compatibility verification into the deployment process.\n\nTestcontainers lets developers programmatically build test environments consisting of real services running in lightweight and disposable containers. It turns the process of integration testing into a seamless, unit-test-like experience.\n\nIn this presentation, we’ll show how contract and integration tests complement each other and explore one of the most natural and reliable approaches to service evolution with contract testing. We’ll discuss why in Spring Cloud Contract, we’ve decided to switch to using Testcontainers as the solution for Kafka and AMQP messaging verification and demonstrate practical use-cases and code examples of how to set up both types of tests in your applications and deployment pipelines.',
        '2023-09-07T15:40', '2023-09-07T16:40', 30, true),
       ('6f9c99a8-2aab-4473-a848-34b4492acb8f', 'Building a fishy cloud datawarehouse in 2023',
        'At Br Karlsen we make, buy and sell fish of all kinds, here are our experience in deploying the modern data stack , built on Airbyte, dbt , BigQuery and Apache Superset. Why we chose the components we did, and what our experience has been. When did we choose the managed version vs the open source version of a product? ',
        '2023-09-07T15:40', '2023-09-07T16:25', 30, true),
       ('6f9c99a8-2aab-4473-a84f', 'Should be inactive',
        'At Br Karlsen we make, buy and sell fish of all kinds, here are our experience in deploying the modern data stack , built on Airbyte, dbt , BigQuery and Apache Superset. Why we chose the components we did, and what our experience has been. When did we choose the managed version vs the open source version of a product? ',
        '2023-09-07T15:40', '2023-09-07T16:25', 30, true);

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

INSERT INTO workshop_registration (user_id, workshop_id, status, created_at, updated_at)
VALUES ('1', '18aaf407-ff02-4f49-b11d-29877a9de906', 'APPROVED', '2023-09-06T10:20', '2023-09-06T10:20'),
       ('1', '37cdf4dd-4f9a-4d93-ad9f-eb4994cb2f52', 'PENDING', '2023-09-06T13:00', '2023-09-06T13:00');
