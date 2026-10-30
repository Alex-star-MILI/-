-- Equipment table
CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(200),
    purchase_date DATE,
    price DECIMAL(12,2) DEFAULT 0,
    location VARCHAR(200),
    status VARCHAR(20) DEFAULT 'normal',
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Repair record table
CREATE TABLE IF NOT EXISTS repair_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    equipment_name VARCHAR(200),
    fault VARCHAR(500),
    repair_date DATE,
    cost DECIMAL(12,2) DEFAULT 0,
    repair_person VARCHAR(100),
    status VARCHAR(20) DEFAULT 'repairing',
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Member table
CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    card_type VARCHAR(20) DEFAULT 'month',
    amount DECIMAL(12,2) DEFAULT 0,
    start_date DATE,
    expire_date DATE,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Energy record table (year/month are reserved in H2, use stat_year/stat_month)
CREATE TABLE IF NOT EXISTS energy_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_year INT NOT NULL,
    stat_month INT NOT NULL,
    elec_usage DECIMAL(12,2) DEFAULT 0,
    elec_price DECIMAL(8,4) DEFAULT 0,
    elec_cost DECIMAL(12,2) DEFAULT 0,
    water_usage DECIMAL(12,2) DEFAULT 0,
    water_price DECIMAL(8,4) DEFAULT 0,
    water_cost DECIMAL(12,2) DEFAULT 0,
    total_cost DECIMAL(12,2) DEFAULT 0,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(stat_year, stat_month)
);
