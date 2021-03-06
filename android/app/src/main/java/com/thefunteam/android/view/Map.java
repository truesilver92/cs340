package com.thefunteam.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import com.thefunteam.android.model.Cord;
import com.thefunteam.android.model.Model;
import com.thefunteam.android.model.shared.City;
import com.thefunteam.android.model.shared.MapHelper;
import com.thefunteam.android.model.shared.Player;
import com.thefunteam.android.model.shared.Route;

import java.util.List;

public class Map extends View {
    Paint paint = new Paint();

    List<Route> openRoutes;
    List<Player> players;

    final float fontSize = 20.0f;
    private void init() {
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        paint.setStyle(Paint.Style.FILL);
    }

    public Map(Context context) {
        super(context);
        init();
    }

    public Map(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Map(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // Draw all routes
        paint.setStrokeWidth(6.0f);
        if(openRoutes != null) {
            for(int i = 0; i < openRoutes.size(); i++) {
                Route route = openRoutes.get(i);
                Cord cord1 = MapHelper.getLocation(route.getCity1());
                Cord cord2 = MapHelper.getLocation(route.getCity2());
                if(cord2.y > cord1.y) {
                    Cord temp = cord2;
                    cord2 = cord1;
                    cord1 = temp;
                }
                MapHelper.adjust(cord1, cord2, route);
                paint.setColor(MapHelper.getColor(route.getType()));
                canvas.drawLine(cord1.x, cord1.y, cord2.x, cord2.y, paint);
            }
        }
        if(players != null) {
            for(int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                if(player == null) { continue; }
                List<Route> routes = player.getRoutes();
                if(routes == null) { continue; }

                for(int j = 0; j < routes.size(); j++) {
                    Route route = routes.get(j);
                    Cord cord1 = MapHelper.getLocation(route.getCity1());
                    Cord cord2 = MapHelper.getLocation(route.getCity2());
                    if(cord2.y > cord1.y) {
                        Cord temp = cord2;
                        cord2 = cord1;
                        cord1 = temp;
                    }
                    MapHelper.adjust(cord1, cord2, route);
                    paint.setStrokeWidth(6.0f);
                    paint.setColor(MapHelper.getColor(route.getType()));
                    canvas.drawLine(cord1.x, cord1.y, cord2.x, cord2.y, paint);

//                    paint.setStrokeWidth(6.0f);
//                    paint.setColor(MapHelper.getPlayerColor(i));
//                    canvas.drawLine(cord1.x, cord1.y, cord2.x, cord2.y, paint);
                }
            }
        }
        paint.setColor(Color.BLACK);

        // Draw all route scores
        if(openRoutes != null) {
            for(int i = 0; i < openRoutes.size(); i++) {
                Route route = openRoutes.get(i);
                Cord cord1 = MapHelper.getLocation(route.getCity1());
                Cord cord2 = MapHelper.getLocation(route.getCity2());
                if(cord2.y > cord1.y) {
                    Cord temp = cord2;
                    cord2 = cord1;
                    cord1 = temp;
                }
                MapHelper.adjust(cord1, cord2, route);
                Cord middle = Cord.middle(cord1, cord2);

                paint.setColor(Color.BLACK);
                Path path = new Path();
                if(route.second) {
                    path.moveTo(middle.x, middle.y);
                    path.lineTo(middle.x + 30, middle.y - 15);
                    path.lineTo(middle.x + 30, middle.y + 15);
                    path.lineTo(middle.x, middle.y);
                    canvas.drawPath(path, paint);

                    paint.setColor(Color.WHITE);
                    canvas.drawText(Integer.toString(route.getLength()), middle.x + 15, middle.y + fontSize/4, paint);
                } else {
                    path.moveTo(middle.x, middle.y);
                    path.lineTo(middle.x - 30, middle.y - 15);
                    path.lineTo(middle.x - 30, middle.y + 15);
                    path.lineTo(middle.x, middle.y);
                    canvas.drawPath(path, paint);

                    paint.setColor(Color.WHITE);
                    canvas.drawText(Integer.toString(route.getLength()), middle.x - 25, middle.y + fontSize/4, paint);
                }
            }
        }
        if(players != null) {
            for(int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                if(player == null) { continue; }
                List<Route> routes = player.getRoutes();
                if(routes == null) { continue; }

                for(int j = 0; j < routes.size(); j++) {
                    Route route = routes.get(j);
                    Cord cord1 = MapHelper.getLocation(route.getCity1());
                    Cord cord2 = MapHelper.getLocation(route.getCity2());
                    if(cord2.y > cord1.y) {
                        Cord temp = cord2;
                        cord2 = cord1;
                        cord1 = temp;
                    }
                    MapHelper.adjust(cord1, cord2, route);
                    Cord middle = Cord.middle(cord1, cord2);

                    paint.setColor(MapHelper.getPlayerColor(i));
                    Path path = new Path();
                    if(route.second) {
                        path.moveTo(middle.x, middle.y);
                        path.lineTo(middle.x + 30, middle.y - 15);
                        path.lineTo(middle.x + 30, middle.y + 15);
                        path.lineTo(middle.x, middle.y);
                        canvas.drawPath(path, paint);

                        paint.setColor(Color.WHITE);
                        canvas.drawText(Integer.toString(route.getLength()), middle.x + 15, middle.y + fontSize/4, paint);
                    } else {
                        path.moveTo(middle.x, middle.y);
                        path.lineTo(middle.x - 30, middle.y - 15);
                        path.lineTo(middle.x - 30, middle.y + 15);
                        path.lineTo(middle.x, middle.y);
                        canvas.drawPath(path, paint);

                        paint.setColor(Color.WHITE);
                        canvas.drawText(Integer.toString(route.getLength()), middle.x - 25, middle.y + fontSize/4, paint);
                    }
                }
            }
        }

        // Draw cities
        City[] allCities = MapHelper.allCities;
        for(int i = 0; i < allCities.length; i++) {
            City city = allCities[i];
            Cord cord = MapHelper.getLocation(city);
            String name = MapHelper.getName(city);
            paint.setColor(Color.BLACK);
            canvas.drawCircle(cord.x, cord.y, 10.0f, paint);

            paint.setColor(Color.rgb(255,100,100));
            canvas.drawText(name, cord.x, cord.y, paint);
        }
    }

    public void updateRoutes(Model model) {
        if(model.getCurrentGame() == null) { return; }
        openRoutes = model.getCurrentGame().getOpenRoutes();
        players = model.getCurrentGame().getPlayers();
        this.invalidate();
    }
}
