package com.example.danmarkskort;

import javafx.scene.paint.Color;
import java.io.Serializable;

public class SerializeableColor implements Serializable {
    int r, g, b, opacity;

    public SerializeableColor(int r, int g, int b, int opacity) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.opacity = opacity;
    }

    public Color toColor() { return new Color(r, g, b, opacity); }
}
