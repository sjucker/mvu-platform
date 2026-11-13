-- optional: voucher is only visible to supporters with a payment in the last n months (null = always visible)
alter table voucher
    add column paid_within_months integer;
alter table supporter_voucher
    add column paid_within_months integer;
