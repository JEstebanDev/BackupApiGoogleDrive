package soiant.ProjectDailyBackUp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectDailyBackUpApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProjectDailyBackUpApplication.class, args);
		FileManager fm=new FileManager();
		System.out.println("La tarea fue un: "+fm.uploadFile()); 
	}
}
