package com.github.mikhasd.vfs2.provider.smb;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

import static org.apache.commons.vfs2.Capability.*;
import static org.apache.commons.vfs2.UserAuthenticationData.*;

public class SmbFileProvider extends AbstractOriginatingFileProvider {

    static final Collection<Capability> CAPABILITIES = EnumSet.of(
            CREATE,
            DELETE,
            RENAME,
            GET_TYPE,
            LIST_CHILDREN,
            READ_CONTENT,
            GET_LAST_MODIFIED,
            URI,
            WRITE_CONTENT,
            APPEND_CONTENT
    );

    static final Type[] AUTHENTICATION_DATA_TYPES = {
            USERNAME,
            PASSWORD,
            DOMAIN
    };

    public SmbFileProvider() {
        this.setFileNameParser(new SmbFileNameParser());
    }

    @Override
    protected FileSystem doCreateFileSystem(FileName rootName, FileSystemOptions fileSystemOptions) throws FileSystemException {
        final SmbFileName smbRootName = (SmbFileName) rootName;
        final DiskShareWrapper diskShareWrapper = createDiskShareConnection(smbRootName, fileSystemOptions);
        final SmbTemplate smbTemplate = new SmbTemplate(diskShareWrapper);
        return new SmbFileSystem(rootName, fileSystemOptions, smbTemplate);
    }

    private DiskShareWrapper createDiskShareConnection(SmbFileName smbRootName, FileSystemOptions fileSystemOptions) {

        final String hostname = smbRootName.getHostName();
        final String share = smbRootName.getShare();

        final String username;
        final String password;
        final String domain;

        Optional<UserAuthenticationData> authenticationData = getAuthenticationData(fileSystemOptions);

        if (authenticationData.isPresent()) {
            UserAuthenticationData data = authenticationData.get();
            username = new String(data.getData(USERNAME));
            password = new String(data.getData(PASSWORD));
            domain = new String(data.getData(DOMAIN));
        } else {
            username = smbRootName.getUserName();
            password = smbRootName.getPassword();
            domain = smbRootName.getDomain();
        }


        final SessionFactory sessionFactory = new SessionFactory(hostname, domain, username, password);
        final DiskShareManager diskShareManager = new DiskShareManager(sessionFactory, share);
        return new DiskShareWrapper(diskShareManager);
    }

    private Optional<UserAuthenticationData> getAuthenticationData(FileSystemOptions fileSystemOptions) {
        UserAuthenticationData userAuthenticationData = UserAuthenticatorUtils.authenticate(fileSystemOptions, AUTHENTICATION_DATA_TYPES);
        return Optional.ofNullable(userAuthenticationData);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return null;
    }
}
