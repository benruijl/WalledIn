package walledin.engine;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

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

	private Frame win;
	private GLCanvas mCanvas;
	private GL gl;
	private RenderListener mEvListener;
	private GLAutoDrawable mCurDrawable;
	private long prevTime;
	private int frameCount;
	private int mWidth;
	private int mHeight;
	private Camera mCam;
	private boolean isFullScreen;
	private boolean isFirstRun;
	private Animator anim;
	private long lastUpdate;
	private Texture lastTexture;

	public void initialize(final String strTitle) {
		win = new Frame(strTitle);
		win.setSize(800, 600);
		win.setLocation(0, 0);
		win.setLayout(new BorderLayout());

		win.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {

				if (isFullScreen) {
					toggleFullScreen();
				}

				System.exit(0);
			}
		});

		final GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);

		mCanvas = new GLCanvas(caps);
		mCanvas.addGLEventListener(this);
		mCanvas.addKeyListener(Input.getInstance()); // listen to keys

		win.add(mCanvas, BorderLayout.CENTER);
		win.setVisible(true);

		mCanvas.requestFocus();

		isFirstRun = true;

		lastUpdate = System.nanoTime();
	}

	/**
	 * Toggle between windowed mode and full screen mode.
	 * 
	 * FIXME: this function works, but as the window is recreated in dispose(),
	 * so is the GLcanvas. This means the init function is run, so resources are
	 * loaded again.
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

	public void beginLoop() {
		// setup the animator
		anim = new Animator(mCanvas);
		anim.start();

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
	public void display(final GLAutoDrawable glDrawable) {
//		 if (frameCount > 10) {
//		 System.out.println((1000000000.0f * frameCount)
//		 / (System.nanoTime() - prevTime) + " ");
//		 prevTime = System.nanoTime();
//		 frameCount = 0;
//		 } else {
//		 frameCount++;
//		 }

		mCurDrawable = glDrawable;
		gl = mCurDrawable.getGL();

		final long currentTime = System.nanoTime();
		double delta = currentTime - lastUpdate;
		// Delta is in seconds. 10^9 nanoseconds per second
		delta /= 1000 * 1000 * 1000;
		lastUpdate = currentTime;

		if (mEvListener != null) {
			mEvListener.update(delta); // TODO: verify if delta is correct

			beginDraw();
			mEvListener.draw(this); // draw the frame
		}
	}

	/**
	 * Draws a textured rectangle to the screen. The full texture is used, and
	 * the dimesions are kept.
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
		Texture texture = TextureManager.getInstance().get(strTex);
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

	private void bindTexture(Texture texture) {
		if (texture != lastTexture) {
			texture.bind();
			lastTexture = texture;
		}
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
	 * Draws a textured rectangle to the screen. It uses the same dimensions on
	 * the screen as the texture's.
	 * 
	 * @param strTex
	 *            Texure name
	 * @param texRect
	 *            Specifies the subtexture
	 * @param vPos
	 *            Position to render to
	 */
	public void drawRect(final String strTex, final Rectangle texRect,
			final Vector2f vPos) {
		drawRect(strTex, texRect, new Rectangle(vPos.getX(), vPos.getY(),
				texRect.getWidth(), texRect.getHeight()));
	}

	private void beginDraw() {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		/* apply the camera transformations */
		translate(mCam.getPos());
		rotate(mCam.getRot());
		scale(mCam.getScale());
	}

	/**
	 * This function is called on window creation and recreation
	 */
	public void init(final GLAutoDrawable glDrawable) {
		mCurDrawable = glDrawable;
		gl = mCurDrawable.getGL();

		gl.glClearColor(0.52f, 0.8f, 1.0f, 0.0f); // sky blue
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_TEXTURE_2D);

		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);

		mEvListener.init();

		mCam = new Camera();
	}

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
	}

	public void displayChanged(final GLAutoDrawable glad, final boolean bln,
			final boolean bln1) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

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
	 *            Angle in <b>degrees</b>
	 */
	public void rotate(final float rad) {
		gl.glRotatef(rad, 0, 0, 1);

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

	public void centerAround(final Vector2f vec) {
		mCam.setPos(new Vector2f(-vec.x + mWidth * 0.5f, -vec.y + mHeight
				* 0.5f));
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

		final Matrix2f mMat = new Matrix2f(mvmat[0], mvmat[1], mvmat[4],
				mvmat[5]);
		final Vector2f vNew = mMat.apply(rect.getLeftTop());

		return !(mvmat[12] + vNew.getX() > mWidth
				|| mvmat[12] + rect.getRight() < 0
				|| mvmat[13] + vNew.getY() > mHeight || mvmat[13]
				+ rect.getBottom() < 0);
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
		final float fSXZ = mCam.getScale().x;
		final float fSXY = fSXZ * mCam.getScale().y;
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
}
