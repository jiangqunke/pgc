package com.bestv.pgc.beans;

import android.text.TextUtils;


import java.util.ArrayList;

public class SpotBean extends Entity<ArrayList<SpotBean>> {
//    private int[] indexList;
//    private int mediaCount;
//    private ArrayList<SpotMediaBean> mediaList;

//    public int[] getIndexList() {
//        return indexList;
//    }
//
//    public void setIndexList(int[] indexList) {
//        this.indexList = indexList;
//    }

//    public int getMediaCount() {
//        return mediaCount;
//    }
//
//    public void setMediaCount(int mediaCount) {
//        this.mediaCount = mediaCount;
//    }
//
//    public ArrayList<SpotMediaBean> getMediaList() {
//        return mediaList;
//    }
//
//    public void setMediaList(ArrayList<SpotMediaBean> mediaList) {
//        this.mediaList = mediaList;
//    }

    //    public class SpotMediaBean{
    private String titleId;
    private String title;
    private String bgCover;
    private int jumpType;
    private String jumpId;
    private String jumpTitle;
    private String jumpCover;
    private int duration;
    private String qualityName;
    private String qualityUrl;
    private String bandWidth;
    private String id;
    private String subTags;
    private String originalUrl;
    private double playDuration;
    private boolean isPlayFinish;
    private String time;
    private boolean isPause;
    public String endTimestamp;
    public String startTimestamp;
    public boolean isJump;//是否跳转剧集页
    private String ipTitle;
    private String ipCover;
    private String ipId;
    private boolean isPraise;
    private String jumpUrl;
    private String titleAppId;
    private long praiseCount;
    private String createDate;
    private int type;
    private boolean isSelect;
    private String bitrateType;
    private String albumId;
    private String albumName;
    private boolean isAlbumFocus;
    private String downloadQualityUrl;
    private long commentCount;
    private String pgcUserId;
    private String source;
    private String titleRecordNumber;
    private int resolutionHeight;
    private int resolutionWidth;
    private String commentId;
    private String height;
    private String width;
    private int vipTitle;
    private int trySeeTime;
    private int freeCache;
    private boolean isRelatedCard =  true;
    private String cornerMarkName;
    private String cornerMarkId;
    private String rightBgColour;
    private String leftBgColour;
    private String nameColor;
    private long fileSize;
    private String downloadCdnUrl;
    private int screen;
    private boolean isDlnaMode;

    private String topName;
    private String topNameColor;
    private String topleftBgColour;
    private String topRightBgColour;
    private String pgcTaskName;
    private String pgcTaskUrl;
    private boolean isBesTv;
    private String algoInfo;

    public String getAlgoInfo() {
        return algoInfo;
    }

    public void setAlgoInfo(String algoInfo) {
        this.algoInfo = algoInfo;
    }

    public boolean isBesTv() {
        return isBesTv;
    }

    public void setBesTv(boolean besTv) {
        isBesTv = besTv;
    }

    public String getPgcTaskName() {
        return pgcTaskName;
    }

    public void setPgcTaskName(String pgcTaskName) {
        this.pgcTaskName = pgcTaskName;
    }

    public String getPgcTaskUrl() {
        return pgcTaskUrl;
    }

    public void setPgcTaskUrl(String pgcTaskUrl) {
        this.pgcTaskUrl = pgcTaskUrl;
    }

    public String getTopName() {
        return topName;
    }

    public void setTopName(String topName) {
        this.topName = topName;
    }

    public String getTopNameColor() {
        return topNameColor;
    }

    public void setTopNameColor(String topNameColor) {
        this.topNameColor = topNameColor;
    }

    public String getTopleftBgColour() {
        return topleftBgColour;
    }

    public void setTopleftBgColour(String topleftBgColour) {
        this.topleftBgColour = topleftBgColour;
    }

    public String getTopRightBgColour() {
        return topRightBgColour;
    }

    public void setTopRightBgColour(String topRightBgColour) {
        this.topRightBgColour = topRightBgColour;
    }

    public boolean isDlnaMode() {
        return isDlnaMode;
    }

    public void setDlnaMode(boolean dlnaMode) {
        isDlnaMode = dlnaMode;
    }

    public int getScreen() {
        return screen;
    }

