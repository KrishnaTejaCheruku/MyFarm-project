package in.myfarm.api;

import org.springframework.boot.SpringApplication;

public class TestMyfarmApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(MyfarmApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
