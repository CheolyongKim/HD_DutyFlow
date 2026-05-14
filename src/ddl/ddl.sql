CREATE TABLE Membership (
    grade             VARCHAR2(100)   NOT NULL,
    criteria          VARCHAR2(100)   NOT NULL,
    discountRate      NUMBER    DEFAULT 0 NOT NULL,

    CONSTRAINT PK_MEMBERSHIP PRIMARY KEY (grade)
);

CREATE TABLE Manager (
    managerId     NUMBER          NOT NULL,
    managerName   VARCHAR2(20)    NOT NULL,
    managerType   VARCHAR2(20)    NOT NULL,

    CONSTRAINT PK_MANAGER PRIMARY KEY (managerId)
);

CREATE TABLE ShopManager (
    managerId             NUMBER  NOT NULL,
    annualLeaveCount      NUMBER  NULL,

    CONSTRAINT PK_SHOPMANAGER PRIMARY KEY (managerId),
    CONSTRAINT FK_MANAGER_TO_SHOPMANAGER_1
        FOREIGN KEY (managerId)
        REFERENCES Manager (managerId)
);

CREATE TABLE AirportManager (
    managerId     NUMBER      NOT NULL,
    shiftTime     DATE   NULL,

    CONSTRAINT PK_AIRPORTMANAGER PRIMARY KEY (managerId),
    CONSTRAINT FK_MANAGER_TO_AIRPORTMANAGER_1
        FOREIGN KEY (managerId)
        REFERENCES Manager (managerId)
);

CREATE TABLE Category (
    categoryId        NUMBER          NOT NULL,
    parentCategoryId  NUMBER          NULL,
    categoryName      VARCHAR2(100)   NOT NULL,
    depth             NUMBER          NOT NULL,

    CONSTRAINT PK_CATEGORY PRIMARY KEY (categoryId)
);

CREATE TABLE Brand (
    brandId       NUMBER          NOT NULL,
    brandName     VARCHAR2(100)   NOT NULL,
    managerId     NUMBER          NOT NULL,

    CONSTRAINT PK_BRAND PRIMARY KEY (brandId),
    CONSTRAINT FK_MANAGER_TO_BRAND_1
        FOREIGN KEY (managerId)
        REFERENCES Manager (managerId)
);

CREATE TABLE Product (
    productId         NUMBER          NOT NULL,
    categoryId        NUMBER          NOT NULL,
    brandId           NUMBER          NOT NULL,
    productName       VARCHAR2(100)   NOT NULL,
    stockAmount       NUMBER          NOT NULL,
    capacity          NUMBER          NOT NULL,
    priceUsd          NUMBER          NOT NULL,
    priceKrw          NUMBER          NOT NULL,
    thresholdValue    NUMBER          NOT NULL,
    madeAt       DATE            NOT NULL,

    CONSTRAINT PK_PRODUCT PRIMARY KEY (productId),
    CONSTRAINT FK_CATEGORY_TO_PRODUCT_1
        FOREIGN KEY (categoryId)
        REFERENCES Category (categoryId),
    CONSTRAINT FK_BRAND_TO_PRODUCT_1
        FOREIGN KEY (brandId)
        REFERENCES Brand (brandId)
);

CREATE TABLE Event (
    productId      NUMBER          NOT NULL,
    discountRate   NUMBER    NOT NULL,

    CONSTRAINT PK_EVENT PRIMARY KEY (productId),
    CONSTRAINT FK_PRODUCT_TO_EVENT_1
        FOREIGN KEY (productId)
        REFERENCES Product (productId)
);

