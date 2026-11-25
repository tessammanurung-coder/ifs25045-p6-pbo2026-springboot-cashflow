package org.delcom.ifs25045springbootcashflow.config;

import org.delcom.ifs25045springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25045springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25045springbootcashflow.domain.repository.IFinanceAiService;
import org.delcom.ifs25045springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25045springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25045springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25045springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25045springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25045springbootcashflow.usecase.UserUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void wiresBeans() throws Exception {
        AppConfig config = new AppConfig();
        DataSource dataSource = createDataSource();

        IUserRepository userRepository = config.userRepository(dataSource);
        ITransactionRepository transactionRepository = config.transactionRepository(dataSource);
        FinanceUseCase financeUseCase = config.financeUseCase(transactionRepository);
        AuthUseCase authUseCase = config.authUseCase(userRepository);
        UserUseCase userUseCase = config.userUseCase(userRepository);
        FinancePresenter presenter = config.financePresenter();
        FileStorage fileStorage = config.fileStorage(tempDir.toString());
        IFinanceAiService aiService = config.financeAiService("http://localhost:8080/v1", "test-key", "gpt-4o-mini");
        FinanceAiUseCase financeAiUseCase = config.financeAiUseCase(aiService, transactionRepository);

        assertNotNull(userRepository);
        assertNotNull(financeUseCase);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
        assertNotNull(fileStorage);
        assertNotNull(aiService);
        assertNotNull(financeAiUseCase);
        assertInstanceOf(ITransactionRepository.class, transactionRepository);

        config.seedRootAdmin(authUseCase, "admin", "admin").run(new DefaultApplicationArguments());
        assertTrue(userRepository.findByUsername("admin").orElseThrow().isAdmin());

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);
        config.uploadResourceConfigurer(fileStorage).addResourceHandlers(registry);
        verify(registry).addResourceHandler("/uploads/**");
        verify(registration).addResourceLocations(anyString());
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:appconfig_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
