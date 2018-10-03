package br.ufc.predetect.ble.receivers

import android.os.Handler
import br.ufc.predetect.ble.constants.BLE_SCANNED
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.interfaces.BeaconObserver
import br.ufc.quixada.predetect.common.receivers.NetworkObserverResultReceiver

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 * Updated by Gabriel Cesar, 2018
 */
class BLENetworkResultReceiver : NetworkObserverResultReceiver<Beacon> {

    constructor(beaconObserver : BeaconObserver) : super(BLE_SCANNED, beaconObserver, null)

    constructor(beaconObserver : BeaconObserver, handler: Handler) : super(BLE_SCANNED, beaconObserver, handler)

}