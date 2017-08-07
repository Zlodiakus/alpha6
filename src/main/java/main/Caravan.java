package main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Well on 17.01.2016.
 * Caravan object
 */
public class Caravan {
    JSONObject jresult = new JSONObject();
    JSONArray jarr = new JSONArray();
    String GUID,PGUID,Start,Finish,StartName,FinishName;
    int Speed, MaxSpeed, Acceleration, Distance, Lat, Lng, LatS,LngS,LatF,LngF, Lifetime, Danger, profit, bonus;
    Connection con;


    public Caravan(Connection CON) {
        con=CON;
    }

    public Caravan(String CGUID, String CPGUID, String Start, String Finish, int PLat, int PLng, Connection CON) {
        GUID=CGUID;
        PGUID=CPGUID;
        StartName=Start;
        FinishName=Finish;
        Lat=PLat;
        Lng=PLng;
        con=CON;
    }

    public Caravan (String guid,String pguid,int lifetime, int danger,String start,String finish,int Bonus,int lat,int lng, int latS, int lngS, int latF, int lngF, int speed, Connection CON) {
        GUID=guid;
        PGUID=pguid;
        Lifetime=lifetime;
        Danger=danger;
        Start=start;
        Finish=finish;
        bonus=Bonus;
        Lat=lat;
        Lng=lng;
        LatS = latS;
        LngS = lngS;
        LatF = latF;
        LngF = lngF;
        Speed=speed;
        con=CON;
    }

    public void ambushed(String APGUID) {
        //MyUtils.Logwrite("Caravan.checkAmbushes", "Poor caravan was ambushed by evil rogues! GUID=" + GUID);
        try {
            PreparedStatement query = con.prepareStatement("delete from GameObjects where GUID=?");
            query.setString(1,GUID);
            query.execute();
            query.close();
            query = con.prepareStatement("delete from Caravans where GUID=?");
            query.setString(1,GUID);
            query.execute();
            query.close();
            con.commit();
            if (APGUID.equals("Elf"))
                MyUtils.Message(PGUID, "Ваш караван, идущий из " + StartName + " в " + FinishName + ", попал в эльфийскую засаду! ", 1,0,Lat,Lng);
            else
                MyUtils.Message(PGUID, "Ваш караван, идущий из " + StartName + " в " + FinishName + ", попал в засаду!", 1,0,Lat,Lng);
        } catch (SQLException e) {
            MyUtils.Logwrite("Caravan.checkAmbushes", "Error while removing caravan GUID=" + GUID+ "from base: " +e.toString());
        }
    }

    public void finish() {
        //int bonus=countBonus();
        Player player=new Player(PGUID,con);
        bonus=(int)(bonus*(float)(100+player.getPlayerUpgradeEffect2("bargain"))/100);
        player.getGold(bonus);
        City city = new City(Start,con);
        city.getGold(bonus,player.Race);
        city = new City(Finish,con);
        city.getGold(bonus, player.Race);
        if (Speed>0) {Lat=LatF;Lng=LngF;Speed=-1;}
        else {Lat=LatS;Lng=LngS;Speed=1;}
        update();
    }

