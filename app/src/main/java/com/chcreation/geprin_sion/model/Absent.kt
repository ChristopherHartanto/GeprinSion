package com.chcreation.geprin_sion.model

data class Absent(
    var DETAIL : String? = "",
    var CHANNEL : String? = "",
    var TYPE : String? = "",
    var STATUS : String? = EStatusCode.ACTIVE.toString(),
    var ABSENT_DATE : String? = "",
    var KEY : String ? = "",
    var CREATED_DATE : String? = "",
    var CREATED_BY : String? = "",
    var UPDATED_DATE : String? = "",
    var UPDATED_BY : String? = ""
)

data class AbsentDetail(
    var ID: String? = "",
    var NAME: String? = "",
    var IMAGE: String? = "",
    var PRESENT: Boolean? = false
)