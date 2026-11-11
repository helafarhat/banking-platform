package com.albaraka.banking_backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.albaraka.banking_backend.repository.banque",
    entityManagerFactoryRef = "banqueEntityManagerFactory",
    transactionManagerRef = "banqueTransactionManager"
)
public class BanqueDataSourceConfig {

    @Bean(name = "banqueDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.banque")
    public DataSource banqueDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "banqueEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean banqueEntityManagerFactory(
            @Qualifier("banqueDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.albaraka.banking_backend.entity.banque");
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(adapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", "true");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "banqueTransactionManager")
    public PlatformTransactionManager banqueTransactionManager(
            @Qualifier("banqueEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(factory.getObject());
    }
}