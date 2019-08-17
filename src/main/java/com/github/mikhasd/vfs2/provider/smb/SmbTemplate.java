package com.github.mikhasd.vfs2.provider.smb;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.hierynomus.msdtyp.AccessMask.*;
import static com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_NORMAL;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN_IF;
import static com.hierynomus.mssmb2.SMB2CreateOptions.*;
import static com.hierynomus.mssmb2.SMB2ShareAccess.FILE_SHARE_READ;
import static com.hierynomus.mssmb2.SMB2ShareAccess.FILE_SHARE_WRITE;
import static java.util.EnumSet.of;

public class SmbTemplate {
    private final DiskShareWrapper diskShareWrapper;

    public SmbTemplate(DiskShareWrapper diskShareWrapper) {
        this.diskShareWrapper = diskShareWrapper;
    }

    boolean exists(String path) throws SmbProviderException {
        return this.diskShareWrapper.fileExists(path) || this.diskShareWrapper.folderExists(path);
    }

    FileAllInformation getFileInfo(String path) throws SmbProviderException {
        return diskShareWrapper.getFileInformation(path);
    }

    void createFolder(String path) throws SmbProviderException {
        diskShareWrapper.mkdir(path);
    }

    Directory openFolderForWrite(String path) throws SmbProviderException {
        return this.diskShareWrapper.openDirectory(
                path,
                of(GENERIC_ALL),
                of(FILE_ATTRIBUTE_NORMAL),
                of(FILE_SHARE_READ),
                FILE_OPEN_IF,
                of(FILE_DIRECTORY_FILE)
        );
    }

    private File openDestFileForCopy(String path) throws SmbProviderException {
        return this.diskShareWrapper.openFile(
                path,
                of(GENERIC_READ),
                of(FILE_ATTRIBUTE_NORMAL),
                of(FILE_SHARE_READ),
                FILE_OPEN,
                of(FILE_NON_DIRECTORY_FILE)
        );
    }

    private File openSourceFileForCopy(String path) throws SmbProviderException {
        return this.diskShareWrapper.openFile(
                path,
                of(GENERIC_WRITE),
                of(FILE_ATTRIBUTE_NORMAL),
                of(FILE_SHARE_WRITE),
                FILE_OPEN_IF,
                of(FILE_NON_DIRECTORY_FILE)
        );
    }

    void serverSideCopy(String source, String destination) throws SmbProviderException {
        try (File sourceFile = openSourceFileForCopy(source); File deestFile = openDestFileForCopy(destination)) {
            sourceFile.remoteCopyTo(deestFile);
        } catch (Throwable e) {
            throw SmbProviderException.remoteCopy(source, destination, e);
        }
    }

    Collection<FileIdBothDirectoryInformation> getChildrenInfo(String path) throws SmbProviderException {
        List<FileIdBothDirectoryInformation> infos = diskShareWrapper.list(path);
        List<FileIdBothDirectoryInformation> children = new ArrayList<>(infos.size());
        for (FileIdBothDirectoryInformation child : children) {
            String name = child.getFileName();
            if (name.equals(".") || name.equals("..") || name.equals("./") || name.equals("../")) {
                continue;
            }
            infos.add(child);
        }
        return Collections.unmodifiableCollection(infos);
    }

    void delete(String path) throws SmbProviderException {
        try {
            FileAllInformation info = diskShareWrapper.getFileInformation(path);
            if(info.getStandardInformation().isDirectory()){
                diskShareWrapper.rmdir(path, true);
            } else {
                diskShareWrapper.rm(path);
            }
        } catch (SmbProviderException e) {
            throw e;
        } catch(Exception e) {
            throw SmbProviderException.deleteError(path, e);
        }
    }

    File openFileForWrite(String path) throws SmbProviderException {
        return this.diskShareWrapper.openFile(
                path,
                of(GENERIC_WRITE),
                of(FILE_ATTRIBUTE_NORMAL),
                of(FILE_SHARE_WRITE),
                FILE_OPEN_IF,
                of(FILE_NON_DIRECTORY_FILE, FILE_NO_COMPRESSION)
        );
    }

    File openFileForRead(String path) throws SmbProviderException {
        return this.diskShareWrapper.openFile(
                path,
                of(GENERIC_READ),
                of(FILE_ATTRIBUTE_NORMAL),
                of(FILE_SHARE_WRITE),
                FILE_OPEN,
                of(FILE_NON_DIRECTORY_FILE, FILE_NO_COMPRESSION)
        );
    }
}
