/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.engine;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import org.apache.log4j.Logger;

import walledin.engine.math.Matrix2f;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.engine.math.Vector2i;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.texture.Texture;

/**
 * Renderer class. Takes care of rendering, window creation, context creation
 * and update and draw dispatching
 * 
 * @author Ben Ruijl
 */
public class Renderer implements GLEventListener {
    private static final Logger LOG = Logger.getLogger(Renderer.class);

    private Frame win;
    private boolean quitting;
    private GLCanvas mCanvas;
    private GL gl;
    private RenderListener mEvListener;
    private GLAutoDrawable mCurDrawable;
    private int mWidth;
    private int mHeight;
    private Camera mCam;
    private boolean isFullScreen;
    private Animator anim;
    private long lastUpdate;
    private Texture lastTexture;

    /* HUD settings */
    private final int hudWidth = 800; // FIXME: adjust to aspect ratio
    private final int hudHeight = 600;

    /* FPS counting */
    private long prevTime;
    private int frameCount;
    private float curFPS;

    /**
     * Initializes the renderer. It creates the window and a GL render canvas.
     * 
     * @param strTitle
     *            Window title
     * @param width
     *            Window width
     * @param height
     *            Window height
     * @param fs
     *            Set full screen
     */
    public void initialize(final String strTitle, final int width,
            final int height, final boolean fs) {
        win = new Frame(strTitle);
        win.setSize(width, height);
        win.setLocation(0, 0);
        win.setLayout(new BorderLayout());
        quitting = false;

        final GLCapabilities caps = new GLCapabilities();
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        mCanvas = new GLCanvas(caps);
        mCanvas.setIgnoreRepaint(true);
        mCanvas.addGLEventListener(this);
        win.add(mCanvas);

        final GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (fs) {
            win.setUndecorated(true);

            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(win);
            }
        }

        win.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (gd.getFullScreenWindow() == win) {
                    gd.setFullScreenWindow(null);
                }

