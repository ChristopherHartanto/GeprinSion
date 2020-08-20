package com.chcreation.geprin_sion.util

import android.content.Context
import android.content.SharedPreferences
import android.view.animation.AlphaAnimation
import com.chcreation.geprin_sion.model.EDataType
import com.chcreation.geprin_sion.model.ESharedPreference
import com.chcreation.geprin_sion.main.ErrorActivity
import org.jetbrains.anko.startActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private lateinit var sharedPreference: SharedPreferences

var RESULT_CLOSE_ALL = 1111

fun getSinode() : String = "GEPRIN"

fun removeAllSharedPreference(context: Context){
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
    val editor = sharedPreference.edit()
    editor.putString(ESharedPreference.NAME.toString(),"")
    editor.putString(ESharedPreference.EMAIL.toString(),"")
    editor.putString(ESharedPreference.MERCHANT.toString(),"")
    editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(),"")
    editor.putString(ESharedPreference.NO_TELP.toString(),"")
    editor.putString(ESharedPreference.USER_GROUP.toString(),"")
    editor.putString(ESharedPreference.ADDRESS.toString(),"")
    editor.apply()
}

fun getName(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(
        ESharedPreference.NAME.toString(),"").toString()
}

fun getEmail(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(
        ESharedPreference.EMAIL.toString(),"").toString()
}

fun getMerchant(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(
        ESharedPreference.MERCHANT.toString(),"").toString()
}

fun getMerchantCredential(context: Context) : String{
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    return sharedPreference.getString(
        ESharedPreference.MERCHANT_CREDENTIAL.toString(),"").toString()
}

fun setDataPreference(context: Context,key: String, value: Any, dataType : EDataType){
    sharedPreference =  context.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
    val editor = sharedPreference.edit()

    when (dataType) {
        EDataType.STRING -> editor.putString(key,value.toString())
        EDataType.INT -> editor.putInt(key,value.toString().toInt())
        EDataType.FLOAT -> editor.putFloat(key,value.toString().toFloat())
    }

    editor.apply()
}

fun normalClickAnimation() : AlphaAnimation = AlphaAnimation(10F,0.5F)

fun dateFormat() : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

fun getYear(convertDate: String): Int {
    val date = dateFormat().parse(convertDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.YEAR)
}

fun getMonth(convertDate: String): Int {
    val date = dateFormat().parse(convertDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.MONTH)
}

fun getCurrentMonth(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.MONTH)
}

fun getCurrentYear(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.YEAR)
}

fun getDateOfMonth(convertDate: String): Int {
    val date = dateFormat().parse(convertDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun parseDateFormat(date: String) : String {
    val currentFormat = dateFormat().parse(date)
    val newFormat = SimpleDateFormat("dd MMM yyyy").format(currentFormat).toString()

    return newFormat
}

fun parseDateFormatFull(date: String) : String {
    val currentFormat = dateFormat().parse(date)
    val newFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(currentFormat).toString()

    return newFormat
}

fun parseTimeFormat(date: String) : String {
    var currentFormat = dateFormat().parse(date)
    var newFormat = SimpleDateFormat("HH:mm:ss").format(currentFormat).toString()

    return newFormat
}

fun indonesiaCurrencyFormat() : NumberFormat{  //  ex : indoCurrencyFormat().format(10000)
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance("IDR")
    return  format
}

fun receiptFormat(number: Int) : String {
    var value = ""
    if (number < 10000)
        value = "#"+String.format("%05d",number)
    else if (number < 100000)
        value = "#"+String.format("%06d",number)
    else
        value = "#"+String.format("%07d",number)

    return value
}

fun showError(context: Context,message: String)
{
    ErrorActivity.errorMessage = message
    context.startActivity<ErrorActivity>()
}

fun encodeEmail(email:String): String{
    val index = if (email == "") 0 else email.indexOf('.',0)
    return email.substring(0,index)
}