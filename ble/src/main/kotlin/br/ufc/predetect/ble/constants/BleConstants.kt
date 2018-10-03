package br.ufc.predetect.ble.constants

import android.os.Environment

const val LOG_TAG = "LOG__PRE_DETECT__BLE"

const val BLE_SCANNED = "BLE_SCANNED"

const val BLE_BUNDLE = "BLE_BUNDLE"

// Get result receiver object at BLENetworkObserverService class
const val RESULT_RECEIVER = "RESULT_RECEIVER"

const val BLE_NETWORK_SLEEP_ERROR = "Error at Thread Sleep in BLENetworkObserverService"

// Action for start NetworkObserverService
const val ACTION_START_OBSERVING_SERVICE = "br.ufc.predetect.ble.NETWORK_SERVICE"

// Action when BLENetworkObserverService ends to observing a network
const val ACTION_OBSERVING_ENDS = "br.ufc.predetect.ble.OBSERVING_ENDS"

const val BUNDLE_FINISH_OBSERVING = "BUNDLE_FINISH_OBSERVING"

val LOG_PATH : String = Environment.getExternalStorageDirectory().path
