package cn.mikezhu.mysqlparser;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
@Profile("!test") // 非 test Profile 时激活配置类
public class SchedulingConfig {
}
