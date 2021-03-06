package com.chcreation.geprin_sion.model

enum class ETable{
    USER,
    JEMAAT,
    ADMIN,
    CONTENT,
    MERCHANT,
    LIKE,
    REMAJA,
    ABSENT,
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

enum class EJemaat{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    NAMA,
    PROVINSI,
    KOTA,
    KECAMATAN,
    KELURAHAN,
    RT,
    RW,
    ALAMAT,
    GENDER,
    GOL_DARAH,
    TEMPAT_LAHIR,
    TANGGAL_LAHIR,
    NO_TEL,
    NOTE,
    BAPTIS,
    NO_SERTIFIKAT,
    TEMPAT_BAPTIS,
    TANGGAL_BAPTIS,
    IMAGE,
    ID,
    KEY,
    STATUS
}

enum class ERemaja{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    NAMA,
    KELAS,
    WARNA_FAV,
    SEKOLAH,
    HOBBY,
    ALAMAT,
    GENDER,
    TEMPAT_LAHIR,
    TANGGAL_LAHIR,
    NO_TEL,
    NOTE,
    IS_PADUS,
    JENIS_SUARA,
    IS_PELAYANAN,
    LITURGOS,
    PENYAMBUT,
    PIANIS,
    GITARIS,
    LCD,
    PENGURUS,
    ABSENSI,
    KOLEKTOR,
    PADUS,
    IMAGE,
    ID,
    KEY,
    STATUS
}

enum class EAbsent{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    ABSENT_DATE,
    DETAIL,
    CHANNEL,
    TYPE,
    KEY,
    STATUS
}

enum class EAbsentType{
    All,
    PADUS
}

enum class EAbsentDetail{
    NAME,
    PRESENT
}

enum class ELike{
    CONTENT_ID
}

enum class EContent{
    CREATED_DATE,
    UPDATED_DATE,
    CREATED_BY,
    UPDATED_BY,
    TOTAL_LIKE,
    IMAGE_CONTENT,
    LINK,
    CAPTION,
    USER_CODE,
    USER_NAME,
    USER_IMAGE,
    STATUS,
    TYPE,
    KEY,
    CHANNEL
}

enum class EUser{
    CREATED_DATE,
    UPDATED_DATE,
    NAME,
    EMAIL,
    ACTIVE,
    PROVINSI,
    KOTA,
    KECAMATAN,
    KELURAHAN,
    RT,
    RW,
    ALAMAT,
    GENDER,
    GOL_DARAH,
    TEMPAT_LAHIR,
    TANGGAL_LAHIR,
    NO_TEL,
    IMAGE,
    CODE,
    STATUS
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

enum class EGender{
    All,
    Pria,
    Perempuan
}

enum class EBaptis{
    All,
    Yes,
    No,
}

enum class EJenisSuara{
    SOPRAN,
    ALTO,
    TENOR,
    BASS
}

enum class EGolDarah{
    All,
    A,
    B,
    O,
    AB
}


enum class EMessageResult{
    SUCCESS,
    UPDATE,
    DELETE_SUCCESS,
    FAILURE,
    FETCH_JEMAAT_SUCCESS,
    FETCH_REMAJA_SUCCESS,
    FETCH_JEMAAT_BY_KEY_SUCCESS,
    FETCH_LIKE_SUCCESS,
    FETCH_USER_SUCCESS,
    FETCH_ADMIN_SUCCESS,
    FETCH_USER_LIST_SUCCESS,
    FETCH_REMAJA_BY_KEY_SUCCESS,
    FETCH_ABSENT_SUCCESS,
    FETCH_CONTENT_SUCCESS
}

enum class EResultCode(var value:Int){
    SUCCESS(1),
    FAILED(2)
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
    ADMIN,
    USER,
    DE_ACTIVE
}

enum class EContentType{
    All,
    Warta,
    Pengumuman,
    File,
    Streaming
}

enum class EChannel{
    All,
    Umum,
    Remaja
}

enum class ESharedPreference{
    MERCHANT,
    MERCHANT_CREDENTIAL,
    MERCHANT_IMAGE,
    USER_GROUP,
    NO_TELP,
    ADDRESS,
    NAME,
    IMAGE,
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
