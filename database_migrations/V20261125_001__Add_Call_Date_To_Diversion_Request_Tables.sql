-- Editable Call Date for Vendor Diversion Request process (ERC & Rail Pad)
-- Run against Azure MySQL / target DB alongside deployment (spring.jpa.hibernate.ddl-auto=none)

ALTER TABLE erc_material_diversion_request
    ADD COLUMN call_date DATE NULL AFTER railway_permission_date;

ALTER TABLE rail_material_diversion_request
    ADD COLUMN call_date DATE NULL AFTER railway_permission_date;
