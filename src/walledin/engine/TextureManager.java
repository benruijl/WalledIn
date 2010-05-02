/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 *
 * @author ben
 */
public class TextureManager extends ResourceManager<String, Texture> {

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static TextureManager getInstance() {
        if (ref == null) {
            ref = new TextureManager();
        }

        return ref;
    }

    private TextureManager() {
        
    }
    private static TextureManager ref = null;

    String generateUniqueID() {
        return "TEX_" + count().toString();
    }

    /*
     * Returns the string ID of the texture. Useful for internal
     * textures
     *
     * @Returns: string ID on succes, null on failure
     */
    public String LoadFromFile(String strFilename) {
        String id = generateUniqueID();

        if (LoadFromFile(strFilename, id))
            return id;

        return null;
    }

    /*
     * Loads a texture from a file and links it with the given ID
     */
    public boolean LoadFromFile(String strFilename, String strTexID) {
        try {
            Texture tex = TextureIO.newTexture(new File(strFilename), true);
            insert(strTexID, tex);
            return true;
            
        } catch (IOException ex) {
            Logger.getLogger(TextureManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GLException ex) {
            Logger.getLogger(TextureManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /*   public boolean LoadFromFile(Renderer renderer, String strFilename, String strTexID) {
    try {
    BufferedImage img = ImageIO.read(new File(strFilename));

    if (img == null)
    {
    System.out.print("Could not load image " + strFilename);
    return false;
    }


    TextureOld tex = new TextureOld();
    tex.mMipmap = true;
    tex.mWidth = img.getWidth();
    tex.mHeight = img.getHeight();

    switch (img.getType()) {
    case BufferedImage.TYPE_3BYTE_BGR:
    case BufferedImage.TYPE_CUSTOM: {
    byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
    tex.mData = ByteBuffer.allocateDirect(data.length);
    tex.mData.order(ByteOrder.nativeOrder());
    tex.mData.put(data, 0, data.length);
    break;
    }
    case BufferedImage.TYPE_INT_RGB: {
    int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    tex.mData = ByteBuffer.allocateDirect(data.length * 4); // 4 for int
    tex.mData.order(ByteOrder.nativeOrder());
    tex.mData.asIntBuffer().put(data, 0, data.length);
    break;
    }
    default:
    throw new RuntimeException("Unsupported image type " + img.getType());
    }


    renderer.linkTexture(tex);
    insert(strTexID, tex);

    } catch (IOException ex) {
    Logger.getLogger(TextureManager.class.getName()).log(Level.SEVERE, null, ex);
    }

    return true;

    }*/
}
