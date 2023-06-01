package com.bestv.pgc.preloader.ui;

import android.content.Context;

import com.bestv.pgc.preloader.videocache.SourceInfo;
import com.bestv.pgc.preloader.videocache.file.FileNameGenerator;
import com.bestv.pgc.preloader.videocache.sourcestorage.SourceInfoStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BestTVSourceInfoStorage implements SourceInfoStorage {

    private static final Object sInfoFileLock = new Object();

    private final File mCacheRoot;

    private final FileNameGenerator mFileNameGenerator;

    public BestTVSourceInfoStorage(File cacheRoot, FileNameGenerator fileNameGenerator) {
        this.mCacheRoot = cacheRoot;
        this.mFileNameGenerator = fileNameGenerator;
    }

    public SourceInfo get(String url) {
        String name = mFileNameGenerator.generate(url) + ".info";
        File file = new File(mCacheRoot, name);
        if (!file.exists()) {
            return null;
        }
        ObjectInputStream fis = null;
        try {
            synchronized (sInfoFileLock) {
                fis = new ObjectInputStream(new FileInputStream(file));
                SourceInfo sourceInfo = (SourceInfo) fis.readObject();
                return sourceInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {

                }
            }
        }
        return null;
    }

    public void put(String url, SourceInfo sourceInfo) {
        String name = mFileNameGenerator.generate(url) + ".info";
        File file = new File(mCacheRoot, name);
        ObjectOutputStream fos = null;
        try {
            synchronized (sInfoFileLock) {
                fos = new ObjectOutputStream(new FileOutputStream(file));
                fos.writeObject(sourceInfo);
                fos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {

                }
            }
        }
    }

    public void release() {

    }
}
