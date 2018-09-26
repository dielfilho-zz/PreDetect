package danielfilho.ufc.br.com.predetect.receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver;
import danielfilho.ufc.br.com.predetect.managers.NetworkManager;

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 * Updated by Gabriel Cesar, 2018
 *
 */
public class NetworkReceiverImpl extends BroadcastReceiver {

    private NetworkReceiver networkManager;

    public NetworkReceiverImpl() {
        networkManager = NetworkManager.getInstance();
    }

    @Override
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        networkManager.onNetworkReceive(context, intent);
    }

}
