package danielfilho.ufc.br.com.predetect.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import danielfilho.ufc.br.com.predetect.constants.PredectConstants;
import danielfilho.ufc.br.com.predetect.managers.NetworkManager;
import danielfilho.ufc.br.com.predetect.intefaces.INetworkReceiver;
import danielfilho.ufc.br.com.predetect.services.NetworkObserverService;

/**
 * Created by Daniel Filho on 6/2/16.
 */
public class NetworkReceiver extends WakefulBroadcastReceiver {

    private INetworkReceiver networkManager;

    public NetworkReceiver() {
        networkManager = NetworkManager.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        networkManager.onNetworkReceive(context, intent);

    }
}
