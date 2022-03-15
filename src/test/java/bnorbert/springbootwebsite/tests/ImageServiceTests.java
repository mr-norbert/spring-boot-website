package bnorbert.springbootwebsite.tests;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import bnorbert.springbootwebsite.steps.ImageSteps;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ImageServiceTests {


    @Autowired
    private ImageSteps imageSteps;
    ImageSteps service = mock(ImageSteps.class);

    @Test
    public void testDetection() throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        service.detect();
        imageSteps.detect();
        verify(service, times(1)).detect();
    }

    @Test
    public void testResize() throws IOException {
        service.resize();
        imageSteps.resize();
        verify(service, times(1)).resize();
    }
}
