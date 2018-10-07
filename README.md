# PreDetect  
_Uma API simples para desenvolvimento de aplicaçoes indoor._  

[![Release](https://jitpack.io/v/dielfilho/predetect.svg)](https://jitpack.io/#dielfilho/PreDetect)

Listar as informações (MAC, SSID, RSSI, Distância aproximada) de todas as redes Wi-Fi próximas ao dispositivo.
Possibilidade de se obter a porcentagem de tempo que um dispositivo ficou próximo a determinada rede Wi-Fi.

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
	compile 'com.github.danielfilho:predetect:VERSION'
}
```

- Caso necessite utilizar apenas um dos módulos basta adicionar a dependência da seguinte forma:

```gradle
dependencies {
	compile 'com.github.danielfilho:predetect:MODULE:VERSION'
}
```

#### USO BÁSICO:

0. Passo: Adicione a permissão para acessar Fine Location ou Coarse Location no Manifest.xml
1. Passo: Implementar a interface WiFiListener
2. Passo: Implementar os metodos "onWifiChange" e "getListenerContext"
3. Passo: Obter a intância da classe NetworkManager
3. Passo: Registrar a sua Activity para receber os dados das redes Wi-Fi
4. Passo: Nos metodos "OnPause", "OnDestroy", utilizar o metodo "unregisterListener" para remover sua activity da lista de listeners.
5. Passo: No metodo "OnResume", registre sua activity novamente.

Todos os passos estão representados logo abaixo na classe de exemplo:
	
```kotlin
class MainActivity : AppCompatActivity(), WiFiListener {

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

Essa funcionalidade permite detectar a porcentagem de presença de um dispositivo dentro de um raio de distancia do roteador.

- Dados necessarios:
	- Lista de MAC do(s) roteador(es) de referência
	- Tempo de checagem em Milisegundos
	- Distância do raio em metros. 

- Retorno:
	- Lista de WiFiData com todas as informações da rede, inclusive a porcentagem de tempo que o dispositivo ficou próximo.

- Modo de Uso:  

   Primeiramente e necessário implementar a interface ```WiFiObserver``` e 
   sobreescrever o metodo ```onObservingEnds(networkResult: NetworkResult<WiFiData>)```. 
   Esse metodo é chamado pela API quando o serviço de observação termina. 
   São retornados um código de resultado e uma lista de WiFiDatas representados por um wrapper ```NetworkResult```. 
   Para facilitar a utilização o wrapper possui os seguintes métodos:

```kotlin
networkResult
    .onSuccess { list: List<WiFiData>? ->  
        // TODO    
    }
    .onFail { list: List<WiFiData>? ->  
         // TODO 
    }
    .onUndefinedNetwork { list: List<WiFiData>? ->  
        // TODO 
    }    
```

> O segundo parâmetro representa a lista de WiFi utilizados como referência para a observação do dispositivo. Para cada objeto nessa lista é possível obter a porcentagem de presença. 
    