                mEvListener.dispose();
            }
        });

        win.setVisible(true);

        mCanvas.requestFocusInWindow();

        mCanvas.addKeyListener(Input.getInstance()); // listen to keys
        mCanvas.addMouseMotionListener(Input.getInstance()); // listen to mouse
        // moves
        mCanvas.addMouseListener(Input.getInstance());
        lastUpdate = -1;
    }

    public void dispose() {
        LOG.info("Disposing window...");
        anim.stop();
        win.dispose();
        quitting = true; // prevent function calls to GL after this
    }

    /**
     * Hides the hardware cursor.
     */
    public final void hideHardwareCursor() {
        final int[] pixels = new int[16 * 16];
        final Image image = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(16, 16, pixels, 0, 16));
        final Cursor transparentCursor = Toolkit.getDefaultToolkit()
                .createCustomCursor(image, new Point(0, 0), "invisibleCursor");

        mCanvas.setCursor(transparentCursor);
    }

    /**
     * Toggle between windowed mode and full screen mode.
     * 
     * FIXME: this function works on some systems. The window is recreated in
     * dispose() and so is the GLcanvas. This means the init function is called
     * again, so resources are loaded again.
     */
    public void toggleFullScreen() {
        final GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (isFullScreen || !gd.isFullScreenSupported()) {
            win.setVisible(false);
            win.dispose();
            win.setUndecorated(false);
            gd.setFullScreenWindow(null);
            win.setVisible(true);
            mCanvas.requestFocus();

            isFullScreen = false;
        } else {
            win.setVisible(false);
            win.dispose();
            win.setUndecorated(true);
            gd.setFullScreenWindow(win);
            win.setVisible(true);
            mCanvas.requestFocus();

            isFullScreen = true;
        }

    }

    /**
     * Begin the render loop by starting an animator. The animator is set to run
     * at 60 FPS.
     */
    public void beginLoop() {
        // setup the animator
        anim = new FPSAnimator(mCanvas, 60);
        anim.start();

    }

    /**
     * Get the current FPS avaraged over 10 frames.
     * 
     * @return Current FPS
     */
    public float getFPS() {
        return curFPS;
    }

    public void setCurFPS(final float curFPS) {
        this.curFPS = curFPS;
    }

    /**
     * This function does not only represent the display callback, but the
     * update callback as well.
     * 
     * This function will call the update and draw event listeners. It also
     * takes care of the framerate.
     * 
     * @param glDrawable
     *            The current GL context
     */
    @Override
    public void display(final GLAutoDrawable glDrawable) {
        mCurDrawable = glDrawable;
        gl = mCurDrawable.getGL();

        /* Update FPS */
        if (frameCount > 10) {
            curFPS = 1000000000.0f * frameCount
                    / (System.nanoTime() - prevTime);
            prevTime = System.nanoTime();
            frameCount = 0;
        } else {
            frameCount++;
        }

        if (lastUpdate == -1) {
            lastUpdate = System.nanoTime();
        }

        final long currentTime = System.nanoTime();
        double delta = currentTime - lastUpdate;
        // Delta is in seconds. 10^9 nanoseconds per second
        delta /= 1000 * 1000 * 1000;
        lastUpdate = currentTime;

        if (mEvListener != null) {
            mEvListener.update(delta); // TODO: verify if delta is correct

            if (!quitting) {
                beginDraw();
                mEvListener.draw(this); // draw the frame
            }
        }

    }

    /**
     * Draws a textured rectangle to the screen. The full texture is used, and
     * the dimensions are kept.
     * 
     * @param strTex
     *            Texture name
     */
    public void drawRect(final String strTex) {
        final Texture tex = TextureManager.getInstance().get(strTex);
        drawRect(strTex, new Rectangle(0, 0, tex.getWidth(), tex.getHeight()));
    }

    /**
     * Draws a textured rectangle to the screen. The full texture is used in the
     * mapping.
     * 
     * @param strTex
     *            Texture name
     * @param destRect
     *            Destination rectangle
     */
    public void drawRect(final String strTex, final Rectangle destRect) {
        final Texture texture = TextureManager.getInstance().get(strTex);
        bindTexture(texture);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex2f(destRect.getLeft(), destRect.getTop());
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex2f(destRect.getLeft(), destRect.getBottom());
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex2f(destRect.getRight(), destRect.getBottom());
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex2f(destRect.getRight(), destRect.getTop());
        gl.glEnd();
    }

    /**
     * Binds a texture if it differs from the previously bound texture.
     * 
     * @param texture
     *            Texture to bind
     */
    private void bindTexture(final Texture texture) {
        if (texture != lastTexture) {
            texture.bind();
            lastTexture = texture;
        }
    }

    /**
     * Draws the outline of a rectangle. Useful for checking and debugging
     * bounding rectangles.
     * 
     * @param rect
     *            Rectangle to draw
     */
    public final void drawRectOutline(final Rectangle rect) {

        gl.glPushAttrib(GL.GL_ENABLE_BIT);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(rect.getLeft(), rect.getTop());
        gl.glVertex2f(rect.getLeft(), rect.getBottom());
        gl.glVertex2f(rect.getRight(), rect.getBottom());
        gl.glVertex2f(rect.getRight(), rect.getTop());
        gl.glEnd();

        gl.glPopAttrib();
    }

    /**
     * Draws a textured rectangle to the screen. Assumes textures are enabled.
     * 
     * @param strTex
     *            Texture name
     * @param texRect
     *            Specifies the subtexture
     * @param destRect
     *            Specifies the destination on the screen. It can be used to
     *            scale and translate the image.
     */
    public void drawRect(final String strTex, final Rectangle texRect,
            final Rectangle destRect) {

        final Texture texture = TextureManager.getInstance().get(strTex);
        drawRect(texture, texRect, destRect);
    }

    /**
     * Draws a textured rectangle to the screen. Assumes textures are enabled.
     * 
     * @param texture
     *            The Texture
     * @param texRect
     *            Specifies the subtexture
     * @param destRect
     *            Specifies the destination on the screen. It can be used to
     *            scale and translate the image.
     */
    public void drawRect(final Texture texture, final Rectangle texRect,
            final Rectangle destRect) {
        bindTexture(texture);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(texRect.getLeft(), texRect.getTop());
        gl.glVertex2f(destRect.getLeft(), destRect.getTop());
        gl.glTexCoord2f(texRect.getLeft(), texRect.getBottom());
        gl.glVertex2f(destRect.getLeft(), destRect.getBottom());
        gl.glTexCoord2f(texRect.getRight(), texRect.getBottom());
        gl.glVertex2f(destRect.getRight(), destRect.getBottom());
        gl.glTexCoord2f(texRect.getRight(), texRect.getTop());
        gl.glVertex2f(destRect.getRight(), destRect.getTop());
        gl.glEnd();
    }

    /**
     * Draws a textured rectangle to the screen. Assumes textures are enabled.
     * 
     * @param texturePartID
     *            The name of the texture part that has to be drawn.
     * @param destination
     *            Specifies the destination on the screen. It can be used to
     *            scale and translate the image.
     */
    public void drawTexturePart(final String texturePartID,
            final Rectangle destination) {
        final TexturePart part = TexturePartManager.getInstance().get(
                texturePartID);
        drawRect(part.getTexture(), part.getRectangle(), destination);
    }

    /**
     * To be called when the rendering of the current frame starts. It clears
     * the buffers, and applies the camera transformations.
     */
    private void beginDraw() {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        /* apply the camera transformations */
        translate(mCam.getPos());
        rotate(mCam.getRot());
        scale(mCam.getScale());
    }

    /**
     * This function is called on window creation and recreation. It sets the
     * basic OpenGL settings and creates the camera.
     */
    @Override
    public void init(final GLAutoDrawable glDrawable) {
        mCurDrawable = glDrawable;
        gl = mCurDrawable.getGL();

        gl.glClearColor(0.52f, 0.8f, 1.0f, 0.0f); // sky blue
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_TEXTURE_2D);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
                GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
                GL.GL_LINEAR);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);

        mEvListener.init();

        mCam = new Camera();
    }

    /**
     * Called when the window is resized. It sets up a new projection matrix
     * from the new window width and height.
     * 
     * @param glDrawable
     *            The new GL context
     * @param x
     *            New x coordinate of window
     * @param y
     *            New y coordinate of window
     * @param width
     *            New width of the window
     * @param height
     *            New height of the window
     */
    @Override
    public void reshape(final GLAutoDrawable glDrawable, final int x,
            final int y, final int width, final int height) {
        mCurDrawable = glDrawable;
        gl = glDrawable.getGL();

        mWidth = width;
        mHeight = height;

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, mWidth, mHeight, 0, -1, 1);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslated(0.375, 0.375, 0.);
    }

    /**
     * This function transforms the current projection and modelview matrix to a
     * convenient one for the rendering of HUDs and GUIs. It saves the state of
     * both matrices. They must be recovered by a call to
     * <code>stopHudRendering</code>.<br/>
     * <br/>
     * The projection will be orthogonal and will have a fixed width and height,
     * defined by hudWidth and hudHeight. <br/>
     * <br/>
     * The modelview matrix will be reset to its identity. <br/>
     * <br/>
     * TODO: make the fixed width and height depend on the aspect ratio
     * 
     * @see stopHudRendering
     */
    public void startHUDRendering() {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix(); // push the current projection matrix
        gl.glLoadIdentity();
        gl.glOrtho(0, hudWidth, hudHeight, 0, -1, 1);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix(); // push the current modelview matrix
        gl.glLoadIdentity();
    }

    /**
     * This function restores the projection and modelview matrices to the point
     * just before the call to <code>startHUDRendering</code>. It is
     * <u>mandatory</u> to call this function after a call to startHudRendering.
     * 
     * @see startHUDRendering
     */
    public void stopHUDRendering() {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
    }

    /**
     * Implementation not required. Do not call.
     */
    @Override
    public void displayChanged(final GLAutoDrawable glad,
            final boolean modeChanged, final boolean deviceChanged) {
    }

    /**
     * Add an event listener for
     * 
     * @param listener
     */
    public void addListener(final RenderListener listener) {
        mEvListener = listener;
    }

    /**
     * Translate the current matrix.
     * 
     * @param vec
     *            Translation vector
     */
    public void translate(final Vector2f vec) {
        gl.glTranslatef(vec.getX(), vec.getY(), 0);
    }

    /**
     * Rotate the current matrix.
     * 
     * @param rad
     *            Angle in <b>radians</b>
     */
    public void rotate(final float rad) {
        gl.glRotatef((float) (rad * 180.0f / Math.PI), 0, 0, 1);

    }

    /**
     * Scale (and mirror) the current matrix.
     * 
     * @param vec
     *            Scale vector. For mirroring, use negative numbers
     */
    public void scale(final Vector2f vec) {
        gl.glScalef(vec.getX(), vec.getY(), 1);

    }

    /**
     * Sets the current color.
     * 
     * @param r
     *            R
     * @param g
     *            G
     * @param b
     *            B
     */
    public void setColorRGB(final float r, final float g, final float b) {
        gl.glColor3f(r, g, b);
    }

    /**
     * Centers the camera around a specific point.
     * 
     * @param vec
     *            The point the camera will center around
     */
    public void centerAround(final Vector2f vec) {
        mCam.setPos(new Vector2f(-vec.getX() + mWidth * 0.5f, -vec.getY()
                + mHeight * 0.5f));
    }

    /**
     * Checks is a rectangle is in the current view frustum.
     * 
     * @param rect
     *            Rectangle to check
     * @return Returns true if the rectangle is fully or partially in the
     *         frustum.
     */
    public boolean inFrustum(final Rectangle rect) {
        final float[] mvmat = new float[16];
        gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, mvmat, 0);

        final Matrix2f mat = new Matrix2f(mvmat[0], mvmat[1], mvmat[4],
                mvmat[5]);
        final Vector2f leftTop = mat.apply(rect.getLeftTop());
        final Vector2f rightBottom = mat.apply(rect.getRightBottom());

        return mvmat[12] + leftTop.getX() < mWidth
                && mvmat[12] + rightBottom.getX() > 0
                && mvmat[13] + leftTop.getY() < mHeight
                && mvmat[13] + rightBottom.getY() > 0;
    }

    /**
     * Converts screen coordinates to world coordinates. Useful for picking with
     * the mouse. This function assumes that the view matrix is orthogonal.
     * 
     * @param p
     *            Screen position in pixels
     * @return Returns world position
     */
    public Vector2f screenToWorld(final Vector2i p) {
        final Vector2f fp = new Vector2f(p.x, p.y);

        // Create a rotation matrix, calculate its inverse and apply the camera
        // translation
        final Matrix2f invRot = new Matrix2f(mCam.getRot()).transpose();
        Vector2f vRes = invRot.apply(fp.sub(mCam.getPos()));
        final float fSXZ = mCam.getScale().getX();
        final float fSXY = fSXZ * mCam.getScale().getY();
        final float fSYZ = 1.0f;
        final float fInvDet = 1.0f / (fSXY * fSXZ);

        vRes = vRes.scale(fInvDet * fSYZ);

        return vRes;
    }

    /**
     * Save the current matrix. Useful if doing transformations.
     */
    public void pushMatrix() {
        gl.glPushMatrix();
    }

    /**
     * Restore the previous matrix.
     */
    public void popMatrix() {
        gl.glPopMatrix();
    }

    /**
     * Resets the current matrix (modelview, projection etc.) to its identity.
     * The identity is a 4 dimensional unit matrix.
     */
    public void loadIdentity() {
        gl.glLoadIdentity();
    }
}
