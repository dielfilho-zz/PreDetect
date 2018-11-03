package br.ufc.predetect.ble.interfaces

import br.ufc.predetect.ble.data.Beacon
import br.ufc.quixada.predetect.common.interfaces.NetworkObserver

/**
 * @author Gabriel Cesar
 * @since 2018
 *
 */
interface BeaconObserver : NetworkObserver<Beacon>