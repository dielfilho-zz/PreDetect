package danielfilho.ufc.br.com.predetect.intefaces;

import java.util.List;

import br.ufc.quixada.predetect.common.interfaces.NetworkListener;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 * Updated by Gabriel Cesar, 2018
 *
 */
public interface WiFiListener extends NetworkListener {
    void onWiFiChange(List<WiFiData> list);
}
