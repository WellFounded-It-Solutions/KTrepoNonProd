package se.infomaker.livecontentui.livecontentrecyclerview.image;

public interface ImageUrlBuilder {
    ImageUrlBuilder setWidth(int width);
    ImageUrlBuilder setHeight(int height);
    ImageUrlBuilder setQuality(int quality);
    ImageUrlBuilder setFunction(String function);
    ImageUrlBuilder setImgSrc(String imgSrc);
    ImageUrlBuilder setImageId(String imageId);
    String build();
}