    private void updateSpeed() {
        PreparedStatement query;
        try {
            query=con.prepareStatement("update Caravans set Speed=? WHERE GUID = ?");
            query.setInt(1,Speed);
            query.setString(2, GUID);
            query.execute();
            con.commit();
            query.close();
            MyUtils.Logwrite("Caravan.updateSpeed","GUID="+GUID+" updated to (Speed="+Speed+")");
        } catch (SQLException e) {
            MyUtils.Logwrite("Caravan.updateSpeed", "GUID=" + GUID + " SQL Error: " + e.toString());
        }

    }

/*    private int countBonus() {
        PreparedStatement query;
        int levelS,levelF,bonus;
        try {
            query=con.prepareStatement("select (select Level from Cities WHERE GUID = ?) levelS, (select Level from Cities WHERE GUID = ?) levelF from dual");
            query.setString(1, Start);
            query.setString(2, Finish);
            ResultSet rs=query.executeQuery();
            rs.first();
            levelS=rs.getInt("levelS");
            levelF=rs.getInt("levelF");
            bonus=(int)((Math.sqrt(levelS)*Math.sqrt(levelF)*Distance)/10);
            rs.close();
            query.close();
            //MyUtils.Logwrite("Caravan.countBonus", "GUID=" + GUID + " bonus should be " + bonus);
        } catch (SQLException e) {
            bonus=0;
            MyUtils.Logwrite("Caravan.countBonus", "GUID=" + GUID + " SQL Error: " + e.toString());
        }
        return bonus;
    }
*/
    public void update() {
        PreparedStatement query;
        try {
            query=con.prepareStatement("update GameObjects set Lat=?,Lng=? WHERE GUID = ?");
            query.setInt(1,Lat);
            query.setInt(2,Lng);
            query.setString(3,GUID);
            query.execute();
            query=con.prepareStatement("update Caravans set Speed=?, Lifetime=?, Danger=? WHERE GUID = ?");
            query.setInt(1,Speed);
            query.setInt(2,Lifetime);
            query.setInt(3,Danger);
            query.setString(4,GUID);
            query.execute();
            con.commit();
            query.close();
            //MyUtils.Logwrite("Caravan.update","GUID="+GUID+" updated to (Lat="+Lat+", Lng="+Lng+")");
        } catch (SQLException e) {
            MyUtils.Logwrite("Caravan.update", "GUID=" + GUID + " SQL Error: " + e.toString());
        }

    }


    public String StartRoute(String PGUID, String CGUID, Connection con) {
        String GUID= UUID.randomUUID().toString();
        JSONObject jobj = new JSONObject();
        PreparedStatement query;
        City city = new City(CGUID,con);
        try{
            query=con.prepareStatement("insert into Caravans (GUID,PGUID,Start,Speed) values (?,?,?,?) ");
            query.setString(1,GUID);
            query.setString(2,PGUID);
            query.setString(3,CGUID);
            query.setInt(4, 0);
            query.execute();
            con.commit();
            query=con.prepareStatement("select Lat,Lng from GameObjects where GUID=?");
            query.setString(1,CGUID);
            ResultSet rs=query.executeQuery();
            rs.first();
            Lat=rs.getInt(1);
            Lng=rs.getInt(2);
            query.close();
        } catch (SQLException e) {jresult.put("Result","DB001");jresult.put("Message","Ошибка обращения к БД.");return jresult.toString();}
        jresult.put("Result","OK");
        jobj.put("GUID",GUID);
        jobj.put("StartLat",Lat);
        jobj.put("StartLng",Lng);
        jobj.put("StartGUID",CGUID);
        jobj.put("StartName",city.Name);
        jresult.put("Route",jobj);
        return jresult.toString();
    }

