/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin.engine;

/**
 *
 * @author ben
 */
public interface RenderListener {

    void init();
    void update(int ndt);

    void draw(Renderer renderer);
}
