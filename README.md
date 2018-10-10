# PreDetect  
_Uma API simples para desenvolvimento de aplicaçoes indoor._  

[![RELEASE](https://jitpack.io/v/gabrielczar/predetect.svg)](https://jitpack.io/#gabrielczar/predetect)

Listar as informações (MAC, SSID, RSSI, Distância aproximada) de todas as redes, sejam elas Wi-Fi ou Bluetooth, próximas ao dispositivo.
Possibilidade de se obter a porcentagem de tempo que um dispositivo ficou próximo a determinada rede.

#### ADICIONANDO A DEPENDÊNCIA:  
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
	compile 'REPOSITORY:predetect:VERSION'
}
```

- Caso necessite utilizar apenas um dos módulos basta adicionar a dependência da seguinte forma:

```gradle
dependencies {
	compile 'REPOSITORY:predetect:MODULE:VERSION'
}
```

#### USO BÁSICO:

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
		// Lista de WiFi proximas.
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

#### OBSERVANDO A PRESENÇA DO DISPOSITIVO:

Essa funcionalidade permite detectar a porcentagem de presença de um dispositivo dentro de um raio de distância do ponto.

- Dados necessarios:
	- Lista de MAC do(s) ponto(s) de referência.
	- Tempo total para checagem em minutos.
	- Distância do raio em metros. 
	- (Opcional) Intervalo de checagem em minutos.
- Retorno:
	- Lista de Data com todas as informações da rede, inclusive a porcentagem de tempo que o dispositivo ficou próximo.

- Modo de Uso:  

   É necessário implementar a interface ```Observer```, criar uma instância de ```NetworkManager```, chamar o método ```observeNetwork(observer : Observer, listMACsToObserve : List<String>, timeInMinutes : Int, maxRangeInMeters : Double, intervalTimeInMinutes : Int)``` passando os respectivos parâmetros e 
   sobreescrever o metodo ```onObservingEnds(networkResult: NetworkResult<Data>)```. 
   Esse metodo é chamado pela API quando o serviço de observação termina. 
   São retornados um código de resultado e uma lista de Data representados por um wrapper ```NetworkResult```. 
   Para facilitar a utilização o wrapper possui os seguintes métodos:

```kotlin
networkResult
    .onSuccess { list: List<Data>? ->  
        // TODO    
    }
    .onFail { list: List<Data>? ->  
        // TODO 
    }
    .onUndefinedNetwork { list: List<Data>? ->  
        // TODO 
    }    
```

> O parâmetro ```list: List<Data>?``` representa a lista com os resultados da observação do dispositivo. Para cada objeto nessa lista é possível obter a porcentagem de presença. 
    
    
