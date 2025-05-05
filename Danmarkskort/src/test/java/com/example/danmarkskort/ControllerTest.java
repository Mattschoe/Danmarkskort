package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {
    //...

    @Test
    protected void controllerTest() {
        Controller controller = new Controller();
        assertNotNull(controller);
    }
}