    public JSONObject FinishRoute(String RGUID, String CGUID, int speed, int accel, int cargo, int trade, Connection con) {
        JSONObject jobj = new JSONObject();
        PreparedStatement query;
        int levelS,levelF;
        double t,t1,t2,s1;
        try{
            query=con.prepareStatement("select Lat,Lng from GameObjects where GUID=?");
            query.setString(1,CGUID);
            ResultSet rs=query.executeQuery();
            rs.first();
            Lat=rs.getInt(1);
            Lng=rs.getInt(2);
            //rs.close();
            query=con.prepareStatement("select z1.Lat,z1.Lng,z2.Start from GameObjects z1, Caravans z2 where z2.GUID=? and z2.Start=z1.GUID");
            query.setString(1,RGUID);
            rs=query.executeQuery();
            rs.first();
            LatS=rs.getInt("Lat");
            LngS=rs.getInt("Lng");
            Start=rs.getString("Start");

            if ((LatS==Lat) && (LngS==Lng)) {jresult.put("Result","O0605");jresult.put("Message","Ваш маршрут начинается в этом городе, вы не можете завершить маршрут в нем!");return jresult;}
            query=con.prepareStatement("insert into GameObjects (GUID,Lat,Lng,Type) values (?,?,?,'Caravan')");
            query.setString(1,RGUID);
            query.setInt(2,LatS);
            query.setInt(3,LngS);
            query.execute();

            query=con.prepareStatement("select Level, Name from Cities where GUID=?");
            query.setString(1,Start);
            rs=query.executeQuery();
            rs.first();
            levelS=rs.getInt("Level");
            StartName=rs.getString("Name");

            query=con.prepareStatement("select Level, Name from Cities where GUID=?");
            query.setString(1,CGUID);
            rs=query.executeQuery();
            rs.first();
            levelF=rs.getInt("Level");
            FinishName=rs.getString("Name");

            Distance=(int)MyUtils.distVincenty(LatS,LngS,Lat,Lng);
            //bonus=(int)((Math.sqrt(levelS*levelF)*Distance*cargo)/1000);
            bonus=(int) ((1+(float)levelS/20)*(1+(float)levelF/20)*((float)Distance*cargo/1000)*(1+(float)trade/100));
            t1=(double)(speed-1-accel)/accel;
            s1=(double)((1+accel)*t1+accel*t1*t1/2);
            t2=(double)(Distance-s1)/speed;
            t=t1+t2;
            profit=(int)(60*bonus/t);
            query=con.prepareStatement("update Caravans set Finish=?,Speed=1,Distance=?, bonus=?,profit=? where GUID=?");
            query.setString(1,CGUID);
            query.setInt(2,Distance);
            query.setInt(3,bonus);
            query.setInt(4,profit);
            query.setString(5,RGUID);
            query.execute();
            con.commit();
//            rs.close();
            query.close();
        } catch (SQLException e) {MyUtils.Logwrite("Caravan.FinishRoute",e.toString());jresult.put("Result","BD001");jresult.put("Message","Ошибка обращения к БД"); return jresult;}
        jresult.put("Result","OK");
        jobj.put("GUID", RGUID);
        jobj.put("Lat",Lat);
        jobj.put("Lng",Lng);
        jobj.put("StartGUID", Start);
        jobj.put("StartName", StartName);
        jobj.put("FinishGUID", CGUID);
        jobj.put("FinishName", FinishName);
        jobj.put("Distance", Distance);
        jobj.put("profit", profit);
        jobj.put("StartLat",LatS);
        jobj.put("StartLng",LngS);
        jobj.put("FinishLat",Lat);
        jobj.put("FinishLng",Lng);
        jresult.put("Route",jobj);
        return jresult;
    }

/*
    public String fix () {
        PreparedStatement query,query2;
        try {
            query=con.prepareStatement("select z2.GUID,z1.Lat LatS,z1.Lng LngS,z3.Lat LatF,z3.Lng LngF from GameObjects z1, Caravans z2, GameObjects z3 where z2.Start=z1.GUID and z2.Finish=z3.GUID");
            ResultSet rs=query.executeQuery();
            while (rs.next()) {
                GUID = rs.getString("GUID");
                LatS = rs.getInt("LatS");
                LngS = rs.getInt("LngS");
                LatF = rs.getInt("LatF");
                LngF = rs.getInt("LngF");
                Distance = (int) MyUtils.distVincenty(LatS, LngS, LatF, LngF);
                query2 = con.prepareStatement("update Caravans set Distance=? where GUID=?");
                query2.setInt(1, Distance);
                query2.setString(2, GUID);
                query2.execute();
                con.commit();
                query2.close();
            }
            rs.close();
            query.close();
        } catch (SQLException e) {
            return e.toString();
        }
        return "Done";
    }
*/
    public void generateAmbush() {
        double randCheck=10000*Math.random();
        if (Danger>=randCheck) {
            if (Math.random()>=0) {
                String OwnerGUID = "Elf";
                //String AmbGUID = UUID.randomUUID().toString();
                int ambLat, ambLng;
                double k = Math.random();
                ambLat = (int) (LatS + k * (LatF - LatS));
                ambLng = (int) (LngS + k * (LngF - LngS));
                Ambush ambush = new Ambush();
                ambush.Set(OwnerGUID, ambLat, ambLng, 20, -180, 1, true, con);
            }
            Danger=0;
        }
    }
}
