package br.ufc.predetect.ble.interfaces

import br.ufc.predetect.ble.domain.Beacon
import br.ufc.quixada.predetect.common.interfaces.NetworkListener

interface BeaconListener : NetworkListener<Beacon>