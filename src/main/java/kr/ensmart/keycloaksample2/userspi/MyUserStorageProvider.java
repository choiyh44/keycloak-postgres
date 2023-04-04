/**
 * 
 */
package kr.ensmart.keycloaksample2.userspi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

/**
 * @author choiyh44
 * @version 1.0
 * @since 2023. 3. 23.
 *
 */
public class MyUserStorageProvider
        implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, CredentialInputUpdater {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MyUserStorageProvider.class);

    protected KeycloakSession session;
    protected ComponentModel config;
    protected Connection conn;
    // map of loaded users in this transaction
    protected Map<String, UserModel> loadedUsers = new HashMap<>();

    public MyUserStorageProvider(KeycloakSession session, ComponentModel config, Connection conn) {
        logger.info("Postgresql MyUserStorageProvider 000000000000000000000000000");
        this.session = session;
        this.config = config;
        this.conn = conn;
    }
    
    @Override
    public boolean supportsCredentialType(String credentialType) {
        logger.info("Postgresql supportsCredentialType 000000000000000000000000000");
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        logger.info("Postgresql isConfiguredFor 000000000000000000000000000");
        String password = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;        
        try {
            String query = "SELECT " + this.config.getConfig().getFirst("usernamecol") + ", "
                    + this.config.getConfig().getFirst("passwordcol") + " FROM "
                    + this.config.getConfig().getFirst("table") + " WHERE "
                    + this.config.getConfig().getFirst("usernamecol") + "=?;";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1,  user.getUsername());
            rs = pstmt.executeQuery();
            String pword = null;
            if (rs.next()) {
                password = rs.getString(this.config.getConfig().getFirst("passwordcol"));
            }
        } catch (SQLException ex) {
            // handle any errors
            logger.error("SQLException: " + ex.getMessage());
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    // Do nothing. Ignore.
                }
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqlEx) {
                    // Do nothing. Ignore.
                }
                pstmt = null;
            }
        }
        
        return credentialType.equals(PasswordCredentialModel.TYPE) && password != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        logger.info("Postgresql isValid 000000000000000000000000000");
        if (!supportsCredentialType(input.getType())) {
            return false;
        }
        String password = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;        
        try {
            String query = "SELECT " + this.config.getConfig().getFirst("usernamecol") + ", "
                    + this.config.getConfig().getFirst("passwordcol") + " FROM "
                    + this.config.getConfig().getFirst("table") + " WHERE "
                    + this.config.getConfig().getFirst("usernamecol") + "=?;";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1,  user.getUsername());
            rs = pstmt.executeQuery();
            String pword = null;
            if (rs.next()) {
                password = rs.getString(this.config.getConfig().getFirst("passwordcol"));
            }
        } catch (SQLException ex) {
            // handle any errors
            logger.error("SQLException: " + ex.getMessage());
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    // Do nothing. Ignore.
                }
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqlEx) {
                    // Do nothing. Ignore.
                }
                pstmt = null;
            }
        }
        
        if (password == null) {
            return false;
        }
        logger.info("Postgresql isValid 1111111111111 {}, {}", password, input.getChallengeResponse());
        return password.equals(input.getChallengeResponse());
    }
    
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        logger.info("Postgresql updateCredential 000000000000000000000000000");
        if (input.getType().equals(PasswordCredentialModel.TYPE)) {
            throw new ReadOnlyException("user is read only for this update");
        }

        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        logger.info("Postgresql disableCredentialType 000000000000000000000000000");
        // Do nothing
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        logger.info("Postgresql getDisableableCredentialTypesStream 000000000000000000000000000");
        return Stream.empty();
    }

    @Override
    public void close() {
        logger.info("Postgresql close 000000000000000000000000000");
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqlEx) {
                logger.error(sqlEx.getMessage());
                // ignore
            }
            conn = null;
        }
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.info("Postgresql getUserByUsername 000000000000000000000000000");
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        UserModel adapter = null;

        adapter = loadedUsers.get(username);
        if (adapter != null) {
            logger.info("getUserByUsername 222222222222 {}", adapter);
            return adapter;
        }
        logger.info("getUserByUsername 3333333333333");

        try {
            String query = "SELECT " + this.config.getConfig().getFirst("usernamecol") + ", "
                    + this.config.getConfig().getFirst("passwordcol") + " FROM "
                    + this.config.getConfig().getFirst("table") + " WHERE "
                    + this.config.getConfig().getFirst("usernamecol") + "=?;";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1,  username);
            rs = pstmt.executeQuery();
            String pword = null;
            if (rs.next()) {
                pword = rs.getString(this.config.getConfig().getFirst("passwordcol"));
            }
            logger.info("getUserByUsername 44444444444444");
            if (pword != null) {
                logger.info("getUserByUsername 55555555555555 {}", adapter);
                adapter = createAdapter(realm, username);
                loadedUsers.put(username, adapter);
            }
        } catch (SQLException ex) {
            // handle any errors
            logger.error("SQLException: " + ex.getMessage());
            logger.error("SQLState: " + ex.getSQLState());
            logger.error("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    // Do nothing. Ignore.
                }
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException sqlEx) {
                    // Do nothing. Ignore.
                }
                pstmt = null;
            }
        }
        return adapter;
    }

    protected UserModel createAdapter(RealmModel realm, String username) {
        logger.info("Postgresql createAdapter 000000000000000000000000000");
//        return new AbstractUserAdapter(session, realm, config) {
//            @Override
//            public String getUsername() {
//                return username;
//            }
//
//            @Override
//            public SubjectCredentialManager credentialManager() {
//                // TODO No information.
//                return null;
//            }
//        };
        DemoUser user = new DemoUser(username);
        return new UserAdapter(session, realm, config, user);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.info("Postgresql getUserById 000000000000000000000000000");
        String externalId = StorageId.externalId(id);
        return this.getUserByUsername(realm, externalId);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.info("Postgresql getUserByEmail 000000000000000000000000000");
        // No email yet.
        return null;
    }

}
