# PreDetect  
_A simple API for indoor application development._  

[![RELEASE](https://jitpack.io/v/gabrielczar/predetect.svg)](https://jitpack.io/#gabrielczar/predetect)

> Also in&nbsp; [![PT-BR](https://assets-cdn.github.com/images/icons/emoji/unicode/1f1e7-1f1f7.png)](README_pt.md)

---

List as information (MAC, SSID, RSSI, Approximate Distance) of all networks, they are Wi-Fi or Bluetooth, close to the device.
Possibility to get a percentage of time that a piece was close to a certain network.

### ADDING DEPENDENCY:  
To make a library available, a JitPack platform will be used, which shares a more original version of the repository.
	
- In the ```build.gradle``` file add a JitPack dependency:
	
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

- In the ```app/build.gradle``` file add a API dependency:

```gradle
dependencies {
	compile 'com.github.REPOSITORY_OWNER:predetect:VERSION'
}
```

- If you need to use only one of the modules, simply add the dependency as follows:
    - Modules: 
        - wifi
        - ble
        
```gradle
dependencies {
	compile 'com.github.REPOSITORY_OWNER:predetect:MODULE:VERSION'
}
```

### HOW TO USE:

1. Step: Implement the interface "Listener"
2. Step: Overrides the methods ```onChange``` e ```getListenerContext```
3. Step: Obtain an instance from the "NetworkManager" class
4. Step: Register your Activity to get network data
5. Step: In the ```OnPause```, ```OnDestroy``` methods, use the ```unregisterListener``` to remove your activity from the list of listeners.
6. Step: In the ```OnResume``` method, log your activity again.

All the steps are represented just below in the example class:
	
```kotlin
class MainActivity : AppCompatActivity(), WifiListener {

    private lateinit var manager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain an instance
        manager = NetworkManager.getInstance()

        // Register your Activity to get network data from Wifi Network
        manager.registerListener(this)
    }

    override fun onChange(list: List<WiFiData>) {
		// List nearby WiFi networks
	}

    override fun getListenerContext(): Context = this

    override fun onPause() {
        manager.unregisterListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        manager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onResume() {
        manager.registerListener(this)
        super.onResume()
    }
}
```

### OBSERVING THE PRESENCE OF THE DEVICE:

This feature allows you to detect a percentage of the presence of a device within a radius of the point.

- Mandatory Data:
	- List of MACs of reference points.
	- Total time to check in minutes.
	- Radius distance in meters.
	- Checking interval in minutes.
	
- Output:
    - Data list with all network information, including the percentage of time the device is near.
    
- How to use:  

1. Step: Implement the interface "Observer"
2. Step: Overrides the method ```onObservingEnds(networkResult: NetworkResult<Data>)```
3. Step: Obtain an instance from the "NetworkManager" class
4. Step: The service will start as soon as you call the method ```observeNetwork(observer : Observer, listMACsToObserve : List<String>, timeInMinutes : Int, maxRangeInMeters : Double, intervalTimeInMinutes : Int)```, 
5. Step: At the end of the service will return the results in the overrides method, the data will be represented by a wrapper ```NetworkResult```.

```kotlin
networkResult
    .onSuccess { list: List<Data>? ->  
        // TODO    
    }
    .onFail { list: List<Data>? ->  
        // TODO 
    }
    .onUndefinedNetwork {   
        // TODO 
    }    
```

> The parameter ```list: List <Data>?``` Represents a list with the results of the observation of the device. For each object you can get a presence percentage.
    
