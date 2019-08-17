package com.github.mikhasd.vfs2.provider.smb;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericFileName;

import java.util.Objects;

public class SmbFileName extends GenericFileName {

    static final int DEFAULT_PORT = 136;
    private final String domain;
    private final String share;

    SmbFileName(String scheme, String hostName, int port, String userName, String password, String path, FileType type, String domain, String share) {
        super(scheme, hostName, port, DEFAULT_PORT, userName, password, path, type);
        this.domain = domain;
        this.share = share;
    }

    public String getDomain() {
        return domain;
    }

    public String getShare() {
        return share;
    }

    @Override
    protected void appendRootUri(StringBuilder buffer, boolean addPassword) {
        super.appendRootUri(buffer, addPassword);
        buffer.append('/').append(this.share);
    }

    @Override
    protected void appendCredentials(StringBuilder buffer, boolean addPassword) {
        if (!isEmpty(this.domain) && !isEmpty(this.getUserName())) {
            buffer.append(domain).append('\\');
        }
        super.appendCredentials(buffer, addPassword);
    }

    private static boolean isEmpty(String string) {
        return Objects.isNull(string) || string.length() == 0;
    }

    @Override
    public FileName createName(String path, FileType type) {
        return new SmbFileName(
                getScheme(),
                getHostName(),
                getPort(),
                getUserName(),
                getPassword(),
                path,
                type,
                domain,
                share
        );
    }

    @Override
    public String toString() {
        return this.getFriendlyURI();
    }
}
