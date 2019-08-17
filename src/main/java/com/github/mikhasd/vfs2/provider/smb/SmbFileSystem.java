package com.github.mikhasd.vfs2.provider.smb;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.util.Collection;

public class SmbFileSystem extends AbstractFileSystem {
    private final SmbTemplate smbTemplate;

    public SmbFileSystem(FileName rootName, FileSystemOptions fileSystemOptions, SmbTemplate smbTemplate) {
        super(rootName, null, fileSystemOptions);
        this.smbTemplate = smbTemplate;
    }

    @Override
    protected SmbFileObject createFile(AbstractFileName name) {
        SmbFileName smbFileName = (SmbFileName) name;
        return new SmbFileObject(smbFileName, this, smbTemplate, getRootName());
    }

    @Override
    protected void addCapabilities(Collection<Capability> caps) {
        caps.addAll(SmbFileProvider.CAPABILITIES);
    }

}
