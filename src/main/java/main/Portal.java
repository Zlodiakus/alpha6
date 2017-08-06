package main;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static main.MyUtils.Logwrite;

public class Portal {
    int Level, Gold, Obsidian, nextGold, nextObsidian;
    int Race;
    Connection con;

    public Portal(int RACE, Connection CON) {
        try{
            con=CON;
            Race=RACE;
            PreparedStatement query=con.prepareStatement("select Gold, Obsidian, portalLevel from Fraction where Id=?");
            query.setInt(1,Race);
            ResultSet rs=query.executeQuery();
            rs.first();
            Level=rs.getInt("portalLevel");
            Gold=rs.getInt("Gold");
            Obsidian=rs.getInt("Obsidian");
            portalNeed();
        }
        catch (SQLException e) {Logwrite("Portal","SQL Error: "+e.toString());}
    }

  /*  private JSONArray portalNeed() {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try{
            PreparedStatement query=con.prepareStatement("select Gold, Obsidian from portalCost where level=?");
            query.setInt(1,Level+1);
            ResultSet rs=query.executeQuery();
            rs.first();
            jobj.put("Gold",rs.getInt("Gold"));
            jobj.put("Obsidian",rs.getInt("Obsidian"));
            jarr.add(jobj);
            return jarr;
        }
        catch (SQLException e) {Logwrite("portalNeed","SQL Error: "+e.toString());jarr.clear();return jarr;}
    }
*/

      private void portalNeed() {
        //TODO Проверку на максимальный уровень
          try{
            PreparedStatement query=con.prepareStatement("select Gold, Obsidian from portalCost where level=?");
            query.setInt(1,Level+1);
            ResultSet rs=query.executeQuery();
            rs.first();
            nextGold = rs.getInt("Gold");
            nextObsidian = rs.getInt("Obsidian");
        }
        catch (SQLException e) {Logwrite("portalNeed","SQL Error: "+e.toString());}
    }



    public JSONObject getInfo() {
        JSONObject jresult = new JSONObject();
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();

        jobj.put("Gold", Gold);
        jobj.put("Obsidian", Obsidian);
        jarr.add(jobj);
        jresult.put("portalLevel",Level);
        jresult.put("portalRes",jarr);

        jarr = new JSONArray();
        jobj = new JSONObject();
        jobj.put("Gold", nextGold);
        jobj.put("Obsidian", nextObsidian);
        jarr.add(jobj);

        jresult.put("portalNeed",jarr);
        return jresult;
    }

    public JSONObject Donate(int GOLD, int OBSIDIAN) {
        Gold+=GOLD;
        Obsidian+=OBSIDIAN;
        checkForLevel();
        save();
        return getInfo();
    }

    private void checkForLevel() {
        if (Gold>=nextGold && Obsidian >=nextObsidian) {
            Level+=1;
            Gold-=nextGold;
            Obsidian-=nextObsidian;
            portalNeed();
            checkForLevel();
        }
    }

    private void save() {
        try{
            PreparedStatement query=con.prepareStatement("update Fraction set Gold=?, Obsidian=?, portalLevel=? where Id=?");
            query.setInt(1,Gold);
            query.setInt(2,Obsidian);
            query.setInt(3,Level);
            query.setInt(4,Race);
            query.execute();
        }
        catch (SQLException e) {Logwrite("Portal.save","SQL Error: "+e.toString());}
    }
}

