package com.cmackowiak.allegro;


import com.cmackowiak.allegro.Resolution;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

@Path("/mozaika")
public class MosaicEndpoint {

    @GET
    @Produces(MediaType.IMAGE_JPEG_VALUE)
    public Response getMosaic(@QueryParam("losowo")String random, @DefaultValue("2048x2048") @QueryParam("rozdzielczosc")String resolution, @QueryParam("zdjecia")String images) throws Exception {

boolean isRandom = isRandom(random);
        BufferedImage outputImage;
        try {
            List<URL> inputUrls = getUrlsFromParam(images);
            if (isRandom) {
                Collections.shuffle(inputUrls);
            }
            List<BufferedImage> bufferedImages = readUlrs(inputUrls);
            Resolution outputResolution = resolveResolutionFromParam(resolution);
            outputImage = buildOutputImage(bufferedImages, outputResolution);
        }catch(Exception e){
e.printStackTrace();
return Response.status(400).build();
            }
            return Response.ok(outputImage).build();

        }


    private Boolean isRandom(String random){
        try{
            if(Integer.parseInt(random)==1){
                return true;
            }else{
                return false;
            }

        }catch(Exception e){
            return false;
        }
    }
    private List<URL> getUrlsFromParam(String images)throws Exception{
        String delimiter = ",";
        String[] splitUrls = images.split(delimiter);
        List<URL> urls = new ArrayList<>();
        for(String url: splitUrls){
            urls.add(new URL(url));

        }return urls;


    }
    private List<BufferedImage> readUlrs(List<URL> urls) throws IOException{
        List<BufferedImage> bufferedImages = new ArrayList<>();;
        for(URL url :urls){
            bufferedImages.add(ImageIO.read(url));
        }
        return bufferedImages;
    }
    private Resolution resolveResolutionFromParam(String resolution){
        String delimiter = "x";
        int[] points = Arrays.stream(resolution.split(delimiter)).mapToInt(Integer::parseInt).toArray();
        return new Resolution(points[0],points[1]);


    }
    private BufferedImage buildOutputImage(List<BufferedImage> bufferedImages,Resolution outputResolution){
        int imagesHorizontally = getHorizontallImagesCount(bufferedImages.size());
        int imagesVertically = getVerticalImagesCount(bufferedImages.size(),imagesHorizontally);

        Resolution singleImagesResolution = createSingleImageResoultion(outputResolution,imagesHorizontally,imagesVertically);
        bufferedImages = resizeAll(bufferedImages,singleImagesResolution);
        return mergeImages(bufferedImages,outputResolution,imagesHorizontally);


    }

    private int getHorizontallImagesCount(int imagesCount) {
        if(imagesCount ==1){
            return 1;
        }else{
            double sqrt = Math.sqrt(imagesCount);
            return(int) Math.ceil(sqrt);
        }
    }

    private int getVerticalImagesCount(int imagesCount, int imagesHorizontally) {
        return imagesCount/imagesHorizontally +((imagesCount%imagesHorizontally==0)?0:1);
    }

    private Resolution createSingleImageResoultion(Resolution outputResolution, int imagesHorizontally,int imagesVertically){
        return new Resolution(outputResolution.getWidth()/imagesHorizontally,outputResolution.getHeight()/imagesVertically);
    }

    private List<BufferedImage> resizeAll(List<BufferedImage> bufferedImages, Resolution resolution) {
        List<BufferedImage> resizedImages = new ArrayList<>();
        for (int i = 0; i < bufferedImages.size(); i++) {
            resizedImages.add(resize(bufferedImages.get(i), resolution));
        }
        return resizedImages;
    }

    private BufferedImage resize(BufferedImage bufferedImage, Resolution resolution) {
        BufferedImage outputImage = new BufferedImage(resolution.getWidth(),
                resolution.getHeight(), bufferedImage.getType());
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(bufferedImage, 0, 0, resolution.getWidth(), resolution.getHeight(), null);
        g2d.dispose();
        return outputImage;
    }

    private BufferedImage mergeImages(List<BufferedImage> bufferedImages, Resolution outputResolution, int imagesHorizontally) {
        BufferedImage merged = new BufferedImage(outputResolution.getWidth(),
                outputResolution.getHeight(),bufferedImages.get(0).getType());
        Graphics2D g = (Graphics2D) merged.getGraphics();

        int imageCount = 0;
        int horizontalElement =0;
        int verticalElement =0;
        while(imageCount<bufferedImages.size()){
            BufferedImage bufferedImage = bufferedImages.get(imageCount);
            g.drawImage(bufferedImage,bufferedImage.getWidth()*horizontalElement,bufferedImage.getHeight()*verticalElement,null);

            horizontalElement++;
            imageCount++;
            if(shouldGoToNextLine(imageCount,imagesHorizontally)){
                horizontalElement=0;
                verticalElement++;
            }

        }return merged;
    }
    private boolean shouldGoToNextLine(int imageCount,int imagesHorizontally){
        return imageCount%imagesHorizontally==0;
    }
}
