package walledin.game.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.network.ServerData;

public class ServerListWidget extends Screen {
    Screen refreshButton;
    List<ServerData> serverList; // list of servers
    List<Screen> serverButtons; // list of buttons

    public ServerListWidget(final Screen parent, final Rectangle boudingRect) {
        super(parent, boudingRect);
        serverButtons = new ArrayList<Screen>();
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
                serverButtons.clear();

                // request a refresh
                getManager().getClient().refreshServerList();
            }
        }

        serverList = new ArrayList<ServerData>(
                getManager().getClient().getServerList());
        
        serverButtons.clear();
        
        for (int i = 0; i < serverList.size(); i++) {
            Screen server = new Button(this,
                    new Rectangle(0, -20, 100, 25), serverList.get(i).getName(),
                    getPosition().add(new Vector2f(10, 65 + i * 20)));
            server.registerScreenManager(getManager());
            serverButtons.add(server);
        }

        for (int i = 0; i < serverButtons.size(); i++) {
            serverButtons.get(i).update(delta);
        }

        // if clicked on server, load the game
        for (int i = 0; i < serverButtons.size(); i++) {
            if (serverButtons.get(i).pointInScreen(
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

        for (int i = 0; i < serverButtons.size(); i++)
            serverButtons.get(i).draw(renderer);

        // TODO Auto-generated method stub
        renderer.drawRectOutline(getRectangle().translate(getPosition()));
        super.draw(renderer);
    }

}
