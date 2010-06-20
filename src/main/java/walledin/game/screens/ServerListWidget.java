package walledin.game.screens;

import java.util.ArrayList;
import java.util.List;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;

public class ServerListWidget extends Screen {
    Screen refreshButton;
    List<Screen> servers; // list of servers

    public ServerListWidget(final Screen parent, final Rectangle boudingRect) {
        super(parent, boudingRect);
        servers = new ArrayList<Screen>();
    }

    @Override
    public void initialize() {
        refreshButton = new Button(this, new Rectangle(0, -20, 100, 25),
                "Refresh", getPosition().add(new Vector2f(400, 40)));
        addChild(refreshButton);
    }

    @Override
    public void update(final double delta) {

        /** If clicked on refresh button, get server list */
        if (refreshButton.pointInScreen(Input.getInstance().getMousePos()
                .asVector2f())) {
            if (Input.getInstance().getMouseDown()) {
                servers.clear();

                // hardcode one for now
                Screen server = new Button(this,
                        new Rectangle(0, -20, 100, 25), "localhost",
                        getPosition().add(new Vector2f(10, 65)));

                // for the server list it is easier to keep track of them
                // manually
                server.registerScreenManager(getManager());
                servers.add(server);
            }
        }

        for (int i = 0; i < servers.size(); i++)
            servers.get(i).update(delta);

        // if clicked on server, load the game
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).pointInScreen(
                    Input.getInstance().getMousePos().asVector2f())) {

            }
        }

        super.update(delta);
    }

    @Override
    public void draw(final Renderer renderer) {
        final Font font = getManager().getFont("arial20");
        font.renderText(renderer, "Server Name",
                getPosition().add(new Vector2f(10, 40)));

        for (int i = 0; i < servers.size(); i++)
            servers.get(i).draw(renderer);

        // TODO Auto-generated method stub
        renderer.drawRectOutline(getRectangle().translate(getPosition()));
        super.draw(renderer);
    }

}
