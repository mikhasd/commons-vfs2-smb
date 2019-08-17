package com.github.mikhasd.vfs2.provider.smb;

import com.hierynomus.smbj.share.File;

import java.io.IOException;
import java.io.InputStream;

public class SmbInputStream extends InputStream {
    private final File file;
    private final InputStream inputStream;

    public SmbInputStream(File file) {
        this.file = file;
        this.inputStream = file.getInputStream();
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        file.close();
    }
}
