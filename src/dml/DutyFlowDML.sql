INSERT INTO Category (parentCategoryId, categoryName, depth) VALUES (NULL, '주류', 1);
INSERT INTO Category (parentCategoryId, categoryName, depth) VALUES (1, '위스키', 2);
INSERT INTO Category (parentCategoryId, categoryName, depth) VALUES (1, '와인', 2);
INSERT INTO Category (parentCategoryId, categoryName, depth) VALUES (NULL, '화장품', 1);
INSERT INTO Category (parentCategoryId, categoryName, depth) VALUES (NULL, '전자제품', 1);

INSERT INTO Manager (managerName, managerType) VALUES ('김관리', '브랜드관리자');
INSERT INTO Manager (managerName, managerType) VALUES ('이관리', '브랜드관리자');
INSERT INTO Manager (managerName, managerType) VALUES ('박관리', '브랜드관리자');

INSERT INTO Brand (brandName, managerId) VALUES ('Johnnie Walker', 1);
INSERT INTO Brand (brandName, managerId) VALUES ('Ballantines', 1);
INSERT INTO Brand (brandName, managerId) VALUES ('Chanel', 2);
INSERT INTO Brand (brandName, managerId) VALUES ('Dior', 2);
INSERT INTO Brand (brandName, managerId) VALUES ('IQOS', 3);

INSERT INTO Product (categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (2, 1, '조니워커 블루라벨', 50, 750, 220, 298000, 10, TO_DATE('2026-01-13', 'YYYY-MM-DD'));
INSERT INTO Product (categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (2, 2, '발렌타인 21년', 30, 700, 180, 243000, 10, TO_DATE('2025-05-13', 'YYYY-MM-DD'));
INSERT INTO Product (categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (4, 3, '샤넬 넘버5 오드퍼퓸', 100, 100, 120, 162000, 10, TO_DATE('2024-05-13', 'YYYY-MM-DD'));
INSERT INTO Product (categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (4, 4, '디올 립글로우', 150, 15, 45, 61000, 10, TO_DATE('2024-05-13', 'YYYY-MM-DD'));
INSERT INTO Product (categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (5, 5, '아이코스 일루마', 80, 1, 95, 128000, 10, TO_DATE('2023-05-13', 'YYYY-MM-DD'));

INSERT INTO Event (productId, discountRate) VALUES (1, 10);
INSERT INTO Event (productId, discountRate) VALUES (2, 15);
INSERT INTO Event (productId, discountRate) VALUES (3, 5);
INSERT INTO Event (productId, discountRate) VALUES (4, 20);
INSERT INTO Event (productId, discountRate) VALUES (5, 12);

INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-10', 'YYYY-MM-DD'), 1350.2500, 'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-11', 'YYYY-MM-DD'), 1348.7800, 'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-12', 'YYYY-MM-DD'), 1345.1200, 'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-13', 'YYYY-MM-DD'), 1342.5600, 'N');
INSERT INTO ExchangeRate (exchangeDate, exchangeRate, isLatest) VALUES (TO_DATE('2026-05-14', 'YYYY-MM-DD'), 1339.9000, 'Y');

COMMIT;