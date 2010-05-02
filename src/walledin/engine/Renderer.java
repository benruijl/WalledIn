package walledin.engine;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

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

	private GLCanvas mCanvas;
	private GL gl;
	private RenderListener mEvListener;
	private GLAutoDrawable mCurDrawable;
	private long prevTime;
	private int frameCount;
	private int mWidth;
	private int mHeight;
	private Camera mCam;

	public void initialize(final String strTitle) {
		final Frame win = new Frame(strTitle);
		win.setSize(800, 600);
		win.setLocation(0, 0);
		win.setLayout(new BorderLayout());

		win.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
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
	}

	public void beginLoop() {
		// setup the animator
		final Animator anim = new FPSAnimator(mCanvas, 60);
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

		/*
		 * if (frameCount > 10) { System.out.print((1000000000.0f * frameCount)
		 * / (System.nanoTime() - prevTime) + " "); prevTime =
		 * System.nanoTime(); frameCount = 0; } else { frameCount++; }
		 */

		mCurDrawable = glDrawable;
		gl = mCurDrawable.getGL();

		if (mEvListener != null) {
			mEvListener.update(0); // FIXME, use real delta time

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
		TextureManager.getInstance().get(strTex).bind();

		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(0.0f, 0.0f);
		gl.glVertex2f(destRect.left(), destRect.top());
		gl.glTexCoord2f(0.0f, 1.0f);
		gl.glVertex2f(destRect.left(), destRect.bottom());
		gl.glTexCoord2f(1.0f, 1.0f);
		gl.glVertex2f(destRect.right(), destRect.bottom());
		gl.glTexCoord2f(1.0f, 0.0f);
		gl.glVertex2f(destRect.right(), destRect.top());
		gl.glEnd();
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

		final Texture tex = TextureManager.getInstance().get(strTex);
		tex.bind();

		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2f(texRect.left(), texRect.top());
		gl.glVertex2f(destRect.left(), destRect.top());
		gl.glTexCoord2f(texRect.left(), texRect.bottom());
		gl.glVertex2f(destRect.left(), destRect.bottom());
		gl.glTexCoord2f(texRect.right(), texRect.bottom());
		gl.glVertex2f(destRect.right(), destRect.bottom());
		gl.glTexCoord2f(texRect.right(), texRect.top());
		gl.glVertex2f(destRect.right(), destRect.top());
		gl.glEnd();
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
		drawRect(strTex, texRect, new Rectangle(vPos.x(), vPos.y(), texRect
				.width(), texRect.height()));
	}

	private void beginDraw() {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		/* apply the camera transformations */
		translate(mCam.getPos());
		rotate(mCam.getRot());
		scale(mCam.getScale());
	}

	public void init(final GLAutoDrawable glDrawable) {
		mCurDrawable = glDrawable;
		gl = mCurDrawable.getGL();

		gl.glClearColor(0.52f, 0.8f, 1.0f, 0.0f); // sky blue background color
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_TEXTURE_2D);

		mEvListener.init();

		mCam = new Camera();
	}

	public void reshape(final GLAutoDrawable glad, final int i, final int i1,
			final int i2, final int i3) {
		mWidth = i2;
		mHeight = i3;

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, i2, i3, 0, -1, 1);

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
		gl.glTranslatef(vec.x(), vec.y(), 0);
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
		gl.glScalef(vec.x(), vec.y(), 1);

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
		final Vector2f vNew = mMat.apply(rect.leftTop());

		return !(mvmat[12] + vNew.x() > mWidth || mvmat[12] + rect.right() < 0
				|| mvmat[13] + vNew.y() > mHeight || mvmat[13] + rect.bottom() < 0);
	}

	/**
	 * Converts screen coordinates to world coordinates. Useful for picking with
	 * the mouse. This function assumes that the view matrix is orthogonal.
	 * 
	 * @param p
	 *            Screen position in pixels
	 * @return Returns world position
	 */
	public Vector2f screenToWorld(Vector2i p) {
		Vector2f fp = new Vector2f(p.x, p.y);

		// Create a rotation matrix, calculate its inverse and apply the camera
		// translation
		final Matrix2f invRot = new Matrix2f(mCam.getRot()).transpose();
		final Vector2f vRes = invRot.apply(fp.sub(mCam.getPos()));
		float fSXZ = mCam.getScale().x;
		float fSXY = fSXZ * mCam.getScale().y;
		float fSYZ = 1.0f;
		float fInvDet = 1.0f / (fSXY * fSXZ);

		vRes.x *= fInvDet * fSYZ;
		vRes.y *= fInvDet * fSXZ;

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
