-- Equipment
INSERT INTO equipment (name, model, purchase_date, price, location, status, remark) VALUES
('Treadmill', 'Precor TRM 885', '2024-03-15', 28000, 'Cardio A', 'normal', 'Main model'),
('Treadmill', 'Technogym Run', '2024-05-20', 32000, 'Cardio A', 'normal', ''),
('Elliptical', 'Precor EFX 835', '2024-03-15', 22000, 'Cardio B', 'repair', 'Pedal noise'),
('Spinning', 'Schwinn IC8', '2024-06-10', 15000, 'Spin Room', 'normal', '10 units'),
('Bench', 'Rogue RM-6', '2023-11-01', 8500, 'Strength A', 'normal', ''),
('Dumbbells', 'Hampton 2.5-50lb', '2023-11-01', 12000, 'Strength A', 'normal', 'Full set'),
('Lat Pulldown', 'Cybex VR3', '2023-08-20', 18000, 'Strength B', 'scrapped', 'Cable broken'),
('Yoga Mat', 'Liforme 4mm', '2024-01-05', 3000, 'Yoga Room', 'normal', '20 pcs'),
('Rower', 'Concept2 Model D', '2024-04-12', 9500, 'Cardio B', 'normal', ''),
('Leg Press', 'Hammer Strength', '2024-02-28', 16000, 'Strength B', 'repair', 'Seat loose');

-- Repair records
INSERT INTO repair_record (equipment_id, equipment_name, fault, repair_date, cost, repair_person, status, remark) VALUES
(3, 'Elliptical', 'Pedal noise', DATEADD(day, -5, CURRENT_DATE), 800, 'Zhang', 'repairing', 'Bearing change'),
(7, 'Lat Pulldown', 'Cable broken', '2026-08-15', 0, '', 'scrapped', 'Parts obsolete'),
(10, 'Leg Press', 'Seat loose', DATEADD(day, -2, CURRENT_DATE), 150, 'Li', 'repairing', 'Waiting for seat');

-- Members
INSERT INTO member (name, phone, card_type, amount, start_date, expire_date, remark) VALUES
('Zhang Wei', '13800138001', 'year', 1999, '2025-06-01', DATEADD(day, 30, CURRENT_DATE), 'VIP'),
('Li Na', '13800138002', 'year', 1999, '2025-05-15', DATEADD(day, 25, CURRENT_DATE), ''),
('Wang Fang', '13800138003', 'quarter', 599, '2025-07-01', DATEADD(day, 180, CURRENT_DATE), ''),
('Liu Qiang', '13800138004', 'month', 199, DATEADD(day, -29, CURRENT_DATE), DATEADD(day, 1, CURRENT_DATE), 'Expiring soon'),
('Chen Jing', '13800138005', 'year', 1999, '2025-08-20', DATEADD(day, 34, CURRENT_DATE), ''),
('Zhao Lei', '13800138006', 'year', 1999, '2025-04-10', DATEADD(day, -5, CURRENT_DATE), 'Expired'),
('Sun Li', '13800138007', 'quarter', 699, DATEADD(day, -61, CURRENT_DATE), DATEADD(day, 20, CURRENT_DATE), ''),
('Zhou Jie', '13800138008', 'month', 199, DATEADD(day, -5, CURRENT_DATE), DATEADD(day, 25, CURRENT_DATE), 'New'),
('Wu Min', '13800138009', 'year', 1999, '2025-11-11', DATEADD(day, 377, CURRENT_DATE), ''),
('Zheng Tao', '13800138010', 'quarter', 599, DATEADD(day, -46, CURRENT_DATE), DATEADD(day, 50, CURRENT_DATE), ''),
('Liu Yang', '13800138011', 'month', 199, DATEADD(day, -2, CURRENT_DATE), DATEADD(day, 4, CURRENT_DATE), ''),
('Huang Lei', '13800138012', 'year', 1999, '2025-12-01', DATEADD(day, 397, CURRENT_DATE), '');

-- Energy 2025
INSERT INTO energy_record (stat_year, stat_month, elec_usage, elec_price, elec_cost, water_usage, water_price, water_cost, total_cost, remark) VALUES
(2025,1,3500,0.85,2975,45,5.6,252,3227,''),(2025,2,3800,0.85,3230,42,5.6,235.2,3465.2,''),(2025,3,4200,0.85,3570,50,5.6,280,3850,''),
(2025,4,4500,0.85,3825,55,5.6,308,4133,''),(2025,5,4800,0.85,4080,60,5.6,336,4416,''),(2025,6,5200,0.85,4420,65,5.6,364,4784,''),
(2025,7,5500,0.85,4675,70,5.6,392,5067,''),(2025,8,5800,0.85,4930,68,5.6,380.8,5310.8,''),(2025,9,5000,0.85,4250,62,5.6,347.2,4597.2,''),
(2025,10,4500,0.85,3825,58,5.6,324.8,4149.8,''),(2025,11,4000,0.85,3400,52,5.6,291.2,3691.2,''),(2025,12,3800,0.85,3230,48,5.6,268.8,3498.8,'');
