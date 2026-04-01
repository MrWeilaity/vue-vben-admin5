ALTER TABLE sys_operation_log
    ALTER COLUMN error_message TYPE TEXT;

ALTER TABLE sys_operation_log
    ADD COLUMN IF NOT EXISTS client_address VARCHAR(255);

COMMENT ON COLUMN sys_operation_log.error_message IS '错误信息：优先记录业务异常提示，否则记录堆栈摘要';
COMMENT ON COLUMN sys_operation_log.client_address IS '客户端IP归属地，尽量精确到区';
