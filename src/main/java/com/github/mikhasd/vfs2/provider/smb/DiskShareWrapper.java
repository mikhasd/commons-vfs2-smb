package com.github.mikhasd.vfs2.provider.smb;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

class DiskShareWrapper {

    private final DiskShareManager diskShareManager;

    public DiskShareWrapper(DiskShareManager diskShareManager) {
        this.diskShareManager = diskShareManager;
    }

    private <A,R> R applyWithDiskShare(BiFunction<DiskShare ,A ,R> diskShareCallback, A argument) throws SmbProviderException {
        DiskShare diskShare = this.diskShareManager.getDiskShare();
        return diskShareCallback.apply(diskShare, argument);
    }

    private <R> R applyWithDiskShare(Function<DiskShare ,R> diskShareCallback) throws SmbProviderException {
        DiskShare diskShare = this.diskShareManager.getDiskShare();
        return diskShareCallback.apply(diskShare);
    }

    private <A> void acceptWithDiskShare(BiConsumer<DiskShare, A> diskShareCallback, A argument) throws SmbProviderException {
        DiskShare diskShare = this.diskShareManager.getDiskShare();
        diskShareCallback.accept(diskShare, argument);
    }

    private void acceptWithDiskShare(Consumer<DiskShare> diskShareCallback) throws SmbProviderException {
        DiskShare diskShare = this.diskShareManager.getDiskShare();
        diskShareCallback.accept(diskShare);
    }

    protected void mkdir(String path) throws SmbProviderException {
        acceptWithDiskShare(DiskShare::mkdir, path);
    }

    protected FileAllInformation getFileInformation(String path) throws SmbProviderException {
        return applyWithDiskShare(DiskShare::getFileInformation, path);
    }

    protected Directory openDirectory(String path,
                                      Set<AccessMask> accessMask,
                                      Set<FileAttributes> attributes,
                                      Set<SMB2ShareAccess> shareAccesses,
                                      SMB2CreateDisposition createDisposition,
                                      Set<SMB2CreateOptions> createOptions) throws SmbProviderException {
        return applyWithDiskShare(share -> share.openDirectory(
                path,
                accessMask,
                attributes,
                shareAccesses,
                createDisposition,
                createOptions
        ));
    }

    protected File openFile(String path,
                            Set<AccessMask> accessMask,
                            Set<FileAttributes> attributes,
                            Set<SMB2ShareAccess> shareAccesses,
                            SMB2CreateDisposition createDisposition,
                            Set<SMB2CreateOptions> createOptions) throws SmbProviderException {
        return applyWithDiskShare(share -> share.openFile(
                path,
                accessMask,
                attributes,
                shareAccesses,
                createDisposition,
                createOptions
        ));
    }

    protected List<FileIdBothDirectoryInformation> list(String path) throws SmbProviderException {
        return applyWithDiskShare(DiskShare::list, path);
    }

    protected void rm(String path) throws SmbProviderException {
        acceptWithDiskShare(DiskShare::rm, path);
    }

    protected void rmdir(String path, boolean recursive) throws SmbProviderException {
        acceptWithDiskShare(share -> share.rmdir(path, recursive));
    }

    protected boolean fileExists(String path) throws SmbProviderException {
        return applyWithDiskShare(DiskShare::fileExists, path);
    }

    protected boolean folderExists(String path) throws SmbProviderException {
        return applyWithDiskShare(DiskShare::folderExists, path);
    }
}
