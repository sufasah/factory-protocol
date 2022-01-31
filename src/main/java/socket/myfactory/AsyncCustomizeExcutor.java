package socket.myfactory;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncCustomizeExcutor extends AsyncConfigurerSupport{

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor e = new ThreadPoolTaskExecutor();
		e.setCorePoolSize(1);
		e.setMaxPoolSize(5);
		e.setQueueCapacity(5);
		e.setThreadNamePrefix("DefaultTask");
		e.initialize();
		return e;
	}
}
