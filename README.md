# commons-vfs2-smb

A production tested SMB FileSystemProvider for [Apache commons-vfs2](https://commons.apache.org/proper/commons-vfs/) based on [smbj](https://github.com/hierynomus/smbj).

Introduction
------------

This project implements required commons-vfs2 interfaces to allow interaction with SMB 2/3 using [Jeroen van Erp](https://github.com/hierynomus)'s [smbj](https://github.com/hierynomus/smbj) implementation.

I've created this library for a project that has been running in production for same time, and the features I've implemented were the ones I needed.

In case you are missing some feature, feel free to [file a bug](https://github.com/mikhasd/commons-vfs2-smb/issues/new) or send a Pull Request.


Usage
-----

I'm still working on having a proper CI pipeline setup and the library uploaded to Maven Central. Any help is welcome.

```java
VFS.getManager().resolveFile("smb://DOMAIN\USERNAME:PASSWORD@HOSTNAME:PORT/SHARENAME/PATH");
```
