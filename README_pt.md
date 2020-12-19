# PreDetect  
_Uma API simples para desenvolvimento de aplicaçoes indoor._  

[![RELEASE](https://jitpack.io/v/gabrielczar/predetect.svg)](https://jitpack.io/#gabrielczar/predetect)

Listar as informações (MAC, SSID, RSSI, Distância aproximada) de todas as redes, sejam elas Wi-Fi ou Bluetooth, próximas ao dispositivo.
Possibilidade de se obter a porcentagem de tempo que um dispositivo ficou próximo a determinada rede.

### ADICIONANDO A DEPENDÊNCIA:  

Para disponibilização da biblioteca será utilizada a plataforma Jitpack, que compartilha a versão mais atualizada do repositório.
	
- No arquivo ```build.gradle``` adicione a dependência do jitpack:
	
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

- No arquivo ```app/build.gradle``` adicione a dependência da API:

```gradle
dependencies {
	compile 'com.github.REPOSITORY_OWNER:predetect:VERSION'
}
```

- Caso necessite utilizar apenas um dos módulos basta adicionar a dependência da seguinte forma:

```gradle
dependencies {
	compile 'com.github.REPOSITORY_OWNER:predetect:MODULE:VERSION'
}
```

### USO BÁSICO:

1. Passo: Implementar a interface Listener
2. Passo: Implementar os metodos "onChange" e "getListenerContext"
3. Passo: Obter a intância da classe NetworkManager
3. Passo: Registrar a sua Activity para receber os dados das redes
4. Passo: Nos metodos "OnPause", "OnDestroy", utilizar o metodo "unregisterListener" para remover sua activity da lista de listeners.
5. Passo: No metodo "OnResume", registre sua activity novamente.

Todos os passos estão representados logo abaixo na classe de exemplo:
	
```kotlin
class MainActivity : AppCompatActivity(), Listener {

    private lateinit var manager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtendo a instancia de NetworkManager
        manager = NetworkManager.getInstance()

        // Registrando sua activity para receber os dados das redes WiFi
        manager.registerListener(this)
    }

    override fun onChange(list: List<WiFiData>) {
		// Lista de redes WiFi próximas.
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

### OBSERVANDO A PRESENÇA DO DISPOSITIVO:

Essa funcionalidade permite detectar a porcentagem de presença de um dispositivo dentro de um raio de distância do ponto.

- Dados necessarios:
	- Lista de MAC do(s) ponto(s) de referência.
	- Tempo total para checagem em minutos.
	- Distância do raio em metros. 
	- (Opcional) Intervalo de checagem em minutos.
- Retorno:
	- Lista de Data com todas as informações da rede, inclusive a porcentagem de tempo que o dispositivo ficou próximo.

- Modo de Uso:  

1. Step: Implemente a interface "Observer"
2. Step: Sobrescreva o método ```onObservingEnds(networkResult: NetworkResult<Data>)```
3. Step: Obtenha uma instância da classe "NetworkManager" 
4. Step: O serviço irá iniciar assim que chamar o método ```observeNetwork(observer : Observer, listMACsToObserve : List<String>, timeInMinutes : Int, maxRangeInMeters : Double, intervalTimeInMinutes : Int)``` 
5. Step: Quando o serviço terminar o resultado será retornado no meétodo sobrescrito, os dados serão abstraidos em um wrapper ```NetworkResult```.

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

> O parâmetro ```list: List<Data>?``` representa a lista com os resultados da observação do dispositivo. Para cada objeto nessa lista é possível obter a porcentagem de presença. 
    
    
