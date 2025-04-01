-- initialise revision/audit tables
DELETE FROM work_readiness_audit;
DELETE FROM revision_info;

SELECT setval('revision_info_rev_number_seq', 1, true);

INSERT INTO revision_info
SELECT currval('revision_info_rev_number_seq') as rev_number, date_part('epoch', now()) * 1000, 'SYSTEM';

insert into work_readiness_audit
select currval('revision_info_rev_number_seq'), 0, wr.* from work_readiness wr where offender_id not in (select distinct offender_id from work_readiness_audit);
