package br.ufc.predetect.ble.utils

import android.util.Log
import br.ufc.predetect.ble.constants.LOG_PATH
import br.ufc.predetect.ble.constants.LOG_TAG
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.file.FilePrinter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun sleepThread(seconds : Long) = try { Thread.sleep(seconds * 1000) } catch (e : Exception) { Log.e(LOG_TAG, "Error in Thread Sleep | $e") }

fun getActualDateString() : String = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT, Locale.US).format(Date())

fun startXLogger() = getActualDateString().plus(" | ").apply {
    try {
        XLog.init(LogLevel.ALL, FilePrinter.Builder(File("$LOG_PATH/$LOG_TAG.log").path).build())
        Log.d(LOG_TAG, this.plus("INITIALING X_LOGGER"))
    } catch (e: Exception) {
        this.plus(e.message).run {
            XLog.e(this)
            Log.e(LOG_TAG, this)
        }
    }
}