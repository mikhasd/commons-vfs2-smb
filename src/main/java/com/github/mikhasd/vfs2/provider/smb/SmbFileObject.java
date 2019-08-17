package com.github.mikhasd.vfs2.provider.smb;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.File;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.hierynomus.mserref.NtStatus.STATUS_OBJECT_NAME_NOT_FOUND;
import static com.hierynomus.mserref.NtStatus.STATUS_OBJECT_PATH_NOT_FOUND;
import static org.apache.commons.vfs2.FileType.FOLDER;
import static org.apache.commons.vfs2.FileType.IMAGINARY;

public class SmbFileObject extends AbstractFileObject<SmbFileSystem> {

    private final SmbTemplate smbTemplate;
    private final FileName rootName;
    private final String path;
    private Info smbFileInfo;

    public SmbFileObject(SmbFileName fileName, SmbFileSystem fileSystem, SmbTemplate smbTemplate, FileName rootName) {
        super(fileName, fileSystem);
        this.smbTemplate = smbTemplate;
        this.rootName = rootName;
        String relativePath = fileName.getURI().substring(rootName.getURI().length());
        relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        relativePath = relativePath.replace('/', '\\');
        String path;
        try {
            path = UriParser.decode(relativePath);
        } catch (FileSystemException e) {
            path = relativePath;
        }
        this.path = path;
    }

    private Optional<Info> getFileInfo() throws SmbProviderException {
        if (Objects.isNull(this.smbFileInfo)) {
            try {
                FileAllInformation allInfo = smbTemplate.getFileInfo(this.path);
                if (Objects.nonNull(allInfo)) {
                    this.smbFileInfo = Info.from(allInfo);
                }
            } catch (SMBApiException saex) {
                NtStatus status = saex.getStatus();
                if (!STATUS_OBJECT_NAME_NOT_FOUND.equals(status) && !STATUS_OBJECT_PATH_NOT_FOUND.equals(status)) {
                    throw SmbProviderException.fileInformationError(path, saex);
                }
            }
        }
        return Optional.of(this.smbFileInfo);
    }

    @Override
    protected long doGetContentSize() throws Exception {
        return getFileInfo().map(Info::getContentSize).orElse(0L);
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        if (!getType().hasContent()) {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }
        File file = smbTemplate.openFileForRead(path);
        return new SmbInputStream(file);
    }

    @Override
    protected OutputStream doGetOutputStream(final boolean append) throws Exception {
        File file = smbTemplate.openFileForWrite(path);
        return file.getOutputStream(append);
    }

    @Override
    protected FileType doGetType() throws Exception {
        return getFileInfo().map(Info::getFileType).orElse(IMAGINARY);
    }

    @Override
    protected String[] doListChildren() throws Exception {
        return null;
    }

    @Override
    public boolean exists() throws FileSystemException {
        return smbTemplate.exists(path);
    }

    @Override
    public FileObject getParent() {
        SmbFileName parentName = (SmbFileName) getName().getParent();

        if (Objects.isNull(parentName))
            return null;

        SmbFileSystem fileSystem = (SmbFileSystem) getFileSystem();
        FileObject cached = fileSystem.getFileSystemManager().getFilesCache().getFile(fileSystem, parentName);

        if (Objects.nonNull(cached)) {
            return cached;
        } else {
            return new SmbFileObject(parentName, fileSystem, smbTemplate, this.rootName);
        }
    }

    @Override
    protected void doCreateFolder() throws Exception {
        smbTemplate.createFolder(path);
    }

    @Override
    protected void doRename(FileObject newFile) throws Exception {
        final DiskEntry entry;
        if (isFolder()) {
            entry = smbTemplate.openFolderForWrite(path);
        } else {
            entry = smbTemplate.openFileForWrite(path);
        }
        try {
            SmbFileObject fo = (SmbFileObject) newFile;
            entry.rename(fo.path);
        } finally {
            entry.close();
        }
    }

    @Override
    protected void doDelete() throws Exception {
        smbTemplate.delete(path);
    }

