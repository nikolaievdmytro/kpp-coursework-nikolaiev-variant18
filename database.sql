DROP DATABASE IF EXISTS forest_inspection;
CREATE DATABASE forest_inspection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE forest_inspection;

-- Лісова ділянка (F3 береться з sensitivity_score)
CREATE TABLE forest_plot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(160) NOT NULL,
    forestry VARCHAR(120) NOT NULL,
    area_ha DOUBLE NOT NULL,
    sensitivity_score DOUBLE NOT NULL
);

-- Спостереження (affected_area_ha -> F1, severity_score -> F2)
CREATE TABLE observation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plot_id BIGINT NOT NULL,
    observation_type VARCHAR(80) NOT NULL,
    affected_area_ha DOUBLE NOT NULL,
    severity_score DOUBLE NOT NULL,
    FOREIGN KEY (plot_id) REFERENCES forest_plot(id)
);

-- Порушення (об'єкт життєвого циклу; state: DETECTED, VERIFIED, ACTION_PLANNED, CLOSED)
CREATE TABLE violation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    observation_id BIGINT NOT NULL,
    state VARCHAR(30) NOT NULL,
    f1 DOUBLE NOT NULL,
    f2 DOUBLE NOT NULL,
    f3 DOUBLE NOT NULL,
    risk_index DOUBLE NOT NULL,
    FOREIGN KEY (observation_id) REFERENCES observation(id)
);

-- Інспекційне завдання
CREATE TABLE inspection_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    violation_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    planned_date VARCHAR(10),
    FOREIGN KEY (violation_id) REFERENCES violation(id)
);

-- Початкові демонстраційні дані (умовні)
INSERT INTO forest_plot(code, name, forestry, area_ha, sensitivity_score) VALUES
('FP-01', 'Північна ділянка', 'Центральне лісництво', 12.5, 80.0),
('FP-02', 'Річкова ділянка', 'Північне лісництво', 20.0, 55.0);

INSERT INTO observation(plot_id, observation_type, affected_area_ha, severity_score) VALUES
(1, 'Незаконна рубка', 2.5, 70.0),
(2, 'Пошкодження шкідниками', 5.0, 40.0);
