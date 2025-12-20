package com.joelcode.personalinvestmentportfoliotracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;

@Configuration
@Profile("dev")
public class DevDatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        return new HikariDataSource(config);
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       @Value("${spring.jpa.database-platform}") String dialect,
                                                                       @Value("${spring.jpa.hibernate.ddl-auto:validate}") String ddlAuto) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.joelcode.personalinvestmentportfoliotracker.entities");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.dialect", dialect);
        jpaProps.put("hibernate.hbm2ddl.auto", ddlAuto);
        emf.setJpaProperties(jpaProps);
        return emf;
    }
}
