package br.ufc.predetect.ble.managers

import br.ufc.predetect.ble.domain.Beacon

object BeaconRepository {
    var beaconsBatch : HashMap<String, MutableList<Beacon>> = hashMapOf()
}