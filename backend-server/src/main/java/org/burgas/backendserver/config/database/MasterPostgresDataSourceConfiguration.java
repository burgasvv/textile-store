package org.burgas.backendserver.config.database;

import jakarta.persistence.EntityManager;
import liquibase.integration.spring.SpringLiquibase;
import org.burgas.backendserver.repository.MasterRepository;
import org.burgas.backendserver.repository.ReplicaRepository;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.Executors;

@Configuration
@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)
@EnableJpaRepositories(
        basePackages = "org.burgas.backendserver.repository",
        includeFilters = @ComponentScan.Filter(value = MasterRepository.class),
        excludeFilters = @ComponentScan.Filter(value = ReplicaRepository.class),
        entityManagerFactoryRef = "masterPostgresEntityManager",
        transactionManagerRef = "masterPostgresTransactionManager"
)
public class MasterPostgresDataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "master.datasource")
    public DataSourceProperties masterPostgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource masterPostgresDataSource() {
        return this.masterPostgresDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean masterPostgresEntityManager() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        hibernateJpaVendorAdapter.setDatabase(Database.POSTGRESQL);
        hibernateJpaVendorAdapter.setGenerateDdl(false);
        hibernateJpaVendorAdapter.setShowSql(true);

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(this.masterPostgresDataSource());
        entityManagerFactoryBean.setEntityManagerInterface(EntityManager.class);
        entityManagerFactoryBean.setBootstrapExecutor(new TaskExecutorAdapter(Executors.newCachedThreadPool()));
        entityManagerFactoryBean.setJpaDialect(new HibernateJpaDialect());
        entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
        entityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        entityManagerFactoryBean.setPackagesToScan("org.burgas.backendserver.entity");
        entityManagerFactoryBean.setJpaPropertyMap(
                Map.of(
                        "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                        "hibernate.hbm2ddl.auto", "none",
                        "hibernate.show_sql","true",
                        "hibernate.format_sql", "true",
                        "hibernate.highlight_sql", "true"
                )
        );
        return entityManagerFactoryBean;
    }

    @Bean
    public JpaTransactionManager masterPostgresTransactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setRollbackOnCommitFailure(true);
        jpaTransactionManager.setNestedTransactionAllowed(true);
        jpaTransactionManager.setValidateExistingTransaction(true);
        jpaTransactionManager.setFailEarlyOnGlobalRollbackOnly(true);
        jpaTransactionManager.setGlobalRollbackOnParticipationFailure(true);
        jpaTransactionManager.setJpaDialect(new HibernateJpaDialect());
        jpaTransactionManager.setDataSource(this.masterPostgresDataSource());
        jpaTransactionManager.setEntityManagerFactory(this.masterPostgresEntityManager().getObject());
        return jpaTransactionManager;
    }

    @Bean
    public SpringLiquibase masterPostgresSpringLiquibase() {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(this.masterPostgresDataSource());
        springLiquibase.setChangeLog("db/changelog/db.changelog-master.yaml");
        return springLiquibase;
    }
}
