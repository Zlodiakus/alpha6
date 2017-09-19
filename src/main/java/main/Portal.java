package main;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static main.MyUtils.Logwrite;
import static main.MyUtils.Message;

public class Portal {
    int Level, Gold, Obsidian, nextGold, nextObsidian;
    int Race;
    Connection con;

    public Portal(int RACE, Connection CON) {
        try{
            con=CON;
            Race=RACE;
            PreparedStatement query=con.prepareStatement("select Gold, Obsidian, portalLevel from Fractions where Id=?");
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
        Logwrite("Portal.getInfo","Start.");
        JSONObject jresult = new JSONObject();
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();

        jobj.put("Type", "Gold");
        jobj.put("Quantity", Gold);
        jarr.add(jobj);
        jobj = new JSONObject();
        jobj.put("Type", "Obsidian");
        jobj.put("Quantity", Obsidian);
        jarr.add(jobj);
        jresult.put("portalLevel",Level);
        jresult.put("portalRes",jarr);

        jarr = new JSONArray();
        jobj = new JSONObject();
        jobj.put("Type", "Gold");
        jobj.put("Quantity", nextGold);
        jarr.add(jobj);
        jobj = new JSONObject();
        jobj.put("Type", "Obsidian");
        jobj.put("Quantity", nextObsidian);
        jarr.add(jobj);

        jresult.put("portalNeed",jarr);
        Logwrite("Portal.getInfo","Finish.");
        return jresult;
    }

    public JSONObject Donate(int GOLD, int OBSIDIAN) {
        Gold+=GOLD;
        Obsidian+=OBSIDIAN;
        checkForLevel();
        if (save()) {
            return getInfo();
        }
        else {
            JSONObject jresult =new JSONObject();
            jresult.put("Result","DB001");
            return jresult;
        }
    }

    private void setPortalEffect(int level) {
        try{
            PreparedStatement query=con.prepareStatement("insert into effects (PGUID,effect,value,source) (select ?,effect,value,'portal' from portalEffects where level=?) ON DUPLICATE KEY UPDATE value=(select value from portalEffects where level=?)");
            query.setString(1,Integer.toString(Race));
            query.setInt(2,level);
            query.setInt(3,level);
            query.execute();
            con.commit();
        }
        catch (SQLException e) {Logwrite("setPortalEffect","SQL Error: "+e.toString());}
    }

    private void checkForLevel() {
        if (Gold>=nextGold && Obsidian >=nextObsidian) {
            Level+=1;
            //добавить/обновить эффект
            setPortalEffect(Level);
            //Отправить сообщения всем игрокам фракции
            MyUtils.MessageFrac(Race,"Построен уровень портала фракции! Текущий уровень: "+Level,10,0,0,0);
            Gold-=nextGold;
            Obsidian-=nextObsidian;
            portalNeed();
            checkForLevel();
        }
    }

    private boolean save() {
        try{
            PreparedStatement query=con.prepareStatement("update Fractions set Gold=?, Obsidian=?, portalLevel=? where Id=?");
            query.setInt(1,Gold);
            query.setInt(2,Obsidian);
            query.setInt(3,Level);
            query.setInt(4,Race);
            query.execute();
            con.commit();
            return true;
        }
        catch (SQLException e) {Logwrite("Portal.save","SQL Error: "+e.toString());return false;}
    }
}

