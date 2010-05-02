/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.texture.Texture;

/**
 * 
 * @author ben
 */
public class Renderer implements GLEventListener {

	private GLCanvas mCanvas;
	private GL gl;
	private GLU glu;
	private RenderListener mEvListener;
	private GLAutoDrawable mCurDrawable;
	private static float fx;
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
			endDraw();
		}
	}

	public void drawRect(final String strTex) {
		final Texture tex = TextureManager.getInstance().get(strTex);
		drawRect(strTex, new Rectangle(0, 0, tex.getWidth(), tex.getHeight()));

	}

	public void drawRect(final String strTex, final Rectangle destRect) {
		// gl.glPushAttrib(GL.GL_ENABLE_BIT);
		// TextureManager.getInstance().get(strTex).enable();
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

		// gl.glPopAttrib();
	}

	/**
	 * Draws a textured rectangle to the screen
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

		// check if visible. FIXME: create space partitioning
		/*
		 * if (!inFrustum(destRect)) { return; }
		 */

		// gl.glPushAttrib(GL.GL_ENABLE_BIT);

		final Texture tex = TextureManager.getInstance().get(strTex);
		// tex.enable();
		tex.bind();

		/*
		 * gl.glBegin(GL.GL_QUADS); gl.glTexCoord2f(texRect.left() / (float)
		 * tex.getWidth(), texRect.top() / (float) tex.getHeight());
		 * gl.glVertex2f(destRect.left(), destRect.top());
		 * gl.glTexCoord2f(texRect.left() / (float) tex.getWidth(),
		 * texRect.bottom() / (float) tex.getHeight());
		 * gl.glVertex2f(destRect.left(), destRect.bottom());
		 * gl.glTexCoord2f(texRect.right() / (float) tex.getWidth(),
		 * texRect.bottom() / (float) tex.getHeight());
		 * gl.glVertex2f(destRect.right(), destRect.bottom());
		 * gl.glTexCoord2f(texRect.right() / (float) tex.getWidth(),
		 * texRect.top() / (float) tex.getHeight());
		 * gl.glVertex2f(destRect.right(), destRect.top()); gl.glEnd();
		 */

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

		// gl.glPopAttrib();

	}

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

	private void endDraw() {
		// gl.glFlush(); is done automatically
	}

	public void init(final GLAutoDrawable glDrawable) {
		mCurDrawable = glDrawable;
		gl = mCurDrawable.getGL();
		glu = new GLU();

		gl.glClearColor(1, 1, 1, 0);
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

	// opengl functions
	public void translate(final Vector2f vec) {
		gl.glTranslatef(vec.x(), vec.y(), 0);
	}

	public void rotate(final float rad) {
		gl.glRotatef(rad, 0, 0, 1);

	}

	public void scale(final Vector2f vec) {
		gl.glScalef(vec.x(), vec.y(), 1);

	}

	public void centerAround(final Vector2f vec) {
		mCam.setPos(new Vector2f(-vec.x + mWidth * 0.5f, -vec.y + mHeight
				* 0.5f));
	}

	public boolean inFrustum(final Rectangle rect) {
		// check if position is in viewport
		final float[] mvmat = new float[16];

		gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, mvmat, 0);

		final Matrix2f mMat = new Matrix2f(mvmat[0], mvmat[1], mvmat[4],
				mvmat[5]);
		final Vector2f vNew = mMat.Apply(rect.leftTop());

		return !(mvmat[12] + vNew.x() > mWidth || mvmat[12] + rect.right() < 0
				|| mvmat[13] + vNew.y() > mHeight || mvmat[13] + rect.bottom() < 0);
	}

	public void pushMatrix() {
		gl.glPushMatrix();
	}

	public void popMatrix() {
		gl.glPopMatrix();
	}
}
