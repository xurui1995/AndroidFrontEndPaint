package ie.tcd.asepaint2020.logic;


import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import ie.tcd.asepaint2020.logic.game.RemotePlayer;
import ie.tcd.asepaint2020.logic.internal.CollidableCircle;
import ie.tcd.asepaint2020.logic.internal.Point;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BackendNetworkSync implements NetworkSync{
    OkHttpClient client = new OkHttpClient();
    String Endpoint = "://42.42.43.12:8080/";
    Boolean MatchMakingFinished = false;

    Boolean GameReady = false;

    private Float StartTime;

    Integer seed;
    Integer userColor;
    Integer roundID;
    Integer userID;

    Handler handler = new Handler();

    String flashmsg;

    public BackendNetworkSync() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    final String s = enroll();
                    final JSONObject resp = new JSONObject(s);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                seed = resp.getInt("seed");
                                userColor = resp.getInt("userColor");
                                roundID = resp.getInt("roundId");
                                userID = resp.getInt("userId");
                                MatchMakingFinished = true;
                                Log.d("BackendNetworkSync",s);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Integer userID = resp.getInt("userId");
                    startws(userID);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private String enroll() throws IOException {
        Request req = new Request.Builder().url("http" + Endpoint + "lobby").build();
        try (Response response = client.newCall(req).execute()) {
            return response.body().string();
        }
    }

    class Ws extends WebSocketListener {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            Log.d("BackendNetworkSync",response.toString());
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            Log.d("BackendNetworkSync", text );
            final NetworkPaintSync np = new NetworkPaintSync();
            try {
                if(text.startsWith("success")){
                    MatchMakingFinished = true;
                    return;
                }
                JSONObject jo = new JSONObject(text);
                String Event = jo.getString("eventType");
                switch (Event){
                    case "connect":
                        break;
                    case "gameStart":
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                GameReady = true;
                            }
                        });
                        break;
                    case "HitNeg":
                        flashmsg = "Missed";
                        break;
                    case "HitPos":
                        flashmsg = "Hitted";
                        np.FromJson(text);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                buf.add(np);
                            }
                        });
                        break;
                    default:
                        Log.d("BackendNetworkSync","Unknown Event Type" + Event );
                        return;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    WebSocket ws;
    private void startws(Integer userID){
        Request request = new Request.Builder().url("ws" + Endpoint + "websocket/" + userID.toString()).build();

        ws = client.newWebSocket(request, new Ws());

        client.dispatcher().executorService().shutdown();
    }

    private void uploadHit(CollidableCircle cc){
        NetworkPaintSync nps = new NetworkPaintSync();
        nps.Location = cc.GetPrincipleLocation();
        nps.Size = Math.round(cc.GetPrincipleSize());
        nps.Event = "Hit";
        try {
            ws.send(nps.ToJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean IsMatchMakingFinished() {
        return MatchMakingFinished;
    }

    @Override
    public Float GetTimeBeforeGameStart() {
        if (!GameReady){
            return 1f;
        }
        StartTime = Float.valueOf(System.currentTimeMillis());
        return (StartTime - Float.valueOf(System.currentTimeMillis())) / 1000f;
    }

    @Override
    public List<RemotePlayer> GetPlayers() {
        return new ArrayList<RemotePlayer>();
    }
    private List<NetworkPaint> buf = new LinkedList<>();
    @Override
    public void SubmitHit(CollidableCircle cc) {
        uploadHit(cc);
    }

    @Override
    public List<NetworkPaint> GetNewConfirmedHits() {
        List<NetworkPaint> bufv = buf;
        buf = new LinkedList<>();
        return bufv;
    }

    public String GetFlashMsg(){
        String fm = flashmsg;
        flashmsg = null;
        return fm;
    }

}
class NetworkPaintSync implements NetworkPaint {
    public String Event;
    public Integer Size;
    public Integer ID;
    public Point Location;
    public void FromJson(String s) throws JSONException {
        JSONObject jo = new JSONObject(s);
        Event = jo.getString("eventType");
        JSONObject detail = jo.getJSONObject("detail");
        Size = detail.getInt("Size");
        //ID = detail.getInt("ID");
        Location = new Point((float)detail.getInt("LocationX"),(float)detail.getInt("LocationY"));
    }
    public String ToJson() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("eventType",Event);
        JSONObject detail = new JSONObject();
        detail.put("Size",Size);
        detail.put("ID",ID);
        detail.put("LocationX",Math.round(Location.getX()));
        detail.put("LocationY",Math.round(Location.getY()));
        jo.put("detail",detail);
        return jo.toString();

    }
    @Override
    public Integer OwnerID() {
        return 0;
    }

    @Override
    public Point Location() {
        return Location;
    }

    @Override
    public Float Size() {
        return (float)Size;
    }
}