    public void setScreen(int screen) {
        this.screen = screen;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDownloadCdnUrl() {
        return downloadCdnUrl;
    }

    public void setDownloadCdnUrl(String downloadCdnUrl) {
        this.downloadCdnUrl = downloadCdnUrl;
    }

    public String getCornerMarkName() {
        return cornerMarkName;
    }

    public void setCornerMarkName(String cornerMarkName) {
        this.cornerMarkName = cornerMarkName;
    }

    public String getCornerMarkId() {
        return cornerMarkId;
    }

    public void setCornerMarkId(String cornerMarkId) {
        this.cornerMarkId = cornerMarkId;
    }

    public String getRightBgColour() {
        return rightBgColour;
    }

    public void setRightBgColour(String rightBgColour) {
        this.rightBgColour = rightBgColour;
    }

    public String getLeftBgColour() {
        return leftBgColour;
    }

    public void setLeftBgColour(String leftBgColour) {
        this.leftBgColour = leftBgColour;
    }

    public String getNameColor() {
        return nameColor;
    }

    public void setNameColor(String nameColor) {
        this.nameColor = nameColor;
    }


    public boolean isRelatedCard() {
        return isRelatedCard;
    }

    public void setRelatedCard(boolean relatedCard) {
        isRelatedCard = relatedCard;
    }

    public int getFreeCache() {
        return freeCache;
    }

    public void setFreeCache(int freeCache) {
        this.freeCache = freeCache;
    }

    public int getVipTitle() {
        return vipTitle;
    }

    public void setVipTitle(int vipTitle) {
        this.vipTitle = vipTitle;
    }

    public int getTrySeeTime() {
        return trySeeTime;
    }

    public void setTrySeeTime(int trySeeTime) {
        this.trySeeTime = trySeeTime;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getTitleRecordNumber() {
        return titleRecordNumber;
    }

    public void setTitleRecordNumber(String titleRecordNumber) {
        this.titleRecordNumber = titleRecordNumber;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    public void setResolutionHeight(int resolutionHeight) {
        this.resolutionHeight = resolutionHeight;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public void setResolutionWidth(int resolutionWidth) {
        this.resolutionWidth = resolutionWidth;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPgcUserId() {
        return pgcUserId;
    }

    public void setPgcUserId(String pgcUserId) {
        this.pgcUserId = pgcUserId;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public String getDownloadQualityUrl() {
        return downloadQualityUrl;
    }

    public void setDownloadQualityUrl(String downloadQualityUrl) {
        this.downloadQualityUrl = downloadQualityUrl;
    }

    public boolean isAlbumFocus() {
        return isAlbumFocus;
    }

    public void setAlbumFocus(boolean albumFocus) {
        isAlbumFocus = albumFocus;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getBitrateType() {
        return bitrateType;
    }

    public void setBitrateType(String bitrateType) {
        this.bitrateType = bitrateType;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    private String shareUrl;

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    private boolean isFocus;

    public String getIpTitle() {
        return ipTitle;
    }

    public void setIpTitle(String ipTitle) {
        this.ipTitle = ipTitle;
    }

    public String getIpCover() {
        return ipCover;
    }

    public void setIpCover(String ipCover) {
        this.ipCover = ipCover;
    }

    public String getIpId() {
        return ipId;
    }

    public void setIpId(String ipId) {
        this.ipId = ipId;
    }

    public boolean isPraise() {
        return isPraise;
    }

    public void setPraise(boolean praise) {
        isPraise = praise;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    public String getTitleAppId() {
        return titleAppId;
    }

    public void setTitleAppId(String titleAppId) {
        this.titleAppId = titleAppId;
    }

    public long getPraiseCount() {
        return praiseCount;
    }

    public void setPraiseCount(long praiseCount) {
        this.praiseCount = praiseCount;
    }

    public boolean isJump() {
        return isJump;
    }

    public void setJump(boolean jump) {
        isJump = jump;
    }

    public String getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isPlayFinish() {
        return isPlayFinish;
    }

    public void setPlayFinish(boolean playFinish) {
        isPlayFinish = playFinish;
    }

    public double getPlayDuration() {
        return playDuration;
    }

    public void setPlayDuration(double playDuration) {
        this.playDuration = playDuration;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getSubTags() {
        return subTags;
    }

    public void setSubTags(String subTags) {
        this.subTags = subTags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getTitleId() {
        return titleId;
    }

    public void setTitleId(String titleId) {
        this.titleId = titleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBgCover() {
        return bgCover;
    }

    public void setBgCover(String bgCover) {
        this.bgCover = bgCover;
    }

    public int getJumpType() {
        return jumpType;
    }

    public void setJumpType(int jumpType) {
        this.jumpType = jumpType;
    }

    public String getJumpId() {
        return jumpId;
    }

    public void setJumpId(String jumpId) {
        this.jumpId = jumpId;
    }

    public String getJumpTitle() {
        return jumpTitle;
    }

    public void setJumpTitle(String jumpTitle) {
        this.jumpTitle = jumpTitle;
    }

    public String getJumpCover() {
        return jumpCover;
    }

    public void setJumpCover(String jumpCover) {
        this.jumpCover = jumpCover;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getQualityName() {
        return qualityName;
    }

    public void setQualityName(String qualityName) {
        this.qualityName = qualityName;
    }

    public String getQualityUrl() {
        return TextUtils.isEmpty(qualityUrl)?"":qualityUrl;
    }

    public void setQualityUrl(String qualityUrl) {
        this.qualityUrl = qualityUrl;
    }

    public String getBandWidth() {
        return bandWidth;
    }

    public void setBandWidth(String bandWidth) {
        this.bandWidth = bandWidth;
    }
//    }
}
