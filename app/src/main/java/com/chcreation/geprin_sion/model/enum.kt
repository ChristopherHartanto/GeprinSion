package com.chcreation.geprin_sion.model

enum class ETable{
    USER,
    USER_LIST,
    PRODUCT,
    MERCHANT,
    AVAILABLE_MERCHANT,
    CUSTOMER,
    CAT,
    TRANSACTION,
    PAYMENT,
    ENQUIRY,
    STOCK_MOVEMENT,
    USER_ACCEPTANCE,
    ABOUT
}

enum class EAbout{
    TEXT1,
    TEXT2,
    TEXT3,
    IMAGE
}
enum class EProduct{
    NAME,
    DESC,
    PRICE,
    COST,
    MANAGE_STOCK,
    STOCK,
    PROD_KEY,
    PROD_CODE,
    UOM_CODE,
    IMAGE,
    CAT,
    CODE,
    STATUS_CODE,
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY
}

enum class EMerchant{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    NAME,
    BUSINESS_INFO,
    NO_TELP,
    CAT,
    IMAGE,
    USER_LIST,
    ADDRESS
}

enum class EAvailableMerchant{
    CREATED_DATE,
    UPDATED_DATE,
    STATUS,
    CREDENTIAL,
    USER_GROUP,
    NAME
}

enum class ECustomer{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    NAME,
    EMAIL,
    PHONE,
    ADDRESS,
    NOTE,
    CODE,
    IMAGE,
    STATUS_CODE
}

enum class ETransaction{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    TOTAL_PRICE,
    TOTAL_OUTSTANDING,
    DISCOUNT,
    TAX,
    PAYMENT_METHOD,
    DETAIL,
    CUST_CODE,
    NOTE,
    TRANS_CODE,
    USER_CODE,
    STATUS_CODE
}

enum class E_Enquiry{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    TRANS_KEY,
    CUST_CODE,
    PROD_KEY,
    PROD_CODE,
    MANAGE_STOCK,
    STOCK,
    STATUS_CODE
}

enum class EUser{
    CREATED_DATE,
    UPDATED_DATE,
    NAME,
    EMAIL,
    ADDRESS,
    TEL,
    IMAGE,
    CODE
}

enum class EPerson{
    CREATED_DATE,
    CREATED_BY,
    UPDATED_DATE,
    UPDATED_BY,
    NAME,
    EMAIL,
    ADDRESS,
    TEL,
    IMAGE,
    GOL_DARAH,
    UMUR
}

enum class EDataType{
    STRING,
    INT,
    FLOAT
}


enum class EMessageResult{
    SUCCESS,
    UPDATE,
    DELETE_SUCCESS,
    FAILURE,
    FETCH_PROD_SUCCESS,
    FETCH_AVAIL_MERCHANT_SUCCESS,
    FETCH_MERCHANT_SUCCESS,
    FETCH_CATEGORY_SUCCESS,
    FETCH_CUSTOMER_SUCCESS,
    FETCH_CUSTOMER_TRANSACTION_SUCCESS,
    FETCH_TRANS_SUCCESS,
    FETCH_TRANS_LIST_PAYMENT_SUCCESS,
    FETCH_STOCK_MOVEMENT_SUCCESS,
    FETCH_PEND_PAYMENT_SUCCESS,
    FETCH_USER_SUCCESS,
    FETCH_USER_LIST_SUCCESS,
    CREATE_INVITATION_SUCCESS,
    FETCH_INVITATION_SUCCESS
}

enum class EPaymentMethod{
    CASH,
    CARD
}

enum class EStatusCode{
    NEW,
    PENDING,
    DONE,
    CANCEL,
    DELETE,
    ACTIVE
}

enum class EStatusUser{
    ACTIVE,
    DE_ACTIVE
}

enum class EStatusStock{
    INBOUND,
    OUTBOUND,
    MISSING,
    CANCEL
}

enum class EUserGroup{
    MANAGER,
    WAITER
}

enum class ESharedPreference{
    MERCHANT,
    MERCHANT_CREDENTIAL,
    MERCHANT_IMAGE,
    USER_GROUP,
    NO_TELP,
    ADDRESS,
    NAME,
    EMAIL
}

enum class EMonth(var value:Int){
    All(99),
    January(1),
    February(2),
    March(3),
    April(4),
    May(5),
    June(6),
    July(7),
    August(8),
    September(9),
    October(10),
    November(11),
    December(12)
}
