package com.bestv.pgc.preloader.tool.proxy;


import com.bestv.pgc.preloader.tool.socket.SocketProcessTask;
import com.bestv.pgc.preloader.tool.utils.LogUtils;
import com.bestv.pgc.preloader.tool.utils.ProxyCacheUtils;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jeffmony
 * 本地代理服务端类
 */

public class LocalProxyVideoServer {

    private static final String TAG = "LocalProxyCacheServer";

    private final ExecutorService mSocketPool = Executors.newFixedThreadPool(8);

    private ServerSocket mLocalServer;
    private Thread mRequestThread;
    private int mPort;

    public LocalProxyVideoServer() {
        try {
            InetAddress address = InetAddress.getByName(ProxyCacheUtils.LOCAL_PROXY_HOST);
            mLocalServer = new ServerSocket(0, 8, address);
            mPort = mLocalServer.getLocalPort();
            ProxyCacheUtils.getConfig().setPort(mPort);
            ProxyCacheUtils.setLocalPort(mPort);
            CountDownLatch startSignal = new CountDownLatch(1);
            WaitSocketRequestsTask task = new WaitSocketRequestsTask(startSignal);
            mRequestThread = new Thread(task);
            mRequestThread.setName("LocalProxyServerThread");
            mRequestThread.start();
            startSignal.await();
        } catch (Exception e) {
            shutdown();
            LogUtils.w(TAG,"Cannot create serverSocket, exception=" + e);
        }
        LogUtils.d(TAG, "LocalProxyVideoServer port = " + mPort);
    }

    private class WaitSocketRequestsTask implements Runnable {

        private CountDownLatch mLatch;

        public WaitSocketRequestsTask(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public void run() {
            mLatch.countDown();
            initSocketProcessor();
        }
    }

    private void initSocketProcessor() {
        LogUtils.i(TAG, "initSocketProcessor begin...");
        do {
            try {
                LogUtils.d(TAG, "---> begin local server accept socket");
                Socket socket = mLocalServer.accept();
                LogUtils.d(TAG, "<--- local server accept socket: " + socket);
                if (ProxyCacheUtils.getConfig().getConnTimeOut() > 0)
                    socket.setSoTimeout(ProxyCacheUtils.getConfig().getConnTimeOut());
                LogUtils.d(TAG, "submit socket:" + socket);
                mSocketPool.submit(new SocketProcessTask(socket));
                LogUtils.d(TAG, "submit socket:" + socket + "finish");
            } catch (Exception e) {
                LogUtils.w(TAG, "WaitRequestsRun ServerSocket accept failed, exception=" + e);
            }
        } while (!mLocalServer.isClosed());
        LogUtils.i(TAG, "initSocketProcessor complete...");
    }

    private void shutdown() {
        if (mLocalServer != null) {
            try {
                mLocalServer.close();
            } catch (Exception e) {
                LogUtils.w(TAG,"ServerSocket close failed, exception=" + e);
            } finally {
                mSocketPool.shutdown();
                if (mRequestThread != null && mRequestThread.isAlive()) {
                    mRequestThread.interrupt();
                }
            }
        }
    }
}
