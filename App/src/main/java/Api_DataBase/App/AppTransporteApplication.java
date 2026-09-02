package Api_DataBase.App;

import javax.swing.SwingUtilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import telas.TelaLogin;

@SpringBootApplication
@ComponentScan(basePackages = { "telas", "service" }) // Certifique-se de que os pacotes corretos estão sendo escaneados
public class AppTransporteApplication implements CommandLineRunner {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(AppTransporteApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		SwingUtilities.invokeLater(() -> {
			TelaLogin telaLogin = applicationContext.getBean(TelaLogin.class);
			telaLogin.setVisible(true);
		});
	}
}
