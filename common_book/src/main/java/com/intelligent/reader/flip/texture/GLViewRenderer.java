package com.intelligent.reader.flip.texture;

public interface GLViewRenderer {

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();
}