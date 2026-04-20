ALTER TABLE leave_application ALTER COLUMN duration TYPE NUMERIC(4,1) USING duration::NUMERIC(4,1);
ALTER TABLE leave_tracker ALTER COLUMN sick_leave_available TYPE NUMERIC(4,1) USING sick_leave_available::NUMERIC(4,1);
ALTER TABLE leave_tracker ALTER COLUMN casual_leave_available TYPE NUMERIC(4,1) USING casual_leave_available::NUMERIC(4,1);
ALTER TABLE leave_tracker ALTER COLUMN loss_of_pay_available TYPE NUMERIC(4,1) USING loss_of_pay_available::NUMERIC(4,1);
ALTER TABLE leave_tracker ALTER COLUMN sick_leave_booked TYPE NUMERIC(4,1) USING sick_leave_booked::NUMERIC(4,1);
ALTER TABLE leave_tracker ALTER COLUMN casual_leave_booked TYPE NUMERIC(4,1) USING casual_leave_booked::NUMERIC(4,1);
ALTER TABLE leave_tracker ALTER COLUMN loss_of_pay_booked TYPE NUMERIC(4,1) USING loss_of_pay_booked::NUMERIC(4,1);
