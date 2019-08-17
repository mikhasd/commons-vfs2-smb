package com.github.mikhasd.vfs2.provider.smb;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericURLFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

import java.util.Objects;

public class SmbFileNameParser extends GenericURLFileNameParser {

    public SmbFileNameParser() {
        super(SmbFileName.DEFAULT_PORT);
    }

    @Override
    public FileName parseUri(VfsComponentContext context, FileName base, String fileName) throws FileSystemException {
        StringBuilder builder = new StringBuilder(128);

        final Authority auth = extractToPath(fileName, builder);
        final String usernameAndDomain = auth.getUserName();
        final String domain = extractDomain(usernameAndDomain);
        final String username = Objects.isNull(domain) ? usernameAndDomain : usernameAndDomain.substring(domain.length() + 1);

        UriParser.canonicalizePath(builder, 0, builder.length(), this);
        UriParser.fixSeparators(builder);

        final String share = UriParser.extractFirstElement(builder);
        if(Objects.isNull(share) || share.trim().isEmpty()){
            throw SmbProviderException.missingShareName(fileName);
        }

        final FileType fileType = UriParser.normalisePath(builder);
        final String path = builder.toString();

        return new SmbFileName(
                auth.getScheme(),
                auth.getHostName(),
                auth.getPort(),
                username,
                auth.getPassword(),
                path,
                fileType,
                domain,
                share
        );
    }

    @Override
    protected String extractUserInfo(StringBuilder name) {
        final int atSign = name.lastIndexOf("@");
        if(atSign < 0){
            return null;
        }
        final String userInfo = name.substring(0, atSign);
        name.delete(0, atSign +1);
        return userInfo;
    }

    private String extractDomain(String username) {
        final int backslash;
        if(Objects.isNull(username) || (backslash = username.indexOf('\\')) <= 0){
            return null;
        } else {
            return username.substring(0, backslash);
        }
    }
}
