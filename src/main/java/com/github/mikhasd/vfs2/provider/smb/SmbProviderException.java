package com.github.mikhasd.vfs2.provider.smb;


import com.hierynomus.mssmb2.SMBApiException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;

public class SmbProviderException extends FileSystemException {

    private static final String CONNECTION_ERROR = "vfs.provider.smb/connection.error";
    private static final String MISSING_SHARE_NAME = "vfs.provider.smb/missing-share-name.error";
    private static final String REMOTE_COPY_ERROR = "vfs.provider.smb/remote-copy.error";
    private static final String DELETE_ERROR = "vfs.provider.smb/delete.error";
    private static final String FILE_INFORMATION_ERROR = "vfs.provider.smb/file-information.error";
    private static final String MISSING_SOURCE_FILE = "vfs.provider.smb/missing-source-file.error";

    static SmbProviderException connectionError(String hostname, IOException cause){
        return new SmbProviderException(CONNECTION_ERROR, hostname, cause);
    }

    public SmbProviderException(String code) {
        super(code);
    }

    public SmbProviderException(String code, Object arg) {
        super(code, arg);
    }

    public SmbProviderException(String code, Object arg, Throwable throwable) {
        super(code, arg, throwable);
    }

    public SmbProviderException(String code, Object... args) {
        super(code, args);
    }

    public SmbProviderException(String code, Throwable throwable) {
        super(code, throwable);
    }

    public SmbProviderException(String code, Throwable throwable, Object... args) {
        super(code, throwable, args);
    }

    public SmbProviderException(Throwable throwable) {
        super(throwable);
    }

    static FileSystemException missingShareName(String fileName) {
        return new SmbProviderException(MISSING_SHARE_NAME, fileName);
    }

    static SmbProviderException remoteCopy(String source, String destination, Throwable cause) {
        return new SmbProviderException(REMOTE_COPY_ERROR, cause, source, destination);
    }

    static SmbProviderException deleteError(String path, Exception cause) {
        return new SmbProviderException(DELETE_ERROR, cause, path);
    }

    static SmbProviderException fileInformationError(String path, SMBApiException cause) {
        return new SmbProviderException(FILE_INFORMATION_ERROR, cause, path);
    }

    static FileSystemException missingSourceFile(FileObject file) {
        return new SmbProviderException(MISSING_SOURCE_FILE, file);
    }
}