    @Override
    public void copyFrom(FileObject file, FileSelector selector) throws FileSystemException {
        if (!file.exists()) {
            throw SmbProviderException.missingSourceFile(file);
        }

        final List<FileObject> files = new ArrayList<>();
        file.findFiles(selector, false, files);

        for (FileObject srcFile : files) {
            final String relativePath = file.getName().getRelativeName(srcFile.getName());
            final FileObject destinationFile = resolveFile(relativePath, NameScope.DESCENDENT_OR_SELF);

            FileType srcFileType = srcFile.getType();
            if (destinationFile.exists() && !destinationFile.getType().equals(srcFileType)) {
                destinationFile.deleteAll();
            }

            try {
                if (srcFileType.hasContent()) {
                    if (srcFile.getFileSystem().equals(destinationFile.getFileSystem())) {
                        performServerSideCopy(srcFile, destinationFile);
                    } else {
                        FileUtil.copyContent(srcFile, destinationFile);
                    }
                } else if (srcFileType.hasChildren()) {
                    destinationFile.createFolder();
                }
            } catch (final IOException ioex) {
                throw new FileSystemException("vfs.provider/copy-file.error", ioex, srcFile, destinationFile);
            }
        }
    }

    private void performServerSideCopy(FileObject source, FileObject destination) throws SmbProviderException {
        SmbFileObject src = (SmbFileObject) source;
        SmbFileObject dest = (SmbFileObject) destination;
        smbTemplate.serverSideCopy(src.path, dest.path);
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return getFileInfo().map(Info::getLastModifiedTime).orElse(0L);
    }

    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        if (!FOLDER.equals(getType())) {

        }
        FileSystemManager fileSystemManager = getFileSystem().getFileSystemManager();
        Collection<FileIdBothDirectoryInformation> childrenInfo = smbTemplate.getChildrenInfo(path);
        List<FileObject> children = new ArrayList<>(childrenInfo.size());
        for (FileIdBothDirectoryInformation info : childrenInfo) {
            String name = UriParser.encode(info.getFileName());
            SmbFileObject fileObject = (SmbFileObject) fileSystemManager.resolveFile(this, name);
            fileObject.initInfo(info);
            children.add(fileObject);
        }

        FileObject[] childrenResolved = new FileObject[children.size()];
        for (int i = 0; i < children.size(); i++) {
            childrenResolved[i] = children.get(i);
        }

        return childrenResolved;
    }

    private void initInfo(FileIdBothDirectoryInformation info) {
        this.smbFileInfo = Info.from(info);
    }

    @Override
    public String toString() {
        return getName().toString();
    }


    @Override
    protected void doDetach() {
        this.smbFileInfo = null;
    }

    private static class Info {
        private final long lastModifiedTime;
        private final boolean directory;
        private final long contentSize;

        private Info(long lastModifiedTime, boolean directory, long contentSize) {
            this.lastModifiedTime = lastModifiedTime;
            this.directory = directory;
            this.contentSize = contentSize;
        }

        private long getContentSize() {
            return contentSize;
        }

        public long getLastModifiedTime() {
            return lastModifiedTime;
        }

        private FileType getFileType() {
            return this.directory ? FileType.FOLDER : FileType.FILE;
        }

        static Info from(FileIdBothDirectoryInformation fdInfo) {
            long lastModifiedTime = fdInfo.getLastWriteTime().toEpochMillis();
            boolean directory = (FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue() * fdInfo.getFileAttributes()) != 0;
            long contentSize = fdInfo.getEndOfFile();
            return new Info(lastModifiedTime, directory, contentSize);
        }

        static Info from(FileAllInformation allInfo) {
            long lastModifiedTime = allInfo.getBasicInformation().getLastWriteTime().toEpochMillis();
            boolean directory = allInfo.getStandardInformation().isDirectory();
            long contentSize = allInfo.getStandardInformation().getEndOfFile();
            return new Info(lastModifiedTime, directory, contentSize);
        }
    }
}
