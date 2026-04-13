DROP INDEX IF EXISTS idx_sys_dict_type_status_sort;

ALTER TABLE sys_dict_type DROP COLUMN IF EXISTS sort_order;
