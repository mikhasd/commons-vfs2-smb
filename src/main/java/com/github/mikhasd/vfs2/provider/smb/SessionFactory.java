package com.github.mikhasd.vfs2.provider.smb;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;

import java.io.IOException;
import java.util.Objects;

public class SessionFactory {

    private final static SmbConfig CONFIG = SmbConfig.builder()
            .withDfsEnabled(true)
            .withMultiProtocolNegotiate(true)
            .build();

    private final String hostname;
    private final AuthenticationContext authenticationContext;
    private final SMBClient smbClient;

    public SessionFactory(String hostname, String domain, String username, String password) {
        this.hostname = hostname;
        this.authenticationContext = createAuthenticationContext(domain, username, password);
        this.smbClient = new SMBClient(CONFIG);
    }

    private AuthenticationContext createAuthenticationContext(String domain, String username, String password) {
        if(Objects.isNull(username) || username.trim().isEmpty())
            return AuthenticationContext.anonymous();
        return new AuthenticationContext(username, password.toCharArray(), domain);
    }

    public Session create() throws SmbProviderException {
        try {
            Connection connection = this.smbClient.connect(this.hostname);
            return connection.authenticate(this.authenticationContext);
        } catch (IOException e) {
            throw SmbProviderException.connectionError(hostname, e);
        }
    }
}
