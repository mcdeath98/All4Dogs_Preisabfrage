package ch.all4dogs.utils

import android.content.Context

object Prefs {

    fun getFolder(context: Context): String {
        return getString(context, "uri", null) ?: ""
    }

    fun setFolder(context: Context, value: String?) {
        setString(context, "uri", value)
    }

    private fun setString(context: Context, mKey: String?, mValue: String?) {
        val mSharedPreferences = context.getSharedPreferences(
            context.applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        val editor = mSharedPreferences.edit()
        editor.putString(mKey, mValue)
        editor.apply()
    }

    private fun getString(context: Context, mKey: String?, mDefValue: String?): String? {
        val mSharedPreferences = context.getSharedPreferences(
            context.applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        return mSharedPreferences.getString(mKey, mDefValue)
    }

}
