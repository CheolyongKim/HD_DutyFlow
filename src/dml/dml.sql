INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (1, NULL, '주류', 1);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (2, 1, '위스키', 2);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (3, 1, '와인', 2);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (4, NULL, '화장품', 1);
INSERT INTO Category (categoryId, parentCategoryId, categoryName, depth) VALUES (5, NULL, '전자제품', 1);

INSERT INTO Manager (managerId, managerName, managerType) VALUES (1, '김관리', '브랜드관리자');
INSERT INTO Manager (managerId, managerName, managerType) VALUES (2, '이관리', '브랜드관리자');
INSERT INTO Manager (managerId, managerName, managerType) VALUES (3, '박관리', '브랜드관리자');

INSERT INTO Brand (brandId, brandName, managerId) VALUES (1, 'Johnnie Walker', 1);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (2, 'Ballantines', 1);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (3, 'Chanel', 2);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (4, 'Dior', 2);
INSERT INTO Brand (brandId, brandName, managerId) VALUES (5, 'IQOS', 3);

INSERT INTO Product (productId, categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (1, 2, 1, '조니워커 블루라벨', 50, 750, 220, 298000, 10, TO_DATE('2026-01-13 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO Product (productId, categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (2, 2, 2, '발렌타인 21년', 30, 700, 180, 243000, 10,TO_DATE('2025-05-13 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO Product (productId, categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (3, 4, 3, '샤넬 넘버5 오드퍼퓸', 100, 100, 120, 162000, 10,TO_DATE('2024-05-13 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO Product (productId, categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (4, 4, 4, '디올 립글로우', 150, 15, 45, 61000, 10, TO_DATE('2024-05-13 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO Product (productId, categoryId, brandId, productName, stockAmount, capacity, priceUsd, priceKrw, thresholdValue, madeAt) VALUES (5, 5, 5, '아이코스 일루마', 80, 1, 95, 128000, 10, TO_DATE('2023-05-13 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));


select * from category;