CREATE TABLE Stock (
    stockId           NUMBER  NOT NULL,
    productId         NUMBER  NOT NULL,
    manufacturedDate  DATE    NOT NULL,
    amount            NUMBER  NOT NULL,

    CONSTRAINT PK_STOCK PRIMARY KEY (stockId, productId),
    CONSTRAINT FK_PRODUCT_TO_STOCK_1
        FOREIGN KEY (productId)
        REFERENCES Product (productId)
);
CREATE TABLE Regulation (
    regulationId      NUMBER  NOT NULL,
    categoryId        NUMBER  NOT NULL,
    limitCapacity     NUMBER  NOT NULL,
    establishedDate   DATE    NOT NULL,
    overageRate   NUMBER    NOT NULL,

    CONSTRAINT PK_REGULATION PRIMARY KEY (regulationId),
    CONSTRAINT FK_CATEGORY_TO_REGULATION_1
        FOREIGN KEY (categoryId)
        REFERENCES Category (categoryId)
);

CREATE TABLE ExchangeRate (
    exchangeDate  DATE            NOT NULL,
    exchangeRate  NUMBER(10, 4)   NOT NULL,
    isLatest      CHAR(1)         NOT NULL,

    CONSTRAINT PK_EXCHANGERATE PRIMARY KEY (exchangeDate),
    CONSTRAINT CHK_EXCHANGERATE_IS_LATEST
        CHECK (isLatest IN ('Y', 'N'))
);

CREATE TABLE Member (
    memberId              NUMBER          NOT NULL,
    grade                 VARCHAR2(100)   NOT NULL,
    loginId               VARCHAR2(100)   NOT NULL,
    password              VARCHAR2(100)   NOT NULL,
    name                  VARCHAR2(50)    NOT NULL,
    birthDate             DATE            NOT NULL,
    phoneNumber           VARCHAR2(100)   NOT NULL,
    gradeSelectionDate    DATE            NOT NULL,
    adult                 CHAR(1)         DEFAULT 'N' NOT NULL,
    passportNumber        VARCHAR2(200)   NOT NULL,
    passportExpiryDate    DATE            NOT NULL,

    CONSTRAINT PK_MEMBER PRIMARY KEY (memberId),
    CONSTRAINT FK_MEMBERSHIP_TO_MEMBER_1
        FOREIGN KEY (grade)
        REFERENCES Membership (grade),
    CONSTRAINT CHK_MEMBER_GRADE_VAL
        CHECK (grade IN ('SILVER', 'GOLD', 'BLACK', 'PRESTIGE')),
    CONSTRAINT CHK_MEMBER_ADULT
        CHECK (adult IN ('Y', 'N')),
    CONSTRAINT UK_MEMBER_LOGIN_ID
        UNIQUE (loginId),
    CONSTRAINT UK_MEMBER_PHONE_NUMBER
        UNIQUE (phoneNumber),
    CONSTRAINT UK_MEMBER_PASSPORT_NUMBER
        UNIQUE (passportNumber)
);

CREATE TABLE Flight (
    flightId       NUMBER          NOT NULL,
    flightCode     VARCHAR2(20)    NOT NULL,
    departureAt    DATE       NOT NULL,
    isDelayed      NUMBER(1)       DEFAULT 0 NOT NULL,

    CONSTRAINT PK_FLIGHT PRIMARY KEY (flightId),
    CONSTRAINT CHK_FLIGHT_IS_DELAYED
        CHECK (isDelayed IN (0, 1))
);

CREATE TABLE FlightBook (
    reservationId    NUMBER         NOT NULL,
    memberId         NUMBER         NOT NULL,
    flightId         NUMBER         NOT NULL,
    reservationCode  VARCHAR2(200)  NOT NULL,

    CONSTRAINT PK_FLIGHTBOOK PRIMARY KEY (reservationId),
    CONSTRAINT FK_MEMBER_TO_FLIGHTBOOK_1
        FOREIGN KEY (memberId)
        REFERENCES Member (memberId),
    CONSTRAINT FK_FLIGHT_TO_FLIGHTBOOK_1
        FOREIGN KEY (flightId)
        REFERENCES Flight (flightId)
);

