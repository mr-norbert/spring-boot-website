package bnorbert.springbootwebsite.service;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ImageService {

    public void resize(String input) throws IOException {
        log.info("Resizing images for our model");
        int targetWidth = 416;
        int targetHeight = 416;

        File inputFile = new File("src/main/resources/build/files/"+input);
        BufferedImage inputImage = ImageIO.read(inputFile);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, inputImage.getType());

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        File outputFile = new File("src/main/resources/build/output/"+input);
        String formatName = input.substring(input.lastIndexOf(".") + 1);
        ImageIO.write(outputImage, formatName, outputFile);
    }

    public void detect(String input) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        int imageSize = 416;
        Pipeline pipeline = new Pipeline();
        pipeline.add(new Resize(imageSize));
        pipeline.add(new ToTensor());

        Translator<Image, DetectedObjects> translator = YoloV5Translator
                .builder()
                .optSynset(Arrays.asList("button", "field", "heading", "iframe", "image", "label", "link", "text"))
                .setPipeline(pipeline)
                .build();

        Image image = ImageFactory
                .getInstance()
                .fromFile(Paths.get("src/main/resources/build/output/"+input));

        Path modelDir = Paths.get("src/main/resources/build/model");

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelPath(modelDir)
                        .optModelName("website.torchscript")
                        .optTranslator(translator)
                        .optEngine("PyTorch")
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<Image, DetectedObjects> model = criteria.loadModel()) {
            try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
                DetectedObjects detection = predictor.predict(image);

                Path outputDir = Paths.get("src/main/resources/build/output");
                Files.createDirectories(outputDir);
                drawBoundingBoxes((BufferedImage) image.getWrappedImage(), detection);
                Path imagePath = outputDir.resolve("detected-object.png");
                image.save(Files.newOutputStream(imagePath), "png");
                log.info("Detected objects image has been saved in: {}", imagePath);
            }
        }
    }

    private Color randomColor() {
        return new Color(RandomUtils.nextInt(255));
    }

    private void drawBoundingBoxes(BufferedImage image, DetectedObjects detections) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        int stroke = 2;
        g.setStroke(new BasicStroke(stroke));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        image.getWidth();
        image.getHeight();

        List<DetectedObjects.DetectedObject> detectedObjects = detections.items();
        for (DetectedObjects.DetectedObject object : detectedObjects) {
            String className = object.getClassName();
            BoundingBox box = object.getBoundingBox();
            g.setPaint(randomColor().darker());

            ai.djl.modality.cv.output.Rectangle rectangle = box.getBounds();
            int x = (int) (rectangle.getX());
            int y = (int) (rectangle.getY());
            g.drawRect(x, y, (int) (rectangle.getWidth()), (int) (rectangle.getHeight()));
            drawText(g, className, x, y, stroke);
        }
        log.info(String.valueOf(detectedObjects.size()));
        g.dispose();
    }

    private void drawText(Graphics2D g, String text, int x, int y, int stroke) {
        FontMetrics metrics = g.getFontMetrics();
        x += stroke / 2;
        y += stroke / 2;
        int width = metrics.stringWidth(text) + 4 * 2 - stroke / 2;
        int height = metrics.getHeight() + metrics.getDescent();
        int ascent = metrics.getAscent();
        Rectangle background = new Rectangle(x, y, width, height);
        g.fill(background);
        g.setPaint(Color.WHITE);
        g.drawString(text, x + 4, y + ascent);
    }


}
