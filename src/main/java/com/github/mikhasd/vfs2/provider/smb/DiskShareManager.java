package com.github.mikhasd.vfs2.provider.smb;

import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.util.Objects;

public class DiskShareManager {

    private final SessionFactory sessionFactory;
    private final String shareName;
    private DiskShare diskShare;


    public DiskShareManager(SessionFactory sessionFactory, String shareName) {
        this.sessionFactory = sessionFactory;
        this.shareName = shareName;
    }

    public DiskShare getDiskShare() throws SmbProviderException {
        if(!isConnected()){
            Session session = this.sessionFactory.create();
            this.diskShare = (DiskShare) session.connectShare(this.shareName);
        }

        return this.diskShare;
    }

    private boolean isConnected() {
        return Objects.nonNull(this.diskShare) && this.diskShare.isConnected();
    }
}
