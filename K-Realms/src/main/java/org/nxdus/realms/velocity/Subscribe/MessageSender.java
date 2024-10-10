package org.nxdus.realms.velocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.core.velocity.KCoreVelocity;

public class MessageSender {

    private static final Gson gson = new Gson();

    public static void copyPaste(
            RegisteredServer server,
            String worldOriginalName,
            String worldCloneName,
            double radius,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "copy-paste");
        jsonObject.addProperty("world-original", worldOriginalName);
        jsonObject.addProperty("world-clone", worldCloneName);
        jsonObject.addProperty("radius", radius);
        jsonObject.addProperty("coordinate-x-one",x1);
        jsonObject.addProperty("coordinate-y-one",y1);
        jsonObject.addProperty("coordinate-z-one",z1);
        jsonObject.addProperty("coordinate-x-two",x2);
        jsonObject.addProperty("coordinate-y-two",y2);
        jsonObject.addProperty("coordinate-z-two",z2);

        KCoreVelocity.redisManager.publish(server.getServerInfo().getName(), gson.toJson(jsonObject));
    }

}
