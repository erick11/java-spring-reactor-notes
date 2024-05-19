package com.bemainick.java.reactor;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


@SpringBootApplication
public class JavaSpringReactorNotesApplication  implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(JavaSpringReactorNotesApplication.class);

	private static List<String> dishes = new ArrayList<>();


	/* ************************************************************************************
	****************            En el Mundo de la Reacticidad              ****************
	****************     nada sucede, nada pasa hasta que te subscribes     ****************
 	***************************************************************************************
	*/

	public void createMonoSyso(){
		//No indica el hilo donde se ejecuta; por ello, utilizaremos un LOG para que nos indique el hilo donde se ejecuta
		Mono <String> m1 = Mono.just("Hello Coders");
		m1.subscribe( x -> System.out.printf(x));
	}

	public void createMono(){
		//Mono de un SOLO ELEMENTO

		Mono <String> m1 = Mono.just("Hello Coders");
		//Referencia de metodos: Es cuando un parametro al izquierda. Se utiliza en la derecha en solo metodo referenciado
		//m1.subscribe( x -> log.info(x));
		m1.subscribe(log::info);

		Mono.just(33).subscribe(x -> log.info(x.toString()));
	}

	public void createFlux(){
		//Flux de VARIOS ELEMENTOS, tambien tener en cuenta que en flux es elemento por elemeto

		// Se puede almacenar en una variable del mimso tipo
		Flux<String> fx1 = Flux.fromIterable(dishes);
		fx1.subscribe(log::info);

		//Nota: cuando le aplicas "collection" al flux pasa a ser mono; por ello, Seria un flux que su unico elemeto es una lista.
		fx1.collectList().subscribe(list -> log.info("list: " + list));
	}

	public void m1doOnNext(){
		Flux<String> fx1 = Flux.fromIterable(dishes);

		/*
		"doOnNext" Es un metodo que se va desencadenar luego de la ejecucion de un metodo interno llamado "onNext".
		Cada vez que se emite un elemento de un flujo sea Mono o Flux internamente se lanza un "onNext". Asi hasta
		terminar el flujo de datos
		*/
		//Se utiliza para procesos de depuracion o logica complementaria
		fx1.doOnNext(log::info).subscribe();
	}

	public void m2map() {

		/*
		   Cada flujo Es inmutable, pero se crea un nuevo flujo cuando aplicas operadores de flujo;
		*/

		//En el flujo fx1 imprime en mayuscula los elementos de las listas; por que la subscripcion viene
		//despues del metodo del operador map
		Flux<String> fx1 = Flux.fromIterable(dishes);
		fx1.map(String::toUpperCase).subscribe(x -> log.info("flujo fx1: " + x));


		Flux<String> fxA = Flux.fromIterable(dishes);
		Flux<String> fxB = fxA.map(String::toUpperCase);

		//La variable que fxB guarda flujo; por ello, cuando imprime en consulo se visualiza en MAYUSCULAS
		fxB.subscribe(b -> log.info("Flujo fxB: " + b));

		//Por otra parte podemos apreciar que el "flujo fxA" impreme en minuscula
		//; ya que, la subscripcion es del original
		fxA.subscribe(a -> log.info("Flujo fxA: " + a));

	}

	public void m3flatMap() {
		// La primera no hay roche
		Mono.just("jaime").map(x -> 33).subscribe(e -> log.info("Data: " + e));

		// Aqui cambiamos el flujo interno e imprimera MonoJusto; porque, map no tiene la capacidad de interpretar
		// o acceder al flujo interno. Por ello, se utiliza flatMap
		Mono.just("jaime").map(x -> Mono.just(33)).subscribe(e -> log.info("Data: " + e));

		//FlatMap si tiene la capacidad de acceder a los elementos de un flujo
		//Si yo necesito saber el valor interno de un flujo Respuesta utilizo flatMap. Es decir, es como si fuera un winZip,
		//lo descromprime y ya puedo acceder a el. Por eso en la llamada a la base de datos te devuelve un flux de elememtos
		//un mono de elementos
		Mono.just("jaime").flatMap(x -> Mono.just(33) ).subscribe(e -> log.info("Data: " + e));
	}

	public void m4range() {
		Flux<Integer> fx1 = Flux.range(0, 10);
		fx1.subscribe(x -> log.info("Lista uno: " + x.toString()));
		fx1.map(x -> x + 1).subscribe(j -> log.info("Lista dos: " + j.toString()));
 	}

	public void m5DelayElements() throws InterruptedException {
		//Demora un segundo para emprimir el elemento, pero lo hace en otro hilo
		Flux.range(0, 20)
				//Genera un nuevo hilo
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(x -> log.info("N: " + x))
				.subscribe();

		Thread.sleep(22000); //22 segundos

		/*
		Nota: En consola veras [ parallel-1] al [   parallel-8) eso depende la cantidad de nucleos de tu PC
		*/
	}

	public void m6ZipWith() {
	//Empata los elemetos de dos flujos diferente 1 con  1, 2 con 2, etc
	//Empareja elemento de los flujos

		List<String> clients = new ArrayList<>();
		clients.add("cliente 1");
		clients.add("Cliente 2");
		//clients.add("cliente 3");

		Flux<String> fx1 = Flux.fromIterable(clients);
		Flux<String> fx2 = Flux.fromIterable(dishes);

		fx1.zipWith(fx2, (c, d) -> c + " - " +d).subscribe(log::info);

		//Nota: Si falto un elemento para hacer el par. Omitira esa pareja y NO genera una Exceptio
	}

	public void m7Merge() {
		//Junto o fusiona los flujo.
		// - No importa si es Just o Mono
		// - Puedes repetir el flujos

		List<String> clients = new ArrayList<>();
		clients.add("cliente 1");
		clients.add("Cliente 2");
		clients.add("cliente 3");

		Flux<String> fx1 = Flux.fromIterable(clients);
		Flux<String> fx2 = Flux.fromIterable(dishes);
		Mono<String> m1  = Mono.just("mitoCode");

		Flux.merge(fx1,fx2, m1, m1).subscribe(log::info);
	}

	public void m8Filter() {
		Flux<String> fx1 = Flux.fromIterable(dishes);

		// el filter te pido como parametro un predicado (java.util.function.Predicate<? super T> p)

		//forma 1
		Predicate<String> predicate = e -> e.startsWith("Ce");
		fx1.filter(predicate).subscribe(log::info);

		//Forma 2
		fx1.filter(e -> e.startsWith("Ce")).subscribe(log::info);
	}

	public void m9TakeLast() {
		//Recuerda que tomo como una porcion
		//Si te pasa el numero de elementos toma todos los elementos
		//No bota Exception

		Flux<String> fx1 = Flux.fromIterable(dishes);
		fx1.takeLast(1).subscribe(log::info);
	}

	public void m10Take() {
		//Toma una porcion desde el inicio
		Flux<String> fx1 = Flux.fromIterable(dishes);
		fx1.take(1).subscribe(log::info);
	}

	public void m11DefaulIfEmpty() {
	    //DefaulIfEmpty es un operador que solo se va activar cuando es un flujo vacio a eso se lo llama Flux.empty o Mono.empty

		dishes = new ArrayList<>();
		Flux<String> fx1 = Flux.fromIterable(dishes);   //Flux.empty | Mono.empty

		fx1.map(e -> "Dish: " + e)
				.defaultIfEmpty("EMPTY FLUX")
				.subscribe(log::info);
		//Nota: El defaultIfEmpty es lo primero que se ejecuta
	}

	public void m12_1_Error() {
		Flux<String> fx1 = Flux.fromIterable(dishes);

		fx1.doOnNext(e -> {
			 throw new ArithmeticException("BAD OPERATION");
		})
		.onErrorReturn("ERROR, TRY AGAIN") // ==> devuelve un mensaje
		.subscribe(log::info);
	}

	public void m12_2_Error() {
		Flux<String> fx1 = Flux.fromIterable(dishes);

		fx1.doOnNext(e -> {
					throw new ArithmeticException("BAD OPERATION");
				})
				.onErrorMap(e -> new Exception(e.getMessage())) //==> captura la Exception la convierte a java.lang.Exception:
				.subscribe(log::info);
	}


	public static void main(String[] args) {
		SpringApplication.run(JavaSpringReactorNotesApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		dishes.add("Ceviche");
		dishes.add("Bandeja Paisa");
		dishes.add("Tacos al Pastor");

		m12_1_Error();
	}
}
