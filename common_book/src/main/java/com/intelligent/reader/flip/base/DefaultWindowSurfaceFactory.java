package com.intelligent.reader.flip.base;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


public class DefaultWindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {


    @Override
    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
        EGLSurface result = null;
        try {
            int[] attribList = {
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_BUFFER_SIZE,256,
                    EGL10.EGL_NONE
            };
            egl.eglCreatePbufferSurface(display,config,attribList);
            result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        } catch (IllegalArgumentException e) {
            // This exception indicates that the surface flinger surface
            // is not valid. This can happen if the surface flinger surface has
            // been torn down, but the application has not yet been
            // notified via SurfaceHolder.Callback.surfaceDestroyed.
            // In theory the application should be notified first,
            // but in practice sometimes it is not. See b/4588890
            Log.e("DefaultWindow", "eglCreateWindowSurface", e);
        }
        return result;
    }

    @Override
    public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
        egl.eglDestroySurface(display, surface);
    }
}