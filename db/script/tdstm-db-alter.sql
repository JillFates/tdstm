update asset_transition set voided = 0 where voided is null;
alter table asset_transition modify time_elapsed bigint(20) not null;