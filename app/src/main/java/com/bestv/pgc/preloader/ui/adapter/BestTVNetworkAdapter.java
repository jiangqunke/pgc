package com.bestv.pgc.preloader.ui.adapter;

public class BestTVNetworkAdapter implements INetworkAdapter{

    private final boolean canPreloadIfNotWifi;

    public BestTVNetworkAdapter(boolean canPreloadIfNotWifi) {
        this.canPreloadIfNotWifi = canPreloadIfNotWifi;
    }

    @Override
    public boolean canPreLoadIfNotWifi() {
        return canPreloadIfNotWifi;
    }
}
