package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Zlodiak on 10.09.2016.
 */
public class Chest {
    String GUID;
    String type="gold";
    int bonus=0;

    Connection con;
    public Chest (Connection CON) {
        con=CON;
    }

    public void generate(int cityLevel, int TLAT, int TLNG)
    {
        MyUtils.Logwrite("Chest.generate", "Start");

        int chestBonus;
        String chestType;
        int a[]={0,1};
        int LAT, LNG;

        a=generateRandomCoords(TLAT,TLNG,500);
        LAT=a[0];
        LNG=a[1];

        if (Math.random()*100<1) { //исключили вариант с обсидианом
            chestBonus=1;
            chestType="obsidian";
        }
        else {
            chestBonus=100+(int)(60*(cityLevel-1)*(cityLevel-1) + Math.random()*350*(2*cityLevel-1));
            chestType="gold";
        }

        chestGen(chestBonus,LAT,LNG,chestType,cityLevel);

        MyUtils.Logwrite("Chest.generate", "Finish");
    }

    public void generateAmbushBonus(int initBonus,int initLat, int initLng) {

        MyUtils.Logwrite("Chest.generateAmbushBonus", "Start");

        int a[]={0,1};
        int LAT, LNG;

        a=generateRandomCoords(initLat,initLng,500);
        LAT=a[0];
        LNG=a[1];

        String chestType="gold";
        int chestLevel=21;//считать геморно и пока не нужно? 21 позволяет легко отследить сундуки засад

        chestGen(initBonus,LAT,LNG,chestType,chestLevel);

        MyUtils.Logwrite("Chest.generateAmbushBonus", "Finish");
    }

    public int[] generateRandomCoords(int TLAT, int TLNG, int Tradius) {

        int[] a = {0,1};
        int LAT, LNG;
        int randLat, randLng, maxRandLng, minRandLng;

        randLat = (int) (Math.random() * 2* Tradius) - Tradius;
        maxRandLng = (int) Math.sqrt(Tradius * Tradius - randLat * randLat);
        if (Math.abs(randLat) < 125) {
            minRandLng = (int) Math.sqrt(125 * 125 - randLat * randLat);
        } else minRandLng = 0;
        randLng = (int) Math.signum((Math.random() * 20000 - 10000)) * (minRandLng + (int) (Math.random() * (maxRandLng - minRandLng)));
        int delta_lat_rand = (int) (1000000 * Math.asin((180 / 3.1415926) * (randLat) / (6378137)));
        int delta_lng_rand = (int) (1000000 * Math.asin((180 / 3.1415926) * (randLng) / (6378137 * Math.cos((TLAT / 1000000) * 3.1415926 / 180))));
        LAT = TLAT + delta_lat_rand;
        LNG = TLNG + delta_lng_rand;
        a[0]=LAT;
        a[1]=LNG;
        return a;
    }

    public void chestGen(int chestBonus, int chestLat, int chestLng, String chestType, int chestLevel)
    {
        DateFormat DTMS = new SimpleDateFormat("dd.MM.yyyy HH");
        Date date = new Date();
        PreparedStatement query, query0;
        try{
            query0=con.prepareStatement("select count(1) from GameObjects where Type='City' and 100>=round(6378137 * acos(cos(Lat / 1e6 * PI() / 180) * cos(? / 1e6 * PI() / 180) * cos(Lng / 1e6 * PI() / 180 - ? / 1e6 * PI() / 180) + sin(Lat / 1e6 * PI() / 180) * sin(? / 1e6 * PI() / 180)))");
            query0.setInt(1,chestLat);
            query0.setInt(2,chestLng);
            query0.setInt(3,chestLat);
            ResultSet rs = query0.executeQuery();
            rs.first();
            if (rs.getInt(1)>0) {MyUtils.Logwrite("Chest.generate", "Слишком близко к городу. "+chestLat+"|"+chestLng);}
            else {
                query = con.prepareStatement("insert into tchests (GUID,level,type,value,created) VALUES (?,?,?,?,?)");
                String chestGUID = UUID.randomUUID().toString();
                query.setString(1, chestGUID);
                query.setInt(2, chestLevel);
                query.setString(3, chestType);
                query.setInt(4, chestBonus);
                query.setString(5, DTMS.format(date));
                query.execute();
                query.close();

                query = con.prepareStatement("INSERT INTO GameObjects(GUID,Lat,Lng,Type)VALUES(?,?,?,'Chest')");
                query.setString(1, chestGUID);
                query.setInt(2, chestLat);
                query.setInt(3, chestLng);
                query.execute();
                query.close();
                con.commit();
            }
            query0.close();
        } catch (SQLException e) {
            MyUtils.Logwrite("Chest.generate", "Error:" + e.toString());
        }
    }

    public void open(String PGUID, String ChGUID) {
        PreparedStatement query;
        GUID=ChGUID;
        try {
            query = con.prepareStatement("select type,value from tchests where GUID=?");
            query.setString(1, GUID);
            ResultSet rs = query.executeQuery();
            rs.first();
            type = rs.getString("type");
            bonus = rs.getInt("value");
        } catch (SQLException e) {type="gold"; bonus= 0;MyUtils.Logwrite("Chest.open","SQL Error: "+e.toString());}
    }

    public void delete() {
        PreparedStatement query;
        try {
            query = con.prepareStatement("delete from tchests where GUID=?");
            query.setString(1, GUID);
            query.execute();
            query = con.prepareStatement("delete from GameObjects where GUID=?");
            query.setString(1, GUID);
            query.execute();
            con.commit();
        } catch (SQLException e) {MyUtils.Logwrite("Chest.delete","Error: "+e.toString());}

    }

}