CREATE TABLE Orders (
    orderId         NUMBER         NOT NULL,
    memberId        NUMBER         NOT NULL,
    reservationId   NUMBER         NOT NULL,
    exchangeDate    DATE           NOT NULL,
    orderedAt       DATE      NOT NULL,
    orderState     VARCHAR2(30)   NOT NULL,
    totalAmount     NUMBER(12, 2)  NOT NULL,

    CONSTRAINT PK_ORDERS PRIMARY KEY (orderId),
    CONSTRAINT FK_MEMBER_TO_ORDERS_1
        FOREIGN KEY (memberId)
        REFERENCES Member (memberId),
    CONSTRAINT FK_FLIGHTBOOK_TO_ORDERS_1
        FOREIGN KEY (reservationId)
        REFERENCES FlightBook (reservationId),
    CONSTRAINT FK_EXCHANGERATE_TO_ORDERS_1
        FOREIGN KEY (exchangeDate)
        REFERENCES ExchangeRate (exchangeDate),
    CONSTRAINT CHK_ORDER_STATE
        CHECK (orderState IN (
            'ORDERED',
            'PAID',
            'PICKUP_RESERVED',
            'PICKED_UP',
            'CANCELED',
            'NO_SHOW'
        ))
);

CREATE TABLE OrderDetail (
    productId       NUMBER          NOT NULL,
    orderId         NUMBER          NOT NULL,
    quantity        NUMBER          NOT NULL,
    discountAmount  NUMBER(12, 2)   NOT NULL,
    dollarPrice     NUMBER(12, 2)   NOT NULL,

    CONSTRAINT PK_ORDERDETAIL PRIMARY KEY (productId, orderId),
    CONSTRAINT FK_PRODUCT_TO_ORDERDETAIL_1
        FOREIGN KEY (productId)
        REFERENCES Product (productId),
    CONSTRAINT FK_ORDERS_TO_ORDERDETAIL_1
        FOREIGN KEY (orderId)
        REFERENCES Orders (orderId)
);

CREATE TABLE Pickup (
    pickupId            VARCHAR2(20)   NOT NULL,
    orderId             NUMBER          NOT NULL,
    managerId           NUMBER         NOT NULL,
    pickupAvailableAt   DATE      NOT NULL,
    pickedUpAt          DATE      NULL,

    CONSTRAINT PK_PICKUP PRIMARY KEY (pickupId),
    CONSTRAINT FK_ORDERS_TO_PICKUP_1
        FOREIGN KEY (orderId)
        REFERENCES Orders (orderId),
    CONSTRAINT FK_AIRPORTMANAGER_TO_PICKUP_1
        FOREIGN KEY (managerId)
        REFERENCES AirportManager (managerId)
);

CREATE TABLE StockPurchase (
    orderId     NUMBER          NOT NULL,
    productId   NUMBER          NOT NULL,
    orderDate   DATE            NOT NULL,
    amount      NUMBER          NOT NULL,
    status      VARCHAR2(20)    NOT NULL,

    CONSTRAINT PK_STOCKPURCHASE PRIMARY KEY (orderId),
    CONSTRAINT FK_PRODUCT_TO_STOCKPURCHASE_1
        FOREIGN KEY (productId)
        REFERENCES Product (productId),
    CONSTRAINT CHK_STOCKPURCHASE_STATUS
        CHECK (status IN (
            'REQUESTED',
            'APPROVED',
            'RECEIVED',
            'CANCELED'
        ))
);

CREATE TABLE ShoppingCart (
    productId   NUMBER  NOT NULL,
    memberId    NUMBER  NOT NULL,
    amount      NUMBER  NOT NULL,

    CONSTRAINT PK_SHOPPINGCART PRIMARY KEY (productId, memberId),
    CONSTRAINT FK_PRODUCT_TO_SHOPPINGCART_1
        FOREIGN KEY (productId)
        REFERENCES Product (productId),
    CONSTRAINT FK_MEMBER_TO_SHOPPINGCART_1
        FOREIGN KEY (memberId)
        REFERENCES Member (memberId)
);

CREATE TABLE SystemLog (
    logId       NUMBER          NOT NULL,
    errorCode   VARCHAR2(20)    NULL,
    logMessage  VARCHAR2(100)   NOT NULL,

    CONSTRAINT PK_SYSTEMLOG PRIMARY KEY (logId)
);

SELECT table_name FROM user_tables;