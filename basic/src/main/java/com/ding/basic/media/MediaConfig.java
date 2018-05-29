package com.ding.basic.media;

/**
 * @author lijun Lee
 * @desc 广告配置构建
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/21 12:16
 */

public class MediaConfig {

    private String userId;

    private String channelCode;

    private int cityCode;

    private String cityName;

    private float latitude;

    private float longitude;

    private MediaConfig() {
    }

    private MediaConfig(Builder builder) {
        this.userId = builder.userId;
        this.channelCode = builder.channelCode;
        this.cityCode = builder.cityCode;
        this.cityName = builder.cityName;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public int getCityCode() {
        return cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public static class Builder {

        private String userId;

        private String channelCode;

        private int cityCode;

        private String cityName;

        private float latitude;

        private float longitude;

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setChannelCode(String channelCode) {
            this.channelCode = channelCode;
            return this;
        }

        public Builder setCityCode(int cityCode) {
            this.cityCode = cityCode;
            return this;
        }

        public Builder setCityName(String cityName) {
            this.cityName = cityName;
            return this;
        }

        public Builder setLatitude(float latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setLongitude(float longitude) {
            this.longitude = longitude;
            return this;
        }

        public MediaConfig build() {
            return new MediaConfig(this);
        }
    }
}
