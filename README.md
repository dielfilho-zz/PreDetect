# PreDetect
Uma API simples para desenvolvimento de aplicaçoes indoor.

#### VERSAO 1.1.0:
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
	compile 'danielfilho.ufc.br.com:predetect:1.1.0'
}
```

#### USO BÁSICO:
	
1. passo: Implementar a interface WiFiListener
2. passo: Implementar os metodos "onWifiChange" e "getListenerContext"
3. passo: Obter a intância da classe NetworkManager
3. passo: Registrar a sua Activity para receber os dados das redes Wi-Fi
4. passo: Nos metodos "OnPause", "OnDestroy", utilizar o metodo "unregisterListener" para remover sua activity da lista de listeners.
5. passo: No metodo "OnResume", registre sua activity novamente.

Todos os passos estão representados logo abaixo na classe de exemplo:
	
```java
public class ExampleActivity extends AppCompatActivity implements WiFiListener {	

	private NetworkManager networkManager;

	@Override
    	protected void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	setContentView(R.layout.example_activity);

        	//Obtendo a instancia de NetworkManager
        	this.networkManager = NetworkManager.getInstance();

        	//Registrando sua activity para receber os dados das redes WiFi
			this.networkManager.registerListener(this);
		}

	@Override
	public void onWiFiChange(List<WiFiData> list) {
		//Lista de WiFi proximas.
	}

	@Override
	public Context getListenerContext() {
		return this;
	}

	@Override
	protected void onPause() {
	        manager.unregisterListener(this);
	        super.onPause();
	}

	@Override
	protected void onDestroy() {
	        manager.unregisterListener(this);
	        super.onPause();
	}

	@Override
	protected void onResume() {
	        manager.registerListener(this);
	        super.onResume();
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

   Primeiramente e necessário implementar a interface ```WiFiObserver``` e sobreescrever o metodo ```onWiFiObservingEnds(final int resultCode, final List<WiFiData> list)```. Esse metodo é chamado pela API quando o serviço de observação termina. São retornados um código de resultado e uma lista de WiFiDatas representados por resultCode e list. O ```resultCode``` é uma constante que informa se o serviço foi executado com sucesso ou não. Seu valor pode assumir as seguintes constantes:

```java
if(resultCode == NetworkObserverService.SERVICE_SUCCESS) {
	//TODO   
} else if(resultCode == NetworkObserverService.SERVICE_FAIL) {
        //TODO
} else if(resultCode == NetworkObserverService.SERVICE_NO_WIFI) {
        //TODO
}
```
> O segundo parâmetro representa a lista de WiFi utilizados como referência para a observação do dispositivo. Para cada objeto nessa lista é possível obter a porcentagem de presença. 
    
