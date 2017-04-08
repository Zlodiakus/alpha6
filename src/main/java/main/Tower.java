package main;

import org.json.simple.JSONObject;

import javax.print.attribute.standard.MediaSize;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Zlodiak on 10.09.2016.
 */
public class Tower {
    Connection con;
    String GUID;
    String PGUID;
    int Lat;
    int Lng;
    String Name;
    int Level;
    int Race;
    String TowerText;
    int Obsidian;
    boolean loaded=false;
    boolean bderror=false;

    public Tower (Connection CON) {
        con=CON;
    }

    public void load(String TGUID) {
        //TODO Создать таблицу Items
        PreparedStatement query;
        loaded=false;
        bderror=false;
        GUID=TGUID;
        try {
            query = con.prepareStatement("select z1.PGUID,z1.text,z1.name,z1.level,z1.obsidian,z2.Lat,z2.Lng,z3.Race from Towers z1, GameObjects z2, Players z3 where z1.GUID=? and z2.GUID=? and z3.GUID=z1.PGUID");
            query.setString(1, GUID);
            query.setString(2, GUID);
            ResultSet rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                loaded = true;
                rs.first();
                PGUID=rs.getString("PGUID");
                Lat = rs.getInt("Lat");
                Lng = rs.getInt("Lng");
                TowerText = rs.getString("text");
                Name = rs.getString("name");
                Level = rs.getInt("level");
                Obsidian = rs.getInt("obsidian");
                Race=rs.getInt("Race");
            }
        } catch (SQLException e) {MyUtils.Logwrite("Tower.load","Error: "+e.toString());bderror=true;}
    }

    public boolean set(String PGUID, String PName, int TLAT, int TLNG)
    {
        PreparedStatement query;
        try {
            query = con.prepareStatement("insert into towers (GUID, PGUID, name) values (?,?,?)");
            GUID= UUID.randomUUID().toString();
            query.setString(1,GUID);
            query.setString(2,PGUID);
            query.setString(3,PName);
            query.execute();
            query = con.prepareStatement("insert into GameObjects (GUID, Lat, Lng, Type) values (?,?,?,'Tower')");
            query.setString(1,GUID);
            query.setInt(2,TLAT);
            query.setInt(3,TLNG);
            query.execute();
            con.commit();
            return true;
        } catch (SQLException e) {MyUtils.Logwrite("Tower.set","Error: "+e.toString());return false;}
    }

    public boolean setText(String TGUID, String text)
    {
        PreparedStatement query;
        try {
            query = con.prepareStatement("update towers set text=? where GUID=?");
            query.setString(1,text);
            query.setString(2,TGUID);
            query.execute();
            con.commit();
            return true;
        } catch (SQLException e) {MyUtils.Logwrite("Tower.setText","Error: "+e.toString());return false;}
    }

    public void update() {
        PreparedStatement query;
        try {
            query = con.prepareStatement("update towers set text=?, name=?, level=?,obsidian=? where GUID=?");
            query.setString(1,TowerText);
            query.setString(2, Name);
            query.setInt(3, Level);
            query.setInt(4, Obsidian);
            query.setString(5,GUID);
            query.execute();
            con.commit();
        } catch (SQLException e) {MyUtils.Logwrite("Tower.update","Error: "+e.toString());}

    }

    public String getInfo(String TGUID) {
        JSONObject jresult = new JSONObject();
        PreparedStatement query, query2;
        ResultSet rs, rs2;
        try {
            query = con.prepareStatement("select PGUID, text from towers where GUID=?");
            query.setString(1, TGUID);
            rs = query.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.first();
                query2 = con.prepareStatement("select Name, Race, Level from Players where GUID=?");
                query2.setString(1, rs.getString("PGUID"));
                rs2 = query2.executeQuery();
                if (rs2.isBeforeFirst()) {
                    rs2.first();
                    jresult.put("Name", rs2.getString("Name"));
                    jresult.put("Race", rs2.getInt("Race"));
                    jresult.put("Level", rs2.getInt("Level"));
                    jresult.put("Text", rs.getString("text"));
                } else {
                    jresult.put("Result", "DB001");
                    jresult.put("Message", "Ошибка обращения к БД");
                    MyUtils.Logwrite("Tower.getInfo", "Player not found");
                }
            } else {
                jresult.put("Result", "DB001");
                jresult.put("Message", "Ошибка обращения к БД");
                MyUtils.Logwrite("Tower.getInfo", "Tower not found");
            }
        } catch (SQLException e) {
            jresult.put("Result", "DB001");
            jresult.put("Message", "Ошибка обращения к БД");
            MyUtils.Logwrite("Tower.getInfo", "Error: " + e.toString());
        }
        return jresult.toString();
    }
}
