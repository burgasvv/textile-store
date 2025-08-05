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
        includeFilters = @ComponentScan.Filter(value = ReplicaRepository.class),
        excludeFilters = @ComponentScan.Filter(value = MasterRepository.class),
        entityManagerFactoryRef = "replicaPostgresEntityManager",
        transactionManagerRef = "replicaPostgresTransactionManager"
)
public class ReplicaPostgresDataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "replica.datasource")
    public DataSourceProperties replicaPostgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource replicaPostgresDataSource() {
        return this.replicaPostgresDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean replicaPostgresEntityManager() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.POSTGRESQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        hibernateJpaVendorAdapter.setGenerateDdl(false);
        hibernateJpaVendorAdapter.setShowSql(true);

        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(this.replicaPostgresDataSource());
        entityManagerFactoryBean.setPackagesToScan("org.burgas.backendserver.entity");
        entityManagerFactoryBean.setEntityManagerInterface(EntityManager.class);
        entityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
        entityManagerFactoryBean.setJpaDialect(new HibernateJpaDialect());
        entityManagerFactoryBean.setBootstrapExecutor(new TaskExecutorAdapter(Executors.newCachedThreadPool()));
        entityManagerFactoryBean.setJpaPropertyMap(
                Map.of(
                        "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                        "hibernate.hbm2ddl.auto", "none",
                        "hibernate.show_sql", "true",
                        "hibernate.format_sql", "true",
                        "hibernate.highlight_sql", "true"
                )
        );
        return entityManagerFactoryBean;
    }

    @Bean
    public JpaTransactionManager replicaPostgresTransactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(this.replicaPostgresEntityManager().getObject());
        jpaTransactionManager.setDataSource(this.replicaPostgresDataSource());
        jpaTransactionManager.setJpaDialect(new HibernateJpaDialect());
        jpaTransactionManager.setValidateExistingTransaction(true);
        jpaTransactionManager.setNestedTransactionAllowed(true);
        jpaTransactionManager.setFailEarlyOnGlobalRollbackOnly(true);
        jpaTransactionManager.setGlobalRollbackOnParticipationFailure(true);
        jpaTransactionManager.setRollbackOnCommitFailure(true);
        return jpaTransactionManager;
    }

    @Bean
    public SpringLiquibase replicaPostgresSpringLiquibase() {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(this.replicaPostgresDataSource());
        springLiquibase.setChangeLog("db/changelog/db.changelog-master.yaml");
        return springLiquibase;
    }
}
