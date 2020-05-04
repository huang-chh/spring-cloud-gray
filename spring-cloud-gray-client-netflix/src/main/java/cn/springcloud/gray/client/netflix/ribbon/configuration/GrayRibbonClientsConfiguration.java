package cn.springcloud.gray.client.netflix.ribbon.configuration;

import cn.springcloud.gray.client.netflix.ribbon.GrayChooserRule;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrayRibbonClientsConfiguration {

    @Bean
    @ConditionalOnProperty(value = "gray.route.ribbon.rule.default-definition", matchIfMissing = true)
    public IRule ribbonRule(
            @Autowired(required = false) IClientConfig config) {
        ZoneAvoidanceRule rule = new GrayChooserRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }

}
