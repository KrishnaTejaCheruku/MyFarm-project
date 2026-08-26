package in.myfarm.worker;

import org.springframework.boot.SpringApplication;

public class TestMyfarmWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.from(MyfarmWorkerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
