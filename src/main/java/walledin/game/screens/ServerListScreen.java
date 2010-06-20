package walledin.game.screens;

import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.MessageType;

public class ServerListScreen extends Screen {
    Screen serverListWidget;

    public ServerListScreen() {
        super(null, null);
    }

    @Override
    public void initialize() {
        serverListWidget = new ServerListWidget(this, new Rectangle(0, 0, 500, 400));
        serverListWidget.setPosition(new Vector2f(100, 0));
        addChild(serverListWidget);
        serverListWidget.initialize(); // initialize after add!
        
    }
    
    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);
        getManager().getCursor().sendMessage(MessageType.RENDER, renderer);
    }

}
