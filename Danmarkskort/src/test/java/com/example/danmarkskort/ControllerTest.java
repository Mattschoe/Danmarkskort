package com.example.danmarkskort;

import com.example.danmarkskort.MVC.Controller;
import javafx.application.Application;
import org.testfx.framework.junit5.ApplicationTest;
import com.example.danmarkskort.MVC.View;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest extends ApplicationTest{
    //...
    View view;
    Stage stage;


    @Override
    public void start(Stage primaryStage) throws Exception {
            this.stage = primaryStage;
            try {
                view = new View(primaryStage, "newStart.fxml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    @Test
    protected void createControllerTest() {
       try{ assertNotNull(view.getFXMLLoader().getController());}
       catch (Exception e){fail("Failed to create controller: " + e.getMessage());}
    }


    @Test
    protected void paletteTest(){

    }

}
