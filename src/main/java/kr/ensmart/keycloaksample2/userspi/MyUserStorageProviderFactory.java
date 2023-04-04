/**
 * 
 */
package kr.ensmart.keycloaksample2.userspi;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * @author choiyh44
 * @version 1.0
 * @since 2023. 3. 24.
 *
 */
public class MyUserStorageProviderFactory implements UserStorageProviderFactory<MyUserStorageProvider> {

    public static final String PROVIDER_NAME = "MyUserStorageProvider";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MyUserStorageProviderFactory.class);
    protected Properties properties = new Properties();

    protected static final List<ProviderConfigProperty> configMetadata;

    static {
        configMetadata = ProviderConfigurationBuilder.create()
                .property()
                    .name("postgresql")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .label("Postgresql URI")
                    .defaultValue("jdbc:postgresql://localhost:5432/postgres")
                    .helpText("Postgresql URI")
                    .add()
                .property()
                    .name("postgresql-user")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .label("Postgresql user")
                    .defaultValue("postgres")
                    .helpText("Postgresql user")
                    .add()
                .property()
                    .name("postgresql-password")
                    .type(ProviderConfigProperty.PASSWORD)
                    .label("Postgresql password")
                    .defaultValue("admin")
                    .helpText("Postgresql password")
                    .add()
                .property()
                    .name("table")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .label("Users Table").defaultValue("users")
                    .helpText("Table where users are stored")
                    .add()
                .property()
                    .name("usernamecol")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .label("Username Column")
                    .defaultValue("username")
                    .helpText("Column name that holds the usernames")
                    .add()
                .property()
                    .name("passwordcol")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .label("Password Column")
                    .defaultValue("password")
                    .helpText("Column name that holds the passwords")
                    .add()
                .property()
                    .name("hash")
                    .type(ProviderConfigProperty.LIST_TYPE)
                    .label("Hash Algorithm")
                    .defaultValue("SHA1")
                    .options(Arrays.asList("SHA1", "MD5"))
                    .helpText("Algorithm used for hashing")
                    .add()
                .build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        logger.info("getConfigProperties: 00000000000000000");
        return configMetadata;
    }

    @Override
    public String getId() {
        logger.info("getId: 00000000000000000");
        return PROVIDER_NAME;
    }
    
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
            throws ComponentValidationException {
        logger.info("validateConfiguration: 00000000000000000");
        String uri = config.getConfig().getFirst("postgresql");
        String user = config.getConfig().getFirst("postgresql-user");
        String password = config.getConfig().getFirst("postgresql-password");
        logger.info("validateConfiguration 11111111111 {}, {}, {}: ", uri, user, password);
        if (uri == null || StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            logger.info("validateConfiguration: 1111111111111111");
            throw new ComponentValidationException("Postgresql connection URI/user/password not present");
        }
        Connection conn = null;
        try {
            logger.info("validateConfiguration: 222222222222222");
            Class.forName("org.postgresql.Driver");
            logger.info("validateConfiguration: 33333333333333333");
            conn = DriverManager.getConnection(uri, user, password);
            logger.info("validateConfiguration: 4444444444444");
            conn.isValid(1000);
            logger.info("validateConfiguration: 55555555555555555");
        } catch (SQLException ex) {
            // handle any errors
            logger.error("SQLException: " + ex.getMessage());
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
            logger.error("", ex);
            throw new ComponentValidationException(ex.getMessage());
        } catch (Exception e) {
            logger.error("", e);
            throw new ComponentValidationException(e.getMessage());
        }
        logger.info("validateConfiguration 9999999999999");
    }

    @Override
    public void init(Config.Scope config) {
        logger.info("init: 00000000000000000");
        InputStream is = getClass().getClassLoader().getResourceAsStream("users.properties");

        if (is == null) {
            logger.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
            logger.warn("Could not find users.properties in classpath");
        } else {
            try {
                logger.info("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
                properties.load(is);
            } catch (IOException ex) {
                logger.error("Failed to load users.properties file", ex);
            }
        }
    }

    @Override
    public MyUserStorageProvider create(KeycloakSession session, ComponentModel config) {
        logger.info("create: 00000000000000000");
        String uri = config.getConfig().getFirst("postgresql");
        String user = config.getConfig().getFirst("postgresql-user");
        String password = config.getConfig().getFirst("postgresql-password");
        
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            logger.info("create 11111111111 {}, {}, {} ", uri, user, password);
            conn = DriverManager.getConnection(uri, user, password);
        } catch (SQLException ex) {
            // handle any errors
            logger.error("SQLException: " + ex.getMessage());
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
            logger.error("", ex);
            throw new ComponentValidationException(ex.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
        return new MyUserStorageProvider(session, config, conn);
    }

}
