CREATE DATABASE IF NOT EXISTS temperature_converter_db;
USE temperature_converter_db;

CREATE TABLE IF NOT EXISTS unit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(64) NOT NULL UNIQUE,
    default_name VARCHAR(128) NOT NULL,
    description VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS unit_translation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    unit_id INT NOT NULL,
    lang_code VARCHAR(10) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255) NOT NULL,
    UNIQUE(unit_id, lang_code),
    FOREIGN KEY (unit_id) REFERENCES unit(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS temp_record (
    id INT AUTO_INCREMENT PRIMARY KEY,
    input_value DOUBLE NOT NULL,
    result_value DOUBLE NOT NULL,
    from_unit_id INT NOT NULL,
    to_unit_id INT NOT NULL,
    language_code VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_unit_id) REFERENCES unit(id),
    FOREIGN KEY (to_unit_id) REFERENCES unit(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO unit (key_name, default_name, description) VALUES
    ('celsius', 'Celsius', 'Temperature in Celsius'),
    ('fahrenheit', 'Fahrenheit', 'Temperature in Fahrenheit'),
    ('kelvin', 'Kelvin', 'Temperature in Kelvin'),
    ('speed_kmh', 'Kilometre per hour', 'Speed in kilometres per hour'),
    ('distance_km', 'Kilometre', 'Distance in kilometres'),
    ('time_hour', 'Hour', 'Time in hours')
ON DUPLICATE KEY UPDATE default_name = VALUES(default_name), description = VALUES(description);

INSERT INTO unit_translation (unit_id, lang_code, name, description) VALUES
    (1, 'en', 'Celsius', 'Temperature in Celsius'),
    (1, 'fi', 'Celsius', 'Lämpötila celsiusasteina'),
    (1, 'sv', 'Celsius', 'Temperatur i Celsius'),
    (1, 'ja', 'セルシウス', 'セルシウス温度'),
    (1, 'ar', 'درجة مئوية', 'درجة الحرارة بالسنتيجراد'),

    (2, 'en', 'Fahrenheit', 'Temperature in Fahrenheit'),
    (2, 'fi', 'Fahrenheit', 'Lämpötila fahrenheitasteina'),
    (2, 'sv', 'Fahrenheit', 'Temperatur i Fahrenheit'),
    (2, 'ja', 'ファーレンハイト', '華氏温度'),
    (2, 'ar', 'فهرنهايت', 'درجة الحرارة بالفهرنهايت'),

    (3, 'en', 'Kelvin', 'Temperature in Kelvin'),
    (3, 'fi', 'Kelvin', 'Lämpötila kelvineinä'),
    (3, 'sv', 'Kelvin', 'Temperatur i Kelvin'),
    (3, 'ja', 'ケルビン', 'ケルビン温度'),
    (3, 'ar', 'كلفن', 'درجة الحرارة بالكلفن'),

    (4, 'en', 'Kilometre per hour', 'Speed in kilometres per hour'),
    (4, 'fi', 'Kilometriä tunnissa', 'Nopeus kilometreinä tunnissa'),
    (4, 'sv', 'Kilometer per timme', 'Hastighet i kilometer per timme'),
    (4, 'ja', '時間あたりキロメートル', '1時間あたりの速度'),
    (4, 'ar', 'كيلومتر في الساعة', 'السرعة بالكيلومتر في الساعة'),

    (5, 'en', 'Kilometre', 'Distance in kilometres'),
    (5, 'fi', 'Kilometri', 'Etäisyys kilometreinä'),
    (5, 'sv', 'Kilometer', 'Avstånd i kilometer'),
    (5, 'ja', 'キロメートル', 'キロメートルの距離'),
    (5, 'ar', 'كيلومتر', 'المسافة بالكيلومترات'),

    (6, 'en', 'Hour', 'Time in hours'),
    (6, 'fi', 'Tunti', 'Aika tunneissa'),
    (6, 'sv', 'Timme', 'Tid i timmar'),
    (6, 'ja', '時間', '時間単位'),
    (6, 'ar', 'ساعة', 'الوقت بالساعات')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